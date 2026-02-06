package org.iwoss.coremmaw.util;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.network.chat.Component;
import java.util.UUID;

public class VillagerBiologyController {

    public static void applyBiology(Villager villager) {
        int gender = getGenderFromUUID(villager);

        String name = NameGenerator.getRandomName(gender);

        String icon = (gender == 0) ? "§b♂ " : "§d♀ ";
        villager.setCustomName(Component.literal(icon + name));
        villager.setCustomNameVisible(true);

        villager.getPersistentData().putInt("Gender", gender);

        villager.getPersistentData().putInt("SkinID", getSkinFromUUID(villager));
    }

    public static int getGenderFromUUID(Villager villager) {
        String prof = villager.getVillagerData().getProfession().toString();
        if (!SkinManager.canBeFemale(prof)) return 0;
        return Math.abs(villager.getUUID().hashCode()) % 2;
    }

    public static int getSkinFromUUID(Villager villager) {
        int gender = getGenderFromUUID(villager);
        int maxSkins = (gender == 1) ? SkinManager.FEMALE_COUNT : SkinManager.MALE_COUNT;

        return (Math.abs(villager.getUUID().hashCode() / 7) % maxSkins) + 1;
    }
}