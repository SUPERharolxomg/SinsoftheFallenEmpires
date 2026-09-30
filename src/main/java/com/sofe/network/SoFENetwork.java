package com.sofe.network;

import com.sofe.SoFEMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * The mod's network channel. Client and server must run the same protocol version;
 * bump it whenever a packet's format changes.
 */
public final class SoFENetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            SoFEMod.id("main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private static int nextId;

    private SoFENetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(RegionEnteredPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RegionEnteredPacket::encode)
                .decoder(RegionEnteredPacket::decode)
                .consumerMainThread(RegionEnteredPacket::handle)
                .add();
    }

    public static void sendTo(ServerPlayer player, Object packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
