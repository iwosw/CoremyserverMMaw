package org.iwoss.coremmaw.foodspoliagefunction;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.network.chat.Component;
import org.iwoss.coremmaw.Coremmaw;

//@Mod.EventBusSubscriber(modid = Coremmaw.MODID) // i'll be back later
public class FoodSpoilageHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        // in inventory checking once a minute
        if (event.player.level().getGameTime() % 1200 != 0) return;

        Player player = event.player;
        Container inv = player.getInventory();
        long time = player.level().getGameTime();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (SpoilageLogic.isSpoilable(stack)) {
                SpoilageLogic.updateSpoilage(stack, time, 1.0f);
                if (SpoilageLogic.isTotallySpoiled(stack)) {
                    convertToRotten(inv, i, stack);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onContainerInteract(PlayerContainerEvent event) {
        if (event.getEntity().level().isClientSide) return;

        Level level = event.getEntity().level();
        AbstractContainerMenu menu = event.getContainer();
        long time = level.getGameTime();
        float multiplier = CellarLogic.getTimeMultiplier(level, event.getEntity().blockPosition());

        for (Slot slot : menu.slots) {
            ItemStack stack = slot.getItem();
            if (SpoilageLogic.isSpoilable(stack)) {
                // ПРИ ОТКРЫТИИ БОЧКИ: предмет "догоняет" время с учетом подвала
                SpoilageLogic.updateSpoilage(stack, time, multiplier);
                if (SpoilageLogic.isTotallySpoiled(stack)) {
                    convertToRotten(slot.container, slot.getSlotIndex(), stack);
                }
            }
        }
    }

    private static void convertToRotten(Container container, int slot, ItemStack original) {
        ItemStack rotten = new ItemStack(Items.ROTTEN_FLESH, original.getCount());
        rotten.setHoverName(Component.literal("§4Испорченная еда"));
        rotten.getOrCreateTag().putBoolean("IsSpoiled", true);
        container.setItem(slot, rotten);
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Player player = event.getEntity();
        if (player == null || !SpoilageLogic.isSpoilable(stack) || !stack.hasTag()) return;

        if (stack.getTag().contains("EffAge")) {
            long effAge = stack.getTag().getLong("EffAge");
            int maxDays = stack.getTag().getInt("MaxDays");

            // Текущий множитель для прогноза
            float currentMult = (player.containerMenu != null && player.containerMenu != player.inventoryMenu)
                    ? CellarLogic.getTimeMultiplier(player.level(), player.blockPosition()) : 1.0f;

            long maxAgeTicks = (long) maxDays * SpoilageLogic.TICKS_PER_DAY;
            long ticksLeftEff = maxAgeTicks - effAge;

            if (ticksLeftEff > 0) {
                // Сколько реально осталось ПРИ ТЕКУЩЕМ множителе
                long realTicksLeft = (long) (ticksLeftEff / currentMult);

                long totalMins = realTicksLeft / 1200;
                long days = totalMins / 20;
                long mins = totalMins % 20;

                event.getToolTip().add(Component.literal("§7Срок: §f" + days + "д. " + mins + "м."));
            }
        }
    }
}