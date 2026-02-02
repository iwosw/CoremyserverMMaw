package org.iwoss.coremmaw.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.iwoss.coremmaw.Coremmaw;
import org.iwoss.coremmaw.init.ItemInit;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.iwoss.coremmaw.util.NameGenerator;


import java.util.UUID;


@Mod.EventBusSubscriber(modid = Coremmaw.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SerfdomEventHandler {

    @SubscribeEvent
    public static void onVillagerInteract(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        Player player = event.getEntity();
        ItemStack stack = player.getItemInHand(event.getHand());

        // Checking that a click for villager
        if (!(target instanceof Villager villager)) return;

        CompoundTag villagerNbt = villager.getPersistentData();


        // LOGIC 1: Use scroll on villager
        if (stack.is(ItemInit.OWNERSHIP_SCROLL.get())) {
            CompoundTag itemNbt = stack.getOrCreateTag();

            // if scroll absolutely new  — generate her unique ID
            if (!itemNbt.contains("ScrollID")) {
                itemNbt.putUUID("ScrollID", UUID.randomUUID());
                itemNbt.putInt("SerfCount", 0);
            }

            // Enter current owner how lord
            itemNbt.putUUID("LordUUID", player.getUUID());
            itemNbt.putString("LordName", player.getName().getString());

            UUID scrollID = itemNbt.getUUID("ScrollID");

            //   linking villager to ID this scroll
            if (!villagerNbt.contains("BoundScrollID") || !villagerNbt.getUUID("BoundScrollID").equals(scrollID)) {
                villagerNbt.putUUID("BoundScrollID", scrollID);


                int gender = villagerNbt.contains("Gender") ? villagerNbt.getInt("Gender") :
                        org.iwoss.coremmaw.util.VillagerBiologyController.getGenderFromUUID(villager);


                if (gender == 0) {
                    itemNbt.putInt("CountMale", itemNbt.getInt("CountMale") + 1);
                } else {
                    itemNbt.putInt("CountFemale", itemNbt.getInt("CountFemale") + 1);
                }

                player.displayClientMessage(Component.literal("§aЖитель внесен в реестр!"), true);


                String vName = villagerNbt.getString("VillagerName");
                if (vName.isEmpty()) vName = "крестьянин";



                player.displayClientMessage(Component.literal("§aЖитель присягнул вашей грамоте!"), true);
            } else {
                player.displayClientMessage(Component.literal("§eЭтот житель уже числится в данной грамоте."), true);
            }

            event.setCanceled(true); // Close vanilla trading by click scroll
            return;
        }

        // LOGIC 2: Checking permissions by default trading
        if (villagerNbt.contains("BoundScrollID")) {
            UUID boundID = villagerNbt.getUUID("BoundScrollID");


            boolean hasRightScroll = false;
            for (ItemStack item : player.getInventory().items) {
                if (item.is(ItemInit.OWNERSHIP_SCROLL.get()) && item.hasTag()) {
                    if (item.getTag().getUUID("ScrollID").equals(boundID)) {
                        hasRightScroll = true;
                        break;
                    }
                }
            }

            // Check second hand
            if (!hasRightScroll) {
                player.displayClientMessage(Component.literal("§cЭтот человек принадлежит владельцу грамоты №" + boundID.toString().substring(0, 5)), true);
                event.setCanceled(true);
            }
        }
    }

    private static void updateScrollOnDeath(Player player, UUID id) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ItemInit.OWNERSHIP_SCROLL.get()) && stack.hasTag()) {
                CompoundTag tag = stack.getTag();
                if (tag.getUUID("ScrollID").equals(id)) {
                    int count = tag.getInt("SerfCount");
                    if (count > 0) {
                        tag.putInt("SerfCount", count -1);
                        player.displayClientMessage(Component.literal("§6Один из ваших крестьян скончался. В реестре осталось: " + (count - 1)), false);

                    }
                }
            }
        }
    }
}