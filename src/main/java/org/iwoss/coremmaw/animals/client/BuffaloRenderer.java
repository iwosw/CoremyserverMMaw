package org.iwoss.coremmaw.animals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.iwoss.coremmaw.Coremmaw;
import org.iwoss.coremmaw.animals.entity.BuffaloEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class BuffaloRenderer extends GeoEntityRenderer<BuffaloEntity> {

    public BuffaloRenderer(EntityRendererProvider.Context renderManager) {

        super(renderManager, new BuffaloModel());

        this.shadowRadius = 0.7f;
    }

    /**
     * This method for redact model
     */
    @Override
    public void render(BuffaloEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {

        if (entity.isBaby()) {
            // Уменьшаем модель в 2 раза для телят
            poseStack.scale(0.5f, 0.5f, 0.5f);
        } else {
            // Можно немного увеличить взрослую особь, если модель кажется маленькой (например, 1.2f)
            poseStack.scale(1.1f, 1.1f, 1.1f);
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    /**
     * if you want change texture dynamic
     */
    @Override
    public ResourceLocation getTextureLocation(BuffaloEntity animatable) {
        return new ResourceLocation(Coremmaw.MODID, "textures/entity/animals/buffalo.png");
    }
}