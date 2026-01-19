package org.iwoss.coremmaw.foodspoliagefunction;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
public class CellarLogic {

    /*
    calculates the spoilage level
    1.0 = default speed
    0.2 = so good cellar (five times slower)
     */
     public static float getTimeMuLtipplier(Level level, BlockPos pos ) {
         float multiplier = 1.0f;

         //FIRST. check depths (underground ever cold)
        if (pos.getY() < level.getSeaLevel()) {
            multiplier -= 0.2f;

        }

        //SECOND. check surroundings (hide stone: default stone, deep stone and bricks)
        int insulationBlocks = 0;
        //checking area 5x5x5 around the cellar
         for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2,-2,-2), pos.offset(2,2,2))) {
             BlockState state = level.getBlockState(p);
             if (state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.STONE_BRICKS)) {
                 insulationBlocks++;

             }
         }
         //if around too many stone (min 30 in radius 2), give a bonus
         if (insulationBlocks > 35) {
             multiplier -= 0.5f;

         }

         //THIRD. CHeck the light (torches and sun generate heat)
         int light = level.getMaxLocalRawBrightness(pos);
         if (light > 7) {
             multiplier += 0.3f; //on lights spoilage so good

         }
    //Forbid min multiplier to food spoilage
        return Math.max(multiplier, 0.15f);



     }


}
