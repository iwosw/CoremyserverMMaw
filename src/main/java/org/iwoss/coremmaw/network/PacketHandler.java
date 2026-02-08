package org.iwoss.coremmaw.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.iwoss.coremmaw.Coremmaw;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Coremmaw.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        //registry packet synchronize knowledge
        INSTANCE.registerMessage(packetId++, SyncKnowledgePacket.class,
                SyncKnowledgePacket::encode,
                SyncKnowledgePacket::decode,
                SyncKnowledgePacket::handle);

        INSTANCE.registerMessage(packetId++, SyncRPNamePacket.class,
                SyncRPNamePacket::encode,
                SyncRPNamePacket::decode,
                SyncRPNamePacket::handle);
    }
}
