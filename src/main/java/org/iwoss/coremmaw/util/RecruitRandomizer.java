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
            // --- so bad profiles ---
            case 0, 1, 2 -> { // plowman
                hp = 4; dmg = -1.5; speed = -0.04;
            }
            case 3, 4 -> { // coward
                hp = -6; speed = 0.08; dmg = -2.0;
            }
            case 5, 6 -> { // old man
                hp = -10; speed = -0.08; dmg = -1.0;
            }
            case 7, 8 -> { // sick
                hp = -8; speed = -0.03; dmg = -1.5;
            }
            case 9, 10 -> { // village fool
                hp = 2; speed = 0.04; dmg = -3.0;
            }
            case 11 -> { // drunkard
                hp = 10; speed = -0.1; dmg = 0.5;
            }
            // --- middle ---
            case 12, 13, 14, 15 -> { // base man
                hp = 0; speed = 0; dmg = 0;
            }
            case 16 -> { // hunter
                hp = -2; speed = 0.03; dmg = 1.0;
            }
            case 17 -> { // blacksmith
                hp = 12; speed = -0.05; dmg = 2.0;
            }
            // --- ELITE ---
            case 18 -> { // former deserter
                hp = 4; speed = 0.02; dmg = 3.5;
            }
            case 19 -> { // veteran
                hp = 20; speed = 0.05; dmg = 5.0;
            }
        }

        // so safe method of calling
        applyAttr(recruit, Attributes.MAX_HEALTH, hp);
        applyAttr(recruit, Attributes.MOVEMENT_SPEED, speed);
        applyAttr(recruit, Attributes.ATTACK_DAMAGE, dmg);

        recruit.setHealth(recruit.getMaxHealth());
    }

    private static void applyAttr(AbstractRecruitEntity e, Object attrObj, double val) {
        if (val == 0) return;

        Attribute attribute;
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