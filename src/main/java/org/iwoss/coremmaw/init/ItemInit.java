package org.iwoss.coremmaw.init;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.iwoss.coremmaw.Coremmaw;
import org.iwoss.coremmaw.items.OwnerShipScrollItem;

public class ItemInit {
    // Base register items
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Coremmaw.MODID);

    // register scroll
    public static final RegistryObject<Item> OWNERSHIP_SCROLL = ITEMS.register("ownership_scroll",
            () -> new OwnerShipScrollItem(new Item.Properties()
                    .stacksTo(1)
                    .rarity(Rarity.EPIC)));

    // register egg
    public static final RegistryObject<Item> BUFFALO_SPAWN_EGG = ITEMS.register("buffalo_spawn_egg",
            () -> new ForgeSpawnEggItem(ModEntities.BUFFALO,
                    0x4b3621,
                    0xf5f5dc,
                    new Item.Properties()));

    // Method for register in main
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}