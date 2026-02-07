package org.iwoss.coremmaw.items;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OwnerShipScrollItem extends Item {

    public OwnerShipScrollItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tolltip, TooltipFlag flag) {
        CompoundTag nbt = stack.getTag();

        if (nbt != null && nbt.contains("LordName")) {
            // display current lord
            tolltip.add(Component.literal("Законный владелец: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(nbt.getString("LordName")).withStyle(ChatFormatting.GOLD)));

            tolltip.add(Component.literal("Реестр подданных:").withStyle(ChatFormatting.DARK_AQUA));

            // get data of villagers
            int males = nbt.getInt("CountMale");
            int females = nbt.getInt("CountFemales");
            int recruits = nbt.getInt("CountRecruits");

            // Output list
            tolltip.add(Component.literal(" ♂ Мужчин: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(males)).withStyle(ChatFormatting.AQUA)));

            tolltip.add(Component.literal(" ♀ Женщин: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(females)).withStyle(ChatFormatting.LIGHT_PURPLE)));

            tolltip.add(Component.literal(" ⚔ Ополченцев: ").withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(recruits)).withStyle(ChatFormatting.RED)));

            tolltip.add(Component.literal(" Всего душ: ").withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(String.valueOf(males + females + recruits)).withStyle(ChatFormatting.WHITE)));

        } else {
            // Text for empty scroll
            tolltip.add(Component.literal("Пустая грамота.")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            tolltip.add(Component.literal("Используйте на жителе для постановки на учет.")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        super.appendHoverText(stack, level, tolltip, flag);
    }
}