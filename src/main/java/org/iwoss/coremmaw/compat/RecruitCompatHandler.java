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
            // ШАГ 1: Базовая привязка (Владелец и Инвентарь)
            // ==========================================
            recruit.setOwnerUUID(Optional.of(player.getUUID()));
            recruit.setMoral(100.0F);
            recruit.resetPaymentTimer();

            // Взлом поля OWNED для работы ПКМ
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
            // ШАГ 2: Взлом Групп (для работы GUI команд)
            // ==========================================
            try {
                // Достаем глобальные данные о группах
                RecruitsGroupsSaveData saveData = RecruitsGroupsSaveData.get(serverLevel);
                List<RecruitsGroup> allGroups = saveData.getAllGroups(); // Этот метод должен быть public

                RecruitsGroup playerGroup = null;

                // Ищем группу игрока вручную через рефлексию (так как метод getPlayerUUID может не существовать)
                for (RecruitsGroup g : allGroups) {
                    UUID groupOwnerID = getFieldValue(g, UUID.class, "playerUUID", "player", "owner");
                    if (groupOwnerID != null && groupOwnerID.equals(player.getUUID())) {
                        playerGroup = g;
                        break;
                    }
                }

                // Если группы нет — создаем её с нуля через рефлексию
                if (playerGroup == null) {
                    // Используем пустой конструктор (он есть всегда для NBT)
                    Constructor<RecruitsGroup> constructor = RecruitsGroup.class.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    playerGroup = constructor.newInstance();

                    UUID newGroupUUID = UUID.randomUUID();

                    // Заполняем приватные поля новой группы
                    setFieldValue(playerGroup, "playerUUID", player.getUUID()); // ID владельца
                    setFieldValue(playerGroup, "uuid", newGroupUUID);           // ID группы
                    setFieldValue(playerGroup, "name", player.getScoreboardName() + " Army"); // Имя

                    // Инициализируем список юнитов, если он null
                    setFieldValue(playerGroup, "units", new ArrayList<UUID>());

                    // Добавляем новую группу в общий список
                    allGroups.add(playerGroup);
                }

                // Добавляем рекрута в список юнитов группы
                // Ищем поле списка (обычно называется units, members или recruitUUIDs)
                List<UUID> unitsList = getFieldValue(playerGroup, List.class, "units", "recruitUUIDs", "members");
                if (unitsList != null && !unitsList.contains(recruit.getUUID())) {
                    unitsList.add(recruit.getUUID());
                }

                // Привязываем рекрута к ID группы
                UUID groupUUID = getFieldValue(playerGroup, UUID.class, "uuid", "groupID", "id");
                if (groupUUID != null) {
                    recruit.setGroupUUID(groupUUID);
                    recruit.needsGroupUpdate = true;
                }

                // Сохраняем изменения
                saveData.setDirty();

            } catch (Exception e) {
                System.err.println("CoreMMAW: Ошибка при регистрации рекрута в группе через Reflection.");
                e.printStackTrace();
            }

            // ШАГ 3: Финальные штрихи
            recruit.getPersistentData().putInt("FollowState", 1);
            recruit.getPersistentData().putBoolean("isOwned", true);
        }
    }

    // --- Вспомогательные методы для чистоты кода ---

    // Установить значение в приватное поле
    private static void setFieldValue(Object instance, String fieldName, Object value) {
        try {
            Field field = findField(instance.getClass(), fieldName);
            if (field != null) field.set(instance, value);
        } catch (Exception ignored) {}
    }

    // Получить значение из приватного поля (с перебором вариантов имен)
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
        // Если по имени не нашли, ищем первое поле подходящего типа (крайняя мера)
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

    // Найти поле и открыть доступ
    private static Field findField(Class<?> clazz, String name) {
        try {
            Field f = clazz.getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            // Иногда поля спрятаны в суперклассе
            if (clazz.getSuperclass() != null) return findField(clazz.getSuperclass(), name);
        }
        return null;
    }
}