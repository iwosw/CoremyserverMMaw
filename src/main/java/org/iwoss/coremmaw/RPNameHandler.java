package org.iwoss.coremmaw;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.iwoss.coremmaw.network.ClientKnowledgeStorage;
import org.iwoss.coremmaw.network.PacketHandler;
import org.iwoss.coremmaw.network.SyncRPNamePacket;

@Mod.EventBusSubscriber(modid =  Coremmaw.MODID)
public class RPNameHandler {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("setname")
                .then(Commands.argument("name", StringArgumentType.greedyString())
                        .executes(context -> {
                            String newName = StringArgumentType.getString(context, "name");
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            //1. Save name in NBT on server
                            player.getPersistentData().putString("rpname", newName);

                            //2. Send packet all players, which see this player
                            PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                                    new SyncRPNamePacket(player.getUUID(), newName));

                            context.getSource().sendSuccess(() ->
                                    Component.literal("Ваше текущее имя: " + newName), false);
                            return 1;

                        })));

    }

    @SubscribeEvent
    public static void onRenderName(RenderNameTagEvent event) {
        // Create variable
        if (event.getEntity() instanceof Player targetPlayer) {
            Player localPlayer = Minecraft.getInstance().player;

            if (localPlayer == null || targetPlayer == localPlayer) return;

            // Check in data NBT
            if (ClientKnowledgeStorage.isKnown(targetPlayer.getUUID())) {
                String rpName = ClientKnowledgeStorage.getRPName(targetPlayer.getUUID());

                if (!rpName.isEmpty()) {
                    // Set RP name
                    event.setContent(Component.literal(rpName));
                }
            } else {
                event.setResult(Event.Result.DENY);
            }
        }
    }
}