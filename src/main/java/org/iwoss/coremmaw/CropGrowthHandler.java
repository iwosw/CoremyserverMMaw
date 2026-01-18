package org.iwoss.coremmaw;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Coremmaw.MODID)
public class CropGrowthHandler {

    private static boolean canGrow(LevelAccessor world, BlockPos pos, Block block) {
        Holder<Biome> biomeHolder = world.getBiome(pos);
        ResourceLocation biomeKey = biomeHolder.unwrapKey().map(key -> key.location()).orElse(null);
        if (biomeKey == null) return true;

        String biome = biomeKey.getPath().toLowerCase();
        ResourceLocation blockRL = ForgeRegistries.BLOCKS.getKey(block);
        String blockId = (blockRL != null) ? blockRL.toString() : "";

        // 1. ПУСТЫНИ И БЕСПЛОДНЫЕ ЗЕМЛИ (Мертвые зоны)
        // Здесь вообще ничего не растет, кроме кактусов и кустов
        if (biome.contains("desert") || biome.contains("badlands")) {
            return false;
        }

        // 2. ЖАРКИЕ КУЛЬТУРЫ (Арбузы, Какао, Томаты)
        // Разрешено только в Джунглях и Саванне
        if (block == Blocks.MELON_STEM || block == Blocks.ATTACHED_MELON_STEM ||
                block == Blocks.COCOA || blockId.equals("farmersdelight:tomatoes")) {
            return biome.contains("jungle") || biome.contains("savanna");
        }

        // 3. УМЕРЕННЫЕ КУЛЬТУРЫ (Пшеница, Морковь, Картофель, Свекла, Лук, Капуста)
        // Разрешено: Равнины, Леса, Березовые рощи, Луга, Болота
        if (block == Blocks.WHEAT || block == Blocks.CARROTS || block == Blocks.POTATOES ||
                block == Blocks.BEETROOTS || blockId.equals("farmersdelight:onions") ||
                blockId.equals("farmersdelight:cabbages") || block == Blocks.PUMPKIN_STEM ||
                block == Blocks.ATTACHED_PUMPKIN_STEM) {

            return biome.contains("plains") || biome.contains("forest") ||
                    biome.contains("meadow") || biome.contains("swamp") ||
                    biome.contains("river") || biome.contains("cherry_grove");
        }

        // 4. ВЛАГОЛЮБИВЫЕ (Рис из Farmer's Delight)
        // Разрешено: Болота, Реки, Джунгли (кроме океанов и ледяных вод)
        if (blockId.contains("rice")) {
            if (biome.contains("ocean") || biome.contains("frozen")) return false;
            return biome.contains("swamp") || biome.contains("river") ||
                    biome.contains("jungle") || biome.contains("plains");
        }

        // 5. ХОЛОДОСТОЙКИЕ (Ягоды)
        // Разрешено: Тайга, Снежные биомы
        if (block == Blocks.SWEET_BERRY_BUSH) {
            return biome.contains("taiga") || biome.contains("snow") || biome.contains("ice");
        }

        // Если это какое-то другое растение (трава, цветы, деревья), разрешаем рост
        return true;
    }

    @SubscribeEvent
    public static void onCropGrow(BlockEvent.CropGrowEvent.Pre event) {
        if (!canGrow(event.getLevel(), event.getPos(), event.getState().getBlock())) {
            event.setResult(Event.Result.DENY);
            // Шанс 5% на засыхание в неподходящем климате
            if (event.getLevel().getRandom().nextFloat() < 0.05f) {
                event.getLevel().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    @SubscribeEvent
    public static void onBonemeal(BonemealEvent event) {
        if (!canGrow(event.getLevel(), event.getPos(), event.getBlock().getBlock())) {
            event.setCanceled(true);
            Player player = event.getEntity();
            if (player != null && !event.getLevel().isClientSide()) {
                player.displayClientMessage(Component.literal("§cКлимат этого биома не подходит для данной культуры!"), true);
            }
        }
    }
}