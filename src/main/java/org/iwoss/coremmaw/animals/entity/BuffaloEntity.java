package org.iwoss.coremmaw.animals.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.iwoss.coremmaw.init.ModEntities;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Comparator;
import java.util.List;

public class BuffaloEntity extends Animal implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // logic herd
    private BuffaloEntity herdLeader;
    private int groupCheckTimer = 0;
    private boolean panicking = false;

    public BuffaloEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        this.setMaxUpStep(1.0F);
        this.rotOffs = 20.0F;
    }

    public boolean isPanicking() {
        return panicking;
    }

    public void setPanicking(boolean value) {
        panicking = value;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D) // Базовая скорость
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.5D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }


    //50/50 code
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        // smart pamic
        this.goalSelector.addGoal(1, new BuffaloSmartPanicGoal(this, 2.0D));

        // follow leader
        this.goalSelector.addGoal(2, new BuffaloFollowLeaderGoal(this));

        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.3D, true));

        this.goalSelector.addGoal(4, new AvoidEntityGoal<>(this, Player.class, 16.0F, 1.4D, 1.6D, (entity) -> {
            return this.isBaby() || this.getHealth() < this.getMaxHealth() * 0.4;
        }));

        this.goalSelector.addGoal(5, new BuffaloAutoBreedGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        // help herd
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers());
    }



    @Override
    public boolean doHurtTarget(Entity target) {
        boolean flag = super.doHurtTarget(target);
        if (flag) {
            this.swing(InteractionHand.MAIN_HAND);
            if (target instanceof LivingEntity livingTarget) {
                double knockbackY = 0.5D;
                double knockbackXZ = 0.8D;
                Vec3 vec31 = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ()).normalize().scale(knockbackXZ);
                livingTarget.setDeltaMovement(livingTarget.getDeltaMovement().x / 2.0D - vec31.x, knockbackY, livingTarget.getDeltaMovement().z / 2.0D - vec31.z);
            }
            this.playSound(SoundEvents.IRON_GOLEM_ATTACK, 1.0F, 1.0F);
        }
        return flag;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            this.yBodyRot = this.yBodyRotO + (this.yHeadRot - this.yBodyRotO) * 0.30F;

            Vec3 vector = this.getDeltaMovement();
            if (vector.horizontalDistanceSqr() > 0.005) {
                float moveAngle = (float) (Mth.atan2(vector.z, vector.x) * (180 / Math.PI)) - 90.0F;
                this.setYRot(this.rotlerp(this.getYRot(), moveAngle, 20.0F));
            }
        }
    }

    private void findHerdLeader() {
        List<? extends BuffaloEntity> list = this.level().getEntitiesOfClass(this.getClass(), this.getBoundingBox().inflate(20.0D));
        if (list.size() > 1) {
            this.herdLeader = list.stream()
                    .filter(b -> b != this && b.isAlive())
                    .min(Comparator.comparingInt(Entity::getId))
                    .orElse(null);
        } else {
            this.herdLeader = null;
        }
    }

    private float rotlerp(float start, float end, float maxStep) {
        float f = Mth.wrapDegrees(end - start);
        if (f > maxStep) f = maxStep;
        if (f < -maxStep) f = -maxStep;
        return start + f;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 4, state -> {

            // 1. ATTACK
            if (this.swingTime > 0) {
                state.getController().setAnimationSpeed(2.8D);
                return state.setAndContinue(RawAnimation.begin().thenPlay("attack"));
            }

            double speed = this.getDeltaMovement().horizontalDistance();

            // 2. RUN / PANIC
            if (this.isPanicking() || speed > 0.15) {
                // Для бега множитель чуть ниже, так как анимация сама по себе короче (0.5с)
                state.getController().setAnimationSpeed(speed * 12.0D);
                return state.setAndContinue(RawAnimation.begin().thenLoop("panic_run"));
            }

            // 3. WALKING (bad synchronisation)
            if (speed > 0.01) {
                double syncSpeed = speed * 28.0D;

                state.getController().setAnimationSpeed(Math.max(1.4D, syncSpeed));
                return state.setAndContinue(RawAnimation.begin().thenLoop("walk"));
            }

            // 4. rest
            state.getController().setAnimationSpeed(1.0D);
            return state.setAndContinue(RawAnimation.begin().thenLoop("idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return ModEntities.BUFFALO.get().create(level);
    }

    // --- internal class ai ---


    static class BuffaloFollowLeaderGoal extends Goal {
        private final BuffaloEntity buffalo;
        private int timeToRecalcPath;

        public BuffaloFollowLeaderGoal(BuffaloEntity buffalo) {
            this.buffalo = buffalo;
        }

        @Override
        public boolean canUse() {
            return buffalo.herdLeader != null && buffalo.herdLeader.isAlive() && buffalo.distanceToSqr(buffalo.herdLeader) > 9.0D;
        }

        @Override
        public void tick() {
            if (--this.timeToRecalcPath <= 0) {
                this.timeToRecalcPath = 10;

                double xOffset = (buffalo.getId() % 3 - 1) * 3.0D;
                double zOffset = ((buffalo.getId() / 3) % 3 - 1) * 3.0D;

                this.buffalo.getNavigation().moveTo(
                        buffalo.herdLeader.getX() + xOffset,
                        buffalo.herdLeader.getY(),
                        buffalo.herdLeader.getZ() + zOffset,
                        1.2D
                );
            }
        }
    }

    // Smart Panic (so bad)
    static class BuffaloSmartPanicGoal extends PanicGoal {
        private final BuffaloEntity buffalo;

        public BuffaloSmartPanicGoal(BuffaloEntity mob, double speed) {
            super(mob, speed);
            this.buffalo = mob;
        }

        @Override
        public void start() {
            super.start();
            this.buffalo.setPanicking(true);
        }

        @Override
        public void stop() {
            super.stop();
            this.buffalo.setPanicking(false);
        }

        @Override
        public boolean canUse() {
            if (super.canUse()) {

                if (buffalo.herdLeader != null && buffalo.herdLeader.isPanicking()) {
                    Vec3 leaderTarget = buffalo.herdLeader.getNavigation().getTargetPos() != null ?
                            Vec3.atBottomCenterOf(buffalo.herdLeader.getNavigation().getTargetPos()) : null;

                    if (leaderTarget != null) {

                        this.posX = leaderTarget.x;
                        this.posY = leaderTarget.y;
                        this.posZ = leaderTarget.z;
                        return true;
                    }
                }
                return true;
            }
            return false;
        }
    }

    static class BuffaloAutoBreedGoal extends Goal {
        private final BuffaloEntity buffalo;
        public BuffaloAutoBreedGoal(BuffaloEntity buffalo, double speed) {
            this.buffalo = buffalo;
        }
        @Override
        public boolean canUse() {
            return this.buffalo.getAge() == 0 && !this.buffalo.isInLove() && this.buffalo.getRandom().nextInt(1000) == 0;
        }
        @Override
        public void start() {
            this.buffalo.setInLove(null);
        }
    }

    // Rules spawn
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
                        level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK));
    }
}