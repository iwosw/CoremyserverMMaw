package org.iwoss.coremmaw.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import org.iwoss.coremmaw.util.SkinManager;
import org.iwoss.coremmaw.util.VillagerBiologyController;


@OnlyIn(Dist.CLIENT)
public class HumanoidSerfRenderer<T extends LivingEntity> extends LivingEntityRenderer<T, PlayerModel<T>> {

    public HumanoidSerfRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        // initializing
        int gender = 0;
        int skinId = 1;

        // 1. check NBT
        if (entity.getPersistentData().contains("Gender") && entity.getPersistentData().contains("SkinID")) {
            gender = entity.getPersistentData().getInt("Gender");
            skinId = entity.getPersistentData().getInt("SkinID");
        }
        // 2. calculate along UUID
        else if (entity instanceof Villager villager) {
            gender = VillagerBiologyController.getGenderFromUUID(villager);
            skinId = VillagerBiologyController.getSkinFromUUID(villager);
        }

        // insurance
        if (skinId <= 0) skinId = 1;

        return SkinManager.getSkin(gender, skinId);
    }
}