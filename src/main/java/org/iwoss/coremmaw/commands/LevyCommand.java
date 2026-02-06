package org.iwoss.coremmaw.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.iwoss.coremmaw.init.ItemInit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LevyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("levy")
                .then(Commands.argument("percent", IntegerArgumentType.integer(1, 100))
                        .executes(context -> levy(context.getSource(), IntegerArgumentType.getInteger(context, "percent")))
                )
        );
    }

    private static int levy(CommandSourceStack source, int percent) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack scroll = findActiveScroll(player);

        if (scroll.isEmpty()) {
            source.sendFailure(Component.literal("§cУ вас в руках должна быть активная грамота!"));
            return 0;
        }

        UUID scrollID = scroll.getTag().getUUID("ScrollID");

        // seek all your villagers in area 64 blocks
        AABB area = new AABB(player.getX() - 64, player.getY() - 32, player.getZ() - 64,
                player.getX() + 64, player.getY() + 32, player.getZ() + 64);

        List<Villager> mySerfs = player.level().getEntitiesOfClass(Villager.class, area).stream()
                .filter(v -> v.getPersistentData().contains("BoundScrollID") &&
                        v.getPersistentData().getUUID("BoundScrollID").equals(scrollID) &&
                        v.getPersistentData().getInt("Gender") == 0) // Фильтруем: только твои и только мужчины
                .collect(Collectors.toList());

        if (mySerfs.isEmpty()) {
            source.sendFailure(Component.literal("§cПоблизости не найдено ваших крепостных мужского пола."));
            return 0;
        }

        int toConscript = Math.max(1, (mySerfs.size() * percent) / 100);
        int actual = 0;

        for (int i = 0; i < toConscript; i++) {
            Villager villager = mySerfs.get(i);

            // spawn recruit
            EntityType<?> recruitType = EntityType.byString("recruits:recruit").orElse(null);
            if (recruitType != null) {
                Entity recruit = recruitType.create(player.level());
                if (recruit instanceof Mob mob) {
                    // 1. POSITION AND INITIALIZATION
                    mob.moveTo(villager.getX(), villager.getY(), villager.getZ(), villager.getYRot(), villager.getXRot());
                    mob.finalizeSpawn(player.serverLevel(), player.level().getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);

                    CompoundTag vNbt = villager.getPersistentData();

                    // 1. Have gender (Gender)
                    int gender = vNbt.contains("Gender") ? vNbt.getInt("Gender") :
                            org.iwoss.coremmaw.util.VillagerBiologyController.getGenderFromUUID(villager);

                    // 2. Have skin (SkinID)
                    int skinId = vNbt.contains("SkinID") ? vNbt.getInt("SkinID") :
                            org.iwoss.coremmaw.util.VillagerBiologyController.getSkinFromUUID(villager);

                    // 3. data for recruit
                    mob.getPersistentData().putInt("Gender", gender);
                    mob.getPersistentData().putInt("SkinID", skinId);
                    mob.getPersistentData().putBoolean("is_serf", true);

                    // save id scroll
                    if (vNbt.contains("BoundScrollID")) {
                        mob.getPersistentData().putUUID("OriginalScrollID", vNbt.getUUID("BoundScrollID"));
                    }

                    // 4. CREATE OWNER AND RANDOMIZE STATS
                    org.iwoss.coremmaw.compat.ModCompat.tryMakeOwner(mob, player);

                    if (mob instanceof com.talhanation.recruits.entities.AbstractRecruitEntity recruitEntity) {
                        org.iwoss.coremmaw.util.RecruitRandomizer.randomize(recruitEntity);
                    }

                    // 5. NAME AND PROFESSION
                    String profId = villager.getVillagerData().getProfession().toString().replace("minecraft:", "");
                    String profName = org.iwoss.coremmaw.util.SkinManager.getRuProf(profId, gender);

                    if (villager.hasCustomName()) {
                        mob.setCustomName(Component.literal(villager.getCustomName().getString() + " (" + profName + ")"));
                    } else {
                        mob.setCustomName(Component.literal(profName));
                    }

                    mob.setCustomNameVisible(true);
                    mob.setPersistenceRequired();

                    // 6. SPAWN
                    player.level().addFreshEntity(mob);

                    villager.discard();
                    actual++;
                }
            }
        }


        // updating counter in scroll
        int males = scroll.getTag().getInt("CountMale");
        int recruits = scroll.getTag().getInt("CountRecruits");
        scroll.getTag().putInt("CountMale", Math.max(0, males - actual));
        scroll.getTag().putInt("CountRecruits", recruits + actual);


        final int finalActual = actual;
        source.sendSuccess(() -> Component.literal("§6Призвано ополчение: §f" + finalActual + " §6чел."), true);
        return actual;
    }

    private static ItemStack findActiveScroll(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (stack.is(ItemInit.OWNERSHIP_SCROLL.get()) && stack.hasTag() && stack.getTag().contains("ScrollID")) return stack;
        return ItemStack.EMPTY;
    }
}