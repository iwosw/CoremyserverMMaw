package org.iwoss.coremmaw.compat;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.RecruitEntity;
import com.talhanation.recruits.world.RecruitsGroup;
import com.talhanation.recruits.world.RecruitsGroupsSaveData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class RecruitCompatHandler {

    public static void applyOwnership(Entity entity, Player player) {
        if (entity instanceof AbstractRecruitEntity recruit && player.level() instanceof ServerLevel serverLevel) {

            // ==========================================
            // ШАГ 1: Base binding
            // ==========================================
            recruit.setOwnerUUID(Optional.of(player.getUUID()));
            recruit.setMoral(100.0F);
            recruit.resetPaymentTimer();

            try {
                Field ownedField = findField(AbstractRecruitEntity.class, "OWNED");
                if (ownedField != null) {
                    EntityDataAccessor<Boolean> ownedAccessor = (EntityDataAccessor<Boolean>) ownedField.get(null);
                    recruit.getEntityData().set(ownedAccessor, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (recruit instanceof RecruitEntity re) re.setEquipment();


            // ==========================================
            // ШАГ 2: Reflection
            // ==========================================
            try {
                // data of group
                RecruitsGroupsSaveData saveData = RecruitsGroupsSaveData.get(serverLevel);
                List<RecruitsGroup> allGroups = saveData.getAllGroups(); // Этот метод должен быть public

                RecruitsGroup playerGroup = null;

                for (RecruitsGroup g : allGroups) {
                    UUID groupOwnerID = getFieldValue(g, UUID.class, "playerUUID", "player", "owner");
                    if (groupOwnerID != null && groupOwnerID.equals(player.getUUID())) {
                        playerGroup = g;
                        break;
                    }
                }

                if (playerGroup == null) {

                    Constructor<RecruitsGroup> constructor = RecruitsGroup.class.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    playerGroup = constructor.newInstance();

                    UUID newGroupUUID = UUID.randomUUID();


                    setFieldValue(playerGroup, "playerUUID", player.getUUID()); // ID владельца
                    setFieldValue(playerGroup, "uuid", newGroupUUID);           // ID группы
                    setFieldValue(playerGroup, "name", player.getScoreboardName() + " Army"); // Имя

                    setFieldValue(playerGroup, "units", new ArrayList<UUID>());

                    allGroups.add(playerGroup);
                }


                List<UUID> unitsList = getFieldValue(playerGroup, List.class, "units", "recruitUUIDs", "members");
                if (unitsList != null && !unitsList.contains(recruit.getUUID())) {
                    unitsList.add(recruit.getUUID());
                }


                UUID groupUUID = getFieldValue(playerGroup, UUID.class, "uuid", "groupID", "id");
                if (groupUUID != null) {
                    recruit.setGroupUUID(groupUUID);
                    recruit.needsGroupUpdate = true;
                }


                saveData.setDirty();

            } catch (Exception e) {
                System.err.println("CoreMMAW: Ошибка при регистрации рекрута в группе через Reflection.");
                e.printStackTrace();
            }


            recruit.getPersistentData().putInt("FollowState", 1);
            recruit.getPersistentData().putBoolean("isOwned", true);
        }
    }

    // ---  and other methods  ---


    private static void setFieldValue(Object instance, String fieldName, Object value) {
        try {
            Field field = findField(instance.getClass(), fieldName);
            if (field != null) field.set(instance, value);
        } catch (Exception ignored) {}
    }


    private static <T> T getFieldValue(Object instance, Class<T> type, String... possibleNames) {
        for (String name : possibleNames) {
            try {
                Field field = findField(instance.getClass(), name);
                if (field != null) {
                    Object val = field.get(instance);
                    if (type.isInstance(val)) return type.cast(val);
                }
            } catch (Exception ignored) {}
        }

        try {
            for (Field field : instance.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getType().equals(type)) {
                    return (T) field.get(instance);
                }
            }
        } catch (Exception ignored) {}
        return null;
    }


    private static Field findField(Class<?> clazz, String name) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {

            if (clazz.getSuperclass() != null) return findField(clazz.getSuperclass(), name);
        }
        return null;
    }
}