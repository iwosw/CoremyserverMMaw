package org.iwoss.coremmaw.animals.client;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.iwoss.coremmaw.Coremmaw;
import org.iwoss.coremmaw.animals.entity.BuffaloEntity;
import software.bernie.geckolib.model.GeoModel;

// inherit geomodel
@OnlyIn(Dist.CLIENT)
public class BuffaloModel extends GeoModel<BuffaloEntity> {

    @Override
    public ResourceLocation getModelResource(BuffaloEntity animatable) {
        // Path to geometry
        return new ResourceLocation(Coremmaw.MODID, "geo/animals/buffalo.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BuffaloEntity animatable) {
        // path to texture
        return new ResourceLocation(Coremmaw.MODID, "textures/entity/animals/buffalo.png");
    }

    @Override
    public ResourceLocation getAnimationResource(BuffaloEntity animatable) {
        // path to animation
        return new ResourceLocation(Coremmaw.MODID, "animations/animals/buffalo.animation.json");
    }
}