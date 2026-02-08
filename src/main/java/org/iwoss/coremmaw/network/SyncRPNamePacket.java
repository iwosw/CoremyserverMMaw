package org.iwoss.coremmaw.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncRPNamePacket {
    private final UUID playerUUID;
    private final String rpName;

    public SyncRPNamePacket(UUID playerUUID, String rpName) {
        this.playerUUID = playerUUID;
        this.rpName = rpName;
    }

    // write data
    public static void encode(SyncRPNamePacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.playerUUID);
        buf.writeUtf(msg.rpName);
    }

    // read data
    public static SyncRPNamePacket decode(FriendlyByteBuf buf) {
        return new SyncRPNamePacket(buf.readUUID(), buf.readUtf());
    }

    // Packet on client
    public static void handle(SyncRPNamePacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Save new name
            ClientKnowledgeStorage.updateRPName(msg.playerUUID, msg.rpName);
        });
        ctx.get().setPacketHandled(true);
    }
}