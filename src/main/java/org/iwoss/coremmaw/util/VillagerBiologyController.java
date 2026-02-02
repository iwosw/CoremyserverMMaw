package org.iwoss.coremmaw.util;

import net.minecraft.world.entity.npc.Villager;
import net.minecraft.network.chat.Component;
import java.util.UUID;

public class VillagerBiologyController {

    public static void applyBiology(Villager villager) {
        // 1. Вычисляем пол строго по UUID (как в рендере!)
        int gender = getGenderFromUUID(villager);

        // 2. Генерируем имя под этот пол
        String name = NameGenerator.getRandomName(gender);

        // 3. Ставим визуал (Имя над головой)
        String icon = (gender == 0) ? "§b♂ " : "§d♀ ";
        villager.setCustomName(Component.literal(icon + name));
        villager.setCustomNameVisible(true);

        // 4. Пишем в NBT (для свитков и прочего)
        villager.getPersistentData().putInt("Gender", gender);

        // Скин тоже вычисляем по UUID, чтобы совпадало с рендером
        villager.getPersistentData().putInt("SkinID", getSkinFromUUID(villager));
    }

    public static int getGenderFromUUID(Villager villager) {
        String prof = villager.getVillagerData().getProfession().toString();
        // Если профессия только мужская — всегда 0
        if (!SkinManager.canBeFemale(prof)) return 0;

        // Иначе берем из UUID (0 или 1)
        return Math.abs(villager.getUUID().hashCode()) % 2;
    }

    public static int getSkinFromUUID(Villager villager) {
        int gender = getGenderFromUUID(villager);
        int maxSkins = (gender == 1) ? SkinManager.FEMALE_COUNT : SkinManager.MALE_COUNT;

        // Используем hashCode для разнообразия
        return (Math.abs(villager.getUUID().hashCode() / 7) % maxSkins) + 1;
    }
}