package org.iwoss.coremmaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncKnowledgePacket {
    private final UUID knownPlayerUUID;

    public SyncKnowledgePacket(UUID uuid) {
        this.knownPlayerUUID = uuid;

    }

    //write data in buffer
    public static void encode(SyncKnowledgePacket msg,FriendlyByteBuf buf) {
        buf.writeUUID(msg.knownPlayerUUID);

    }

    //read data in buffer
    public static SyncKnowledgePacket decode(FriendlyByteBuf buf) {
        return new SyncKnowledgePacket(buf.readUUID());
    }

    //logic which complete on clientside
    public static void handle(SyncKnowledgePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientKnowledgeStorage.addPlayer(msg.knownPlayerUUID);
        });
        ctx.get().setPacketHandled(true);

    }

}