package org.iwoss.coremmaw.foodspoliagefunction;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.network.chat.Component;

public class SpoilageLogic {

    public static final long TICKS_PER_DAY = 24000L;

    /**
     * ОСНОВНОЙ ПРОЦЕССОР ПОРЧИ
     * Этот метод вызывается и для инвентаря, и для бочек.
     * Он высчитывает, сколько времени прошло с последней проверки и применяет множитель.
     */
    public static void updateSpoilage(ItemStack stack, long currentTime, float multiplier) {
        if (!isSpoilable(stack)) return;

        CompoundTag nbt = stack.getOrCreateTag();

        // 1. Если предмет новый - инициализируем
        if (!nbt.contains("LastUpdate")) {
            nbt.putLong("LastUpdate", currentTime);
            nbt.putInt("MaxDays", getMaxDaysForFood(stack));
            nbt.putLong("EffAge", 0L);
            return;
        }

        // 2. Считаем дельту времени (сколько тиков прошло в реальности)
        long lastUpdate = nbt.getLong("LastUpdate");
        long elapsedReal = currentTime - lastUpdate;

        if (elapsedReal <= 0) return;

        // 3. ПРИМЕНЯЕМ МНОЖИТЕЛЬ К ПРОШЕДШЕМУ ВРЕМЕНИ
        // Это и есть магия: если прошло 24000 тиков (день), а множитель 0.1,
        // к эффективному возрасту прибавится только 2400 тиков.
        long elapsedEffective = (long) (elapsedReal * multiplier);

        long currentEffAge = nbt.getLong("EffAge");
        nbt.putLong("EffAge", currentEffAge + elapsedEffective);
        nbt.putLong("LastUpdate", currentTime);
    }

    /**
     * ПРОВЕРКА НА ГНИЛЬ
     */
    public static boolean isTotallySpoiled(ItemStack stack) {
        if (!stack.hasTag() || !stack.getTag().contains("EffAge")) return false;
        long maxAgeTicks = (long) stack.getTag().getInt("MaxDays") * TICKS_PER_DAY;
        return stack.getTag().getLong("EffAge") >= maxAgeTicks;
    }

    /**
     * КАТЕГОРИИ ЕДЫ (Расширенный список)
     */
    public static int getMaxDaysForFood(ItemStack stack) {
        ResourceLocation res = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (res == null) return 7;
        String id = res.getPath().toLowerCase();

        // Очень быстрая порча (2 дня)
        if (id.contains("raw_") || id.contains("meat") || id.contains("fish") ||
                id.contains("berry") || id.contains("berries") || id.contains("egg") ||
                id.contains("seafood") || id.contains("oyster") || id.contains("mussel")) return 2;

        // Быстрая порча (3-4 дня)
        if (id.contains("cake") || id.contains("pie") || id.contains("pastry") || id.contains("cookie")) return 3;
        if (id.contains("apple") || id.contains("fruit") || id.contains("tomato") ||
                id.contains("cabbage") || id.contains("onion") || id.contains("carrot") ||
                id.contains("potato") || id.contains("salad") || id.contains("vegetable")) return 4;

        // Средняя порча (5 дней - FD и готовые блюда)
        if (id.contains("cooked_") || id.contains("stew") || id.contains("soup") ||
                id.contains("pasta") || id.contains("meal") || id.contains("roast") ||
                id.contains("burger") || id.contains("sandwich") || id.contains("bread") ||
                id.contains("baguette") || id.contains("rice_bowl")) return 5;

        // Долгая порча (30 дней)
        if (id.contains("cheese") || id.contains("honey_glazed")) return 30;

        return 7; // По умолчанию
    }

    public static boolean isSpoilable(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() == Items.ROTTEN_FLESH) return false;
        // Не портим "вечную" еду
        if (stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) return false;
        return stack.isEdible();
    }
}