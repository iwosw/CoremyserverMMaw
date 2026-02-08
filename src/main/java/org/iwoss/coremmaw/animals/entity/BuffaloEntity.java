package org.iwoss.coremmaw.animals.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.iwoss.coremmaw.init.ModEntities;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;

public class BuffaloEntity extends Animal implements GeoEntity {

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private static final EntityDataAccessor<Boolean> IS_PANICKING =
            SynchedEntityData.defineId(BuffaloEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ATTACKING =
            SynchedEntityData.defineId(BuffaloEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_STRIKING =
            SynchedEntityData.defineId(BuffaloEntity.class, EntityDataSerializers.BOOLEAN);

    private BuffaloEntity herdLeader;
    private int groupCheckTimer = 0;
    private int panicTimer = 0;
    private int attackResetTimer = 0;
    private int strikeTimer = 0;

    public BuffaloEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.setMaxUpStep(1.0F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_PANICKING, false);
        this.entityData.define(IS_ATTACKING, false);
        this.entityData.define(IS_STRIKING, false);
    }

    public boolean isPanicking() { return entityData.get(IS_PANICKING); }
    public void setPanicking(boolean v) { entityData.set(IS_PANICKING, v); }

    public boolean isAttacking() { return entityData.get(IS_ATTACKING); }
    public void setAttacking(boolean v) { entityData.set(IS_ATTACKING, v); }

    public boolean isStriking() { return entityData.get(IS_STRIKING); }
    public void setStriking(boolean v) { entityData.set(IS_STRIKING, v); }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.2D)
                .add(Attributes.FOLLOW_RANGE, 32.0D)
                .add(Attributes.ATTACK_SPEED, 1.2D);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new CustomMeleeAttackGoal(this, 1.3D, true));
        goalSelector.addGoal(2, new BuffaloPanicGoal(this));
        goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        goalSelector.addGoal(4, new TemptGoal(this, 1.25D, Ingredient.of(Items.WHEAT), false));
        goalSelector.addGoal(5, new FollowParentGoal(this, 1.25D));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        goalSelector.addGoal(8, new RandomStrollGoal(this, 0.9D));

        targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return !mob.isBaby() && super.canUse();
            }
        }.setAlertOthers(BuffaloEntity.class));
    }

    static class CustomMeleeAttackGoal extends MeleeAttackGoal {
        public CustomMeleeAttackGoal(PathfinderMob mob, double speed, boolean pause) {
            super(mob, speed, pause);
        }

        @Override
        public void tick() {
            super.tick();

            LivingEntity target = mob.getTarget();
            if (target != null) {
                double distSqr = mob.distanceToSqr(target);
                double reach = getAttackReachSqr(target);

                if (distSqr <= reach) {
                    BuffaloEntity buffalo = (BuffaloEntity) mob;
                    if (!buffalo.isStriking()) {
                        buffalo.setStriking(true);
                        buffalo.strikeTimer = 28; // ~1.4 сек — чтобы анимация дожила до урона и чуть после
                    }

                    if (getTicksUntilNextAttack() <= 0) {
                        resetAttackCooldown();
                        mob.doHurtTarget(target);
                        mob.swing(InteractionHand.MAIN_HAND);
                    }
                }
            }
        }

        @Override
        protected double getAttackReachSqr(LivingEntity target) {
            return 4.5D;
        }
    }

    static class BuffaloPanicGoal extends Goal {
        private final BuffaloEntity buffalo;

        public BuffaloPanicGoal(BuffaloEntity b) {
            this.buffalo = b;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override public boolean canUse() { return buffalo.isPanicking(); }

        @Override
        public void tick() {
            LivingEntity last = buffalo.getLastHurtByMob();
            if (last != null) {
                Vec3 away = buffalo.position().subtract(last.position()).normalize().scale(16);
                buffalo.getNavigation().moveTo(
                        buffalo.getX() + away.x,
                        buffalo.getY(),
                        buffalo.getZ() + away.z,
                        1.4D
                );
            }
        }

        @Override public void stop() {
            buffalo.getNavigation().stop();
        }
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 1, this::movementPredicate)); // 1 тик — минимум задержки
        controllers.add(new AnimationController<>(this, "action", 1, state -> PlayState.CONTINUE));
    }

    private PlayState movementPredicate(software.bernie.geckolib.core.animation.AnimationState<BuffaloEntity> state) {
        var controller = state.getController();

        boolean panicking = isPanicking();
        boolean striking = isStriking();

        if (level().isClientSide && tickCount % 10 == 0) {
            System.out.println("[CLIENT] panic=" + panicking + " strike=" + striking + " moving=" + isMoving() +
                    " speed=" + getDeltaMovement().horizontalDistanceSqr());
        }

        if (striking) {
            controller.setAnimation(RawAnimation.begin().then("attack", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        if (panicking) {
            controller.setAnimation(RawAnimation.begin().then("panic_run", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        if (isMoving()) {
            controller.setAnimation(RawAnimation.begin().then("walk", Animation.LoopType.LOOP));
            return PlayState.CONTINUE;
        }

        controller.setAnimation(RawAnimation.begin().then("idle", Animation.LoopType.LOOP));
        return PlayState.CONTINUE;
    }

    public boolean isMoving() {
        double speed = getDeltaMovement().horizontalDistanceSqr();
        return getNavigation().isInProgress() || (speed > 0.0005D && !isPanicking());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean didHurt = super.hurt(source, amount);

        if (didHurt && !level().isClientSide && source.getEntity() instanceof LivingEntity attacker) {
            System.out.println("[HURT] От " + attacker.getName().getString());

            if (isBaby()) {
                setPanicking(true);
                panicTimer = 120;
                setTarget(null);
                getNavigation().stop();
                setDeltaMovement(Vec3.ZERO);
                xxa = zza = 0.0F;
                return didHurt;
            }

            List<BuffaloEntity> nearby = level().getEntitiesOfClass(
                    BuffaloEntity.class,
                    getBoundingBox().inflate(14.0)
            ).stream().filter(b -> b.isAlive() && b != this).toList();

            for (BuffaloEntity b : nearby) {
                if (b.isBaby()) {
                    b.setPanicking(true);
                    b.panicTimer = 120;
                    b.setTarget(null);
                    b.getNavigation().stop();
                    b.setDeltaMovement(Vec3.ZERO);
                    b.xxa = b.zza = 0.0F;
                } else {
                    b.setAttacking(true);
                    b.setTarget(attacker);
                    b.attackResetTimer = 600;
                }
            }

            if (nearby.isEmpty()) {
                setPanicking(true);
                panicTimer = 120;
                setTarget(null);
                getNavigation().stop();
                setDeltaMovement(Vec3.ZERO);
                xxa = zza = 0.0F;
            } else {
                setAttacking(true);
                setTarget(attacker);
                attackResetTimer = 600;
            }
        }

        return didHurt;
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!level().isClientSide) {
            if (isStriking() && --strikeTimer <= 0) {
                setStriking(false);
            }

            if (isPanicking() && --panicTimer <= 0) {
                setPanicking(false);
                getNavigation().stop();
                setDeltaMovement(Vec3.ZERO);
                xxa = zza = 0.0F;
                setTarget(null);
            }

            if (isAttacking() && (--attackResetTimer <= 0 || getTarget() == null || !getTarget().isAlive())) {
                setAttacking(false);
            }

            if (++groupCheckTimer > 80) {
                groupCheckTimer = 0;
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setPanicking(tag.getBoolean("Panicking"));
        setAttacking(tag.getBoolean("Attacking"));
        setStriking(tag.getBoolean("Striking"));
        panicTimer = tag.getInt("PanicTimer");
        attackResetTimer = tag.getInt("AttackReset");
        strikeTimer = tag.getInt("StrikeTimer");
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Panicking", isPanicking());
        tag.putBoolean("Attacking", isAttacking());
        tag.putBoolean("Striking", isStriking());
        tag.putInt("PanicTimer", panicTimer);
        tag.putInt("AttackReset", attackResetTimer);
        tag.putInt("StrikeTimer", strikeTimer);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return ModEntities.BUFFALO.get().create(level);
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.COW_AMBIENT; }
    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.COW_HURT; }
    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.COW_DEATH; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.COW_STEP, 0.15F, 1.0F);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.WHEAT) && !isBaby()) {
            setInLove(player);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    public static boolean checkBuffaloSpawnRules(EntityType<BuffaloEntity> type, LevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        if (level.getBlockState(pos.below()).is(Blocks.AIR)) return false;
        boolean waterFound = false;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int x = -15; x <= 15; x++) {
            for (int z = -15; z <= 15; z++) {
                mutable.set(pos.getX() + x, pos.getY(), pos.getZ() + z);
                if (level.getBlockState(mutable).is(Blocks.WATER)) {
                    waterFound = true;
                    break;
                }
            }
            if (waterFound) break;
        }
        return waterFound && (
                level.getBlockState(pos.below()).is(Blocks.SAND) ||
                        level.getBlockState(pos.below()).is(Blocks.RED_SAND) ||
                        level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK)
        );
    }
}