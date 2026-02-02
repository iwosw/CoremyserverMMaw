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
     * Вызывается, когда любая сущность заходит в мир (спавн, загрузка чанка).
     */
    @SubscribeEvent
    public static void onVillagerSpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Villager villager && !event.getLevel().isClientSide) {
            // Если пол еще не назначен в NBT
            if (!villager.getPersistentData().contains("Gender")) {
                // Вызываем единый контроллер, который всё сделает по UUID
                org.iwoss.coremmaw.util.VillagerBiologyController.applyBiology(villager);
            }
        }
    }
}