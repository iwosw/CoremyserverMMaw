package org.iwoss.coremmaw.events;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.iwoss.coremmaw.Coremmaw;
import org.iwoss.coremmaw.util.NameGenerator;
import org.iwoss.coremmaw.util.SkinManager;


//this method bad working
@Mod.EventBusSubscriber(modid = Coremmaw.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RecruitBiologyHandler {

    @SubscribeEvent
    public static void onRecruitSpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof AbstractRecruitEntity recruit && !event.getLevel().isClientSide) {


            if (!recruit.getPersistentData().contains("SkinID")) {


                int gender = 0;
                int skinId = SkinManager.getRandomSkinId(gender);


                recruit.getPersistentData().putInt("Gender", gender);
                recruit.getPersistentData().putInt("SkinID", skinId);
                recruit.getPersistentData().putBoolean("is_serf", true);


                if (!recruit.hasCustomName()) {
                    String name = NameGenerator.getRandomName(gender);

                    String icon = "§b♂ ";
                    recruit.setCustomName(Component.literal(icon + name));
                    recruit.setCustomNameVisible(true);
                }
            }
        }
    }
}