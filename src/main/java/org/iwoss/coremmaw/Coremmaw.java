package org.iwoss.coremmaw;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.iwoss.coremmaw.commands.DismissCommand;
import org.iwoss.coremmaw.commands.LevyCommand;
import org.iwoss.coremmaw.init.ItemInit;
import org.iwoss.coremmaw.init.ModItems;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Coremmaw.MODID)
public class Coremmaw {
    // Внутри класса Coremmaw или отдельного слушателя событий
    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        LevyCommand.register(event.getDispatcher());

        DismissCommand.register(event.getDispatcher());
    }

    // Define mod id in a common place for everything to reference
    public static final String MODID = "coremmaw";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "coremmaw" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "coremmaw" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "coremmaw" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a new Block with the id "coremmaw:example_block", combining the namespace and path
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    // Creates a new BlockItem with the id "coremmaw:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));


    public Coremmaw() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        ModItems.ITEMS.register(modEventBus);

        // Твои новые предметы
        ItemInit.ITEMS.register(modEventBus);

        org.iwoss.coremmaw.init.ModEntities.ENTITY_TYPES.register(modEventBus);

        // Твои существующие регистрации (блоки, табы и т.д.)
        // Оставь их по одному разу!
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus); // Если в этом ITEMS еще остались предметы
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);

        // Регистрация твоего обработчика событий присяги (если не через аннотацию)
        // MinecraftForge.EVENT_BUS.register(new SerfdomEventHandler());

        modEventBus.addListener(this::addCreative);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // ВАЖНО: Удали строку с EXAMPLE_BLOCK_ITEM, если она там была.
        // Вместо неё добавь свою грамоту:
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ItemInit.OWNERSHIP_SCROLL.get());
        }

        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.BUFFALO_SPAWN_EGG);
        }

    }


    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEventBusEvents {

        // 1. Регистрация атрибутов (Жизни, Атака)
        @SubscribeEvent
        public static void entityAttributeEvent(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
            event.put(org.iwoss.coremmaw.init.ModEntities.BUFFALO.get(),
                    org.iwoss.coremmaw.animals.entity.BuffaloEntity.createAttributes().build());
        }

        // 2. Регистрация правил спавна (Где и как часто)
        @SubscribeEvent
        public static void registerSpawnPlacements(net.minecraftforge.event.entity.SpawnPlacementRegisterEvent event) {
            event.register(
                    org.iwoss.coremmaw.init.ModEntities.BUFFALO.get(),
                    net.minecraft.world.entity.SpawnPlacements.Type.ON_GROUND,
                    net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    org.iwoss.coremmaw.animals.entity.BuffaloEntity::checkBuffaloSpawnRules, // Ссылка на наш метод с водой
                    net.minecraftforge.event.entity.SpawnPlacementRegisterEvent.Operation.REPLACE
            );
        }
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
        }

        @SubscribeEvent
        public static void registerRenderers(net.minecraftforge.client.event.EntityRenderersEvent.RegisterRenderers event) {
            // Регистрация для жителей (явно указываем тип Villager)
            event.registerEntityRenderer(net.minecraft.world.entity.EntityType.VILLAGER,
                    context -> new org.iwoss.coremmaw.render.HumanoidSerfRenderer<>(context));

            event.registerEntityRenderer(org.iwoss.coremmaw.init.ModEntities.BUFFALO.get(),
                    org.iwoss.coremmaw.animals.client.BuffaloRenderer::new);


            // Регистрация для рекрутов
            net.minecraft.world.entity.EntityType<?> recruitType = net.minecraft.world.entity.EntityType.byString("recruits:recruit").orElse(null);
            if (recruitType != null) {
                // Кастуем тип для корректной регистрации
                event.registerEntityRenderer((net.minecraft.world.entity.EntityType) recruitType,
                        context -> new org.iwoss.coremmaw.render.HumanoidSerfRenderer<>(context));

            }
        }
    }




}

