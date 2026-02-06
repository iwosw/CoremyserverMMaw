package org.iwoss.coremmaw.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.iwoss.coremmaw.Coremmaw;
import org.iwoss.coremmaw.animals.entity.BuffaloEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Coremmaw.MODID);

    // Registry Buffalo
    // ModEntities.java
    public static final RegistryObject<EntityType<BuffaloEntity>> BUFFALO =
            ENTITY_TYPES.register("buffalo",
                    () -> EntityType.Builder.of(BuffaloEntity::new, MobCategory.CREATURE) // CREATURE = спавнится редко, но кучно
                            .sized(1.6f, 1.95f) // Увеличил хитбокс, чтобы соответствовал "стене"
                            .clientTrackingRange(10)
                            .build("buffalo"));
}