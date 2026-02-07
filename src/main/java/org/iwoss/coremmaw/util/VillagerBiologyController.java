// VillagerBiologyController.java
package org.iwoss.coremmaw.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.Villager;

import java.util.Random;

public class VillagerBiologyController {

    private static final Random RANDOM = new Random();

    public static void applyBiology(Villager villager) {
        if (villager.level().isClientSide) return;
        applyBiologyInternal(villager);
    }

    public static void applyBiologyClient(Villager villager) {
        applyBiologyInternal(villager);
    }

    private static void applyBiologyInternal(Villager villager) {
        int gender = getGenderFromUUID(villager);

        // имя, иконка, SkinID — без изменений
        String name = NameGenerator.getRandomName(gender);
        String icon = (gender == 0) ? "§b♂ " : "§d♀ ";
        villager.setCustomName(Component.literal(icon + name));
        villager.setCustomNameVisible(true);

        villager.getPersistentData().putInt("Gender", gender);
        villager.getPersistentData().putInt("SkinID", getSkinFromUUID(villager));

        // Грудь только для взрослых женщин
        if (gender == 1 && !villager.isBaby()) {
            RANDOM.setSeed(Math.abs(villager.getUUID().hashCode()) + 0x1337L);

            // Умеренный размер — без экстремальных значений
            float breastSize = 0.50f + RANDOM.nextFloat() * 0.45f; // базовый диапазон 0.50 – 0.95

            // редкие отклонения (опционально, можно убрать совсем)
            float rarity = RANDOM.nextFloat();
            if (rarity < 0.08f) breastSize += 0.15f;      // ~8% чуть больше среднего
            else if (rarity < 0.20f) breastSize -= 0.10f; // ~12% чуть меньше среднего

            breastSize = Math.max(0.48f, Math.min(1.05f, breastSize)); // жёсткие границы

            boolean cleavage = RANDOM.nextFloat() < 0.65f;

            float offsetX = RANDOM.nextFloat() * 0.40f - 0.20f;
            float offsetY = RANDOM.nextFloat() * 0.50f - 0.25f;
            float offsetZ = RANDOM.nextFloat() * 0.30f - 0.15f;

            villager.getPersistentData().putFloat("BreastSize", breastSize);
            villager.getPersistentData().putBoolean("Cleavage", cleavage);
            villager.getPersistentData().putFloat("BreastOffsetX", offsetX);
            villager.getPersistentData().putFloat("BreastOffsetY", offsetY);
            villager.getPersistentData().putFloat("BreastOffsetZ", offsetZ);

            // отладка
            System.out.println("[Biology] " + villager.getName().getString() +
                    " → size=" + String.format("%.3f", breastSize) +
                    ", cleavage=" + cleavage);
        }

        villager.refreshDimensions();
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