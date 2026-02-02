package org.iwoss.coremmaw.compat;

import net.minecraftforge.fml.ModList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ModCompat {

    public static final boolean RECRUITS_LOADED = ModList.get().isLoaded("recruits");

   public static void tryMakeOwner(Entity recruit, Player player) {
        if (ModCompat.RECRUITS_LOADED) {
            RecruitCompatHandler.applyOwnership(recruit, player);


        }
   }
}