package org.iwoss.coremmaw.foodspoliagefunction;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class CellarLogic {

    /*
    calculates the spoilage level
    1.0 = default speed
    0.2 = so good cellar (five times slower)
     */
     public static float getTimeMultiplier(Level level, BlockPos pos ) {
         float multiplier = 1.0f;

         //FIRST. check isolation of sky (underground ever cold)
        if (!level.canSeeSky(pos)) {
            multiplier -= 0.2f;

        }

        //SECOND. check surroundings (hide stone: default stone, deep stone and bricks)
        float biomeTemp = level.getBiome(pos).get().getBaseTemperature();
        if (biomeTemp < 0.5f) {
            multiplier -= 0.15f; // base cold of mountains

        }
        //THIRD. Stone structure (termo isolation)
         int stoneCount = 0;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (level.getBlockState(p).is(BlockTags.BASE_STONE_OVERWORLD)) stoneCount++;

        }
        if (stoneCount > 10) multiplier -= 0.3f;

        //FOURTH. Bonus of barrel
         if (level.getBlockState(pos).is(Blocks.BARREL)) {
             multiplier -= 0.15f; // barrel is so good for your food

         }

        return Math.max(multiplier, 0.15f); // maximum doorstep of conservation

     }


}
