package org.iwoss.coremmaw;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Coremmaw.MODID)
public class RPNameHandler {

    //FIRST. command for setting nickname
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("setname")
        //greedyString allows enter name with spaces, for example "Ivan Grozniy"
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(context -> {
                            //Getting text which was entered player
                            String newName = StringArgumentType.getString(context, "name");
                            //hide player which is typing command
                            Player player = context.getSource().getPlayerOrException();

                            /* Saving name in Minecraft with the help of NBT about the player
                            This data saving always*/
                            player.getPersistentData().putString("rpname", newName);

                            // send message to player
                            context.getSource().sendSuccess(() ->
                                    Component.literal("Your name is " + newName), false);
                            return 1;

                        })));

    }

    // SECOND. Rendering Nickname over the player
    @SubscribeEvent
    public static void onRenderName(RenderNameTagEvent event) {
        // checking what we watch on player
        if (event.getEntity() instanceof Player targetPlayer) {

            // check nbt the one we're looking at
            CompoundTag data = targetPlayer.getPersistentData();
            if (data.contains("rpname")) {
                String name = data.getString("rpname");

                //change nickname
                event.setContent(Component.literal(name));
            } else {
                event.setResult(Event.Result.DENY);

            }

        }
    }





}
