package org.iwoss.coremmaw.init;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.iwoss.coremmaw.Coremmaw;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemInit {
    // Create a register items, using MODID from main class
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Coremmaw.MODID);

    //extended registry scroll with supporting lore
    public static final RegistryObject<Item> OWNERSHIP_SCROLL = ITEMS.register("ownership_scroll",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)) {

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tolltip, TooltipFlag flag) {
            CompoundTag nbt = stack.getTag();

            // if at scroll have nbt about owner
            if (nbt != null && nbt.contains("LordName")) {
                // Showing current Lord
                tolltip.add(Component.literal("Законный владелец: ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(nbt.getString("LordName")).withStyle(ChatFormatting.GOLD)));

                tolltip.add(Component.literal("Реестр подданных:").withStyle(ChatFormatting.DARK_AQUA));

                //Get data
                int males = nbt.getInt("CountMale");
                int females = nbt.getInt("CountFemales");
                int recruits = nbt.getInt("CountRecruits");

                //list output
                tolltip.add(Component.literal(" ♂ Мужчин: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(males)).withStyle(ChatFormatting.AQUA)));

                tolltip.add(Component.literal(" ♀ Женщин: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(females)).withStyle(ChatFormatting.LIGHT_PURPLE)));

                tolltip.add(Component.literal(" ⚔ Ополченцев: ").withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.valueOf(recruits)).withStyle(ChatFormatting.RED)));

                tolltip.add(Component.literal(" Всего душ: ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(String.valueOf(males + females + recruits)).withStyle(ChatFormatting.WHITE)));

            } else {
                tolltip.add(Component.literal("Пустая грамота.")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                tolltip.add(Component.literal("Используйте на жителе для постановки на учет.")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            }

            super.appendHoverText(stack, level, tolltip, flag);


        }

    });

}