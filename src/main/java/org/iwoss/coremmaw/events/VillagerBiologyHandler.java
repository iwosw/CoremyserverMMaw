package org.iwoss.coremmaw.events;

import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.iwoss.coremmaw.Coremmaw;
import org.iwoss.coremmaw.util.VillagerBiologyController;

@Mod.EventBusSubscriber(modid = Coremmaw.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerBiologyHandler {

    /**
     * called when all entity join to world
     */
    @SubscribeEvent
    public static void onVillagerSpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Villager villager && !event.getLevel().isClientSide) {
            if (!villager.getPersistentData().contains("Gender")) {
                org.iwoss.coremmaw.util.VillagerBiologyController.applyBiology(villager);
            }
        }
    }
}