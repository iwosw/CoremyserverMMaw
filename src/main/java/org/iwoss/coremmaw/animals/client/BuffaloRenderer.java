package org.iwoss.coremmaw.animals.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.iwoss.coremmaw.Coremmaw;
import org.iwoss.coremmaw.animals.entity.BuffaloEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

@OnlyIn(Dist.CLIENT)
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

            poseStack.scale(0.5f, 0.5f, 0.5f);
        } else {

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