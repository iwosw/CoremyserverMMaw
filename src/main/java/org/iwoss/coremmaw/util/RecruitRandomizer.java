package org.iwoss.coremmaw.util;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import java.util.Random;

public class RecruitRandomizer {
    private static final Random RANDOM = new Random();

    public static void randomize(AbstractRecruitEntity recruit) {
        int profile = RANDOM.nextInt(20);
        double hp = 0, speed = 0, dmg = 0;

        switch (profile) {
            // --- ЛОХОВСКИЕ ПРОФИЛИ ---
            case 0, 1, 2 -> { // Пахарь
                hp = 4; dmg = -1.5; speed = -0.04;
            }
            case 3, 4 -> { // Трус
                hp = -6; speed = 0.08; dmg = -2.0;
            }
            case 5, 6 -> { // Старик
                hp = -10; speed = -0.08; dmg = -1.0;
            }
            case 7, 8 -> { // Больной
                hp = -8; speed = -0.03; dmg = -1.5;
            }
            case 9, 10 -> { // Деревенский дурачок
                hp = 2; speed = 0.04; dmg = -3.0;
            }
            case 11 -> { // Пьяница
                hp = 10; speed = -0.1; dmg = 0.5;
            }
            // --- СРЕДНИЕ ---
            case 12, 13, 14, 15 -> { // Обычный мужик
                hp = 0; speed = 0; dmg = 0;
            }
            case 16 -> { // Охотник
                hp = -2; speed = 0.03; dmg = 1.0;
            }
            case 17 -> { // Кузнец
                hp = 12; speed = -0.05; dmg = 2.0;
            }
            // --- ЭЛИТА ---
            case 18 -> { // Бывший дезертир
                hp = 4; speed = 0.02; dmg = 3.5;
            }
            case 19 -> { // ВЕТЕРАН
                hp = 20; speed = 0.05; dmg = 5.0;
            }
        }

        // Используем Attributes.XXX.get(), если это Holder, или просто Attributes.XXX
        // Ниже самый безопасный способ вызова:
        applyAttr(recruit, Attributes.MAX_HEALTH, hp);
        applyAttr(recruit, Attributes.MOVEMENT_SPEED, speed);
        applyAttr(recruit, Attributes.ATTACK_DAMAGE, dmg);

        recruit.setHealth(recruit.getMaxHealth());
    }

    // Метод принимает Object, чтобы мы сами разобрались, Holder это или Attribute
    private static void applyAttr(AbstractRecruitEntity e, Object attrObj, double val) {
        if (val == 0) return;

        Attribute attribute;
        // Проверка: если нам подсунули Holder (в новых версиях), достаем из него Attribute
        if (attrObj instanceof net.minecraft.core.Holder<?> holder) {
            attribute = (Attribute) holder.value();
        } else {
            attribute = (Attribute) attrObj;
        }

        AttributeInstance inst = e.getAttribute(attribute);
        if (inst != null) {
            inst.setBaseValue(inst.getBaseValue() + val);
        }
    }
}