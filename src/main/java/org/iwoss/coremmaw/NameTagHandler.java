package org.iwoss.coremmaw;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Coremmaw.MODID)
public class NameTagHandler {
    //Variable responsible for whether or not the nicknames are visible
    //Nicknames are visible by default
    public static boolean hideNames = false;

    // FIRST. Events rendering the Nicknames (Hide Nicknames)
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        //if variable HideNames active and entity is a player
        if (hideNames && event.getEntity() instanceof Player) {
            //Cancel render Nickname
            event.setResult(Event.Result.DENY);


        }
    }
    // SECOND. registry Commands
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());

    }

    public static void register(CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("rpnames")
                //Checking permission: only for admins (level 2 - default for moderators)
                .requires(source -> source.hasPermission(2))

                //Subcommand "off" (off nicknames)
                .then(Commands.literal("off")
                        .executes(context -> {
                            hideNames = true;
                            context.getSource().sendSuccess(() -> Component.literal("Nicknames are now hidden!"), true);
                            return 1;
                        }))
                //Subcommand "on" (on nicknames)
                .then(Commands.literal("on")
                        .executes(context -> {
                            hideNames = false;
                            context.getSource().sendSuccess(() -> Component.literal("Nicknames are now visible!"), true);
                            return 1;
                        }))

        );

    }


}


