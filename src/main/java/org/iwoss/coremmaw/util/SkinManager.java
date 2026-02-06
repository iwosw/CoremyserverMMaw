package org.iwoss.coremmaw.util;

import net.minecraft.resources.ResourceLocation;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class SkinManager {
    // exact number of your skins
    public static final int MALE_COUNT = 36;
    public static final int FEMALE_COUNT = 15;
    private static final Random RANDOM = new Random();

    public static ResourceLocation getSkin(int gender, int skinId) {
        // 0 - male, 1 - female
        if (gender == 1) {
            // path for female: textures/entity/skins/female/f1.png
            return new ResourceLocation("coremmaw", "textures/entity/skins/female/f" + skinId + ".png");
        } else {
            // path for male: textures/entity/skins/male/1.png
            return new ResourceLocation("coremmaw", "textures/entity/skins/male/" + skinId + ".png");
        }
    }

    public static int getRandomSkinId(int gender) {
        // choose random number 1 до MAX
        return RANDOM.nextInt(gender == 1 ? FEMALE_COUNT : MALE_COUNT) + 1;
    }

    public static boolean canBeFemale(String prof) {
        List<String> blacklist = Arrays.asList("armorer", "toolsmith", "weaponsmith", "mason");
        return !blacklist.contains(prof);
    }

    public static String getRuProf(String profId, int gender) {
        switch (profId) {
            case "farmer": return gender == 1 ? "Крестьянка" : "Пахарь";
            case "fletcher": return "Лучник";
            case "librarian": return gender == 1 ? "Ведунья" : "Книжник";
            case "cleric": return "Священник";
            case "armorer": return "Бронник";
            case "toolsmith": return "Кузнец";
            case "weaponsmith": return "Оружейник";
            case "butcher": return "Мясник";
            case "leatherworker": return "Кожемяка";
            case "mason": return "Каменщик";
            case "shepherd": return gender == 1 ? "Пастушка" : "Пастух";
            case "fisherman": return gender == 1 ? "Рыбачка" : "Рыбак";
            default: return gender == 1 ? "Жительница" : "Житель";
        }
    }
}