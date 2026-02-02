package org.iwoss.coremmaw.MobRealisticSpawn;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.iwoss.coremmaw.Coremmaw;

import java.util.Random;



@Mod.EventBusSubscriber(modid = Coremmaw.MODID)
public class SpawnLogic {

    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;

    //Interval 12000 ticks = 10 minutes
    private static final int SPAWN_INTERVAL = 12000;

    @SubscribeEvent
    public static void onServerTick(TickEvent.LevelTickEvent event) {
        // Working only on server and only once at 10 minutes
        if (event.phase == TickEvent.Phase.END && event.level instanceof ServerLevel level) {
            tickCounter++;
            if (tickCounter >= SPAWN_INTERVAL) {
                tickCounter = 0;
                runRealisticSpawnCycle(level);
            }
        }
    }

    private static void runRealisticSpawnCycle(ServerLevel level) {
        // We go through the players to spawn mobs only where they are
        level.players().forEach(player -> {
            BlockPos playerPos = player.blockPosition();

            // Пытаемся заспавнить несколько групп вокруг каждого игрока
            for (int i = 0; i < 3; i++) { // 3 attempts for spawn in cycle
                attemptSpawnInArea(level, playerPos);
            }
        });
    }


private static void attemptSpawnInArea(ServerLevel level, BlockPos center) {
    // Choosing a random spawn point in radius from 24 to 64 blocks from the player
    int range = 64;
    int minRange = 24;
    int x = center.getX() + RANDOM.nextInt(range * 2) - range;
    int z = center.getZ() + RANDOM.nextInt(range * 2) - range;

    //Cutting not far away spawn
    if (center.distSqr(new BlockPos(x, center.getY(), z)) < minRange * minRange) return;
    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    BlockPos spawnPos = new BlockPos(x, y, z);

    //---WRITING FROM CONFIG---
    //for example: logic forests deer
    if (isValidForSpawn(level, spawnPos)) {
        //in future cycle for list at config
        spawnPack(level, EntityType.COW, spawnPos, 4, 8); //spawn herd

        }

    }

private static boolean isValidForSpawn(ServerLevel level, BlockPos pos) {
        // 1. Check surface slope
        int hLeft = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() - 1, pos.getZ());
        int hRight = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX() + 1, pos.getZ());
        if (Math.abs(hLeft - hRight) > 2) return false; // Слишком круто для обычных животных

        // 2. Check block
        if (level.getBlockState(pos.below()).is(Blocks.WATER) ||
                level.getBlockState(pos.below()).is(Blocks.AIR)) return false;

        // 3. Проверка lights
        return true;
    }

    private static void spawnPack(ServerLevel level, EntityType<?> type, BlockPos pos, int min, int max) {
        int count = min + RANDOM.nextInt(max - min + 1);

        for (int i = 0; i < count; i++) {

            BlockPos memberPos = pos.offset(RANDOM.nextInt(4) - 2, 0, RANDOM.nextInt(4) - 2);

            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, memberPos.getX(), memberPos.getZ());

            Mob entity = (Mob) type.create(level);
            if (entity != null) {
                entity.moveTo(memberPos.getX() + 0.5, y, memberPos.getZ() + 0.5, RANDOM.nextFloat() * 360, 0);
                entity.finalizeSpawn(level, level.getCurrentDifficultyAt(memberPos), MobSpawnType.NATURAL, null, null);
                level.addFreshEntity(entity);
            }
        }
    }

}