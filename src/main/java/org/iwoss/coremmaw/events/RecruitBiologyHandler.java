package org.iwoss.coremmaw.events;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.iwoss.coremmaw.Coremmaw;
import org.iwoss.coremmaw.util.NameGenerator;
import org.iwoss.coremmaw.util.SkinManager;

@Mod.EventBusSubscriber(modid = Coremmaw.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RecruitBiologyHandler {

    @SubscribeEvent
    public static void onRecruitSpawn(EntityJoinLevelEvent event) {
        // Проверяем, что это рекрут и мы на сервере
        if (event.getEntity() instanceof AbstractRecruitEntity recruit && !event.getLevel().isClientSide) {

            // Если у него еще нет данных о скине
            if (!recruit.getPersistentData().contains("SkinID")) {

                // СТРОГО мужской пол
                int gender = 0;
                int skinId = SkinManager.getRandomSkinId(gender);

                // Записываем в NBT
                recruit.getPersistentData().putInt("Gender", gender);
                recruit.getPersistentData().putInt("SkinID", skinId);
                recruit.getPersistentData().putBoolean("is_serf", true);

                // Даем мужское имя, если его нет
                if (!recruit.hasCustomName()) {
                    String name = NameGenerator.getRandomName(gender);
                    // Голубой значок Марса для мужиков
                    String icon = "§b♂ ";
                    recruit.setCustomName(Component.literal(icon + name));
                    recruit.setCustomNameVisible(true);
                }
            }
        }
    }
}