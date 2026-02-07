// VillagerBiologyHandler.java
package org.iwoss.coremmaw.events;

import net.minecraft.world.entity.npc.Villager;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.iwoss.coremmaw.Coremmaw;
import org.iwoss.coremmaw.util.VillagerBiologyController;

@Mod.EventBusSubscriber(modid = Coremmaw.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class VillagerBiologyHandler {

    @SubscribeEvent
    public static void onVillagerSpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Villager villager && !event.getLevel().isClientSide()) {
            // Только если данных ещё нет — чтобы не перезаписывать при перезагрузке чанка
            if (!villager.getPersistentData().contains("Gender")) {
                System.out.println("[Biology] Applying biology to new villager: " + villager.getName().getString());
                VillagerBiologyController.applyBiology(villager);
            }
        }
    }
}