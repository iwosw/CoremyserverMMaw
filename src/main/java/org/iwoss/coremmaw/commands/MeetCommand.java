package org.iwoss.coremmaw.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import org.iwoss.coremmaw.network.PacketHandler;
import org.iwoss.coremmaw.network.SyncKnowledgePacket;
import org.iwoss.coremmaw.network.KnowledgeHandler;

public class MeetCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("meet")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            ServerPlayer source = context.getSource().getPlayerOrException();
                            ServerPlayer target = EntityArgument.getPlayer(context, "player");

                            if (source == target) {
                                context.getSource().sendFailure(Component.literal("Вы не можете познакомится с собой!"));
                                return 0;

                            }

                        //1. Save meet on server
                        KnowledgeHandler.learnPlayer(source, target);


                        //2. Send packet on client
                        PacketHandler.INSTANCE.send(
                                PacketDistributor.PLAYER.with(() -> source),
                                new SyncKnowledgePacket(target.getUUID())
                        );

                        context.getSource().sendSuccess(() ->
                                Component.literal("Вы познакомились с игроком!"), false);

                        return 1;



                        })));

    }
}