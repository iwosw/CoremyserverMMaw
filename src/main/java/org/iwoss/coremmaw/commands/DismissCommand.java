package org.iwoss.coremmaw.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.iwoss.coremmaw.init.ItemInit;
import java.util.UUID;

public class DismissCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dismiss").executes(context -> {
            Player player = context.getSource().getPlayerOrException();
            AABB area = new AABB(player.blockPosition()).inflate(15.0);

            ItemStack scroll = player.getMainHandItem();
            boolean hasScroll = scroll.is(ItemInit.OWNERSHIP_SCROLL.get());

            var recruits = player.level().getEntitiesOfClass(AbstractRecruitEntity.class, area,
                    e -> {
                        UUID ownerId = e.getOwnerUUID();
                        return ownerId != null && ownerId.equals(player.getUUID()) && e.getPersistentData().getBoolean("is_serf");
                    });

            int count = 0;
            for (AbstractRecruitEntity recruit : recruits) {
                Villager villager = EntityType.VILLAGER.create(player.level());
                if (villager != null) {
                    villager.moveTo(recruit.getX(), recruit.getY(), recruit.getZ(), recruit.getYRot(), recruit.getXRot());

                    CompoundTag rNbt = recruit.getPersistentData();
                    CompoundTag vNbt = villager.getPersistentData();

                    // 1. RESTORING APPEARANCE(Gender и SkinID)
                    int savedGender = rNbt.getInt("Gender");
                    int savedSkin = rNbt.getInt("SkinID");

                    vNbt.putInt("Gender", savedGender);
                    vNbt.putInt("SkinID", savedSkin);

                    // 2. CLEAR NAME
                    if (recruit.hasCustomName()) {
                        String fullName = recruit.getCustomName().getString();
                        String cleanName = fullName.split(" \\(")[0];
                        cleanName = cleanName.replace("§b♂ ", "").replace("§d♀ ", "");

                        String icon = (savedGender == 0) ? "§b♂ " : "§d♀ ";
                        villager.setCustomName(Component.literal(icon + cleanName));
                        villager.setCustomNameVisible(true);
                    }

                    // 3. RESTORING SCROLL
                    if (rNbt.hasUUID("OriginalScrollID")) {
                        vNbt.putUUID("BoundScrollID", rNbt.getUUID("OriginalScrollID"));
                    }

                    villager.setPersistenceRequired();
                    player.level().addFreshEntity(villager);

                    if (hasScroll) {
                        CompoundTag tag = scroll.getOrCreateTag();
                        tag.putInt("CountRecruits", Math.max(0, tag.getInt("CountRecruits") - 1));
                        tag.putInt("CountMale", tag.getInt("CountMale") + 1);
                    }
                }
                recruit.discard();
                count++;
            }

            int finalCount = count;
            if (finalCount > 0) {
                context.getSource().sendSuccess(() -> Component.literal("§6§l[!] §6Ополченцы (" + finalCount + ") распущены."), true);
            } else {
                context.getSource().sendFailure(Component.literal("§cПоблизости нет ваших ополченцев."));
            }
            return count;
        }));
    }
}