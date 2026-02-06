package org.iwoss.coremmaw.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.iwoss.coremmaw.Coremmaw;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Coremmaw.MODID);

    // Registry egg
    public static final RegistryObject<Item> BUFFALO_SPAWN_EGG = ITEMS.register("buffalo_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BUFFALO,
                    0x4b3621, // Цвет фона (коричневый)
                    0xf5f5dc, // Цвет крапинок (бежевый)
                    new Item.Properties()));

    public static void register(net.minecraftforge.eventbus.api.IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}