package com.sofe.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server → client: open the Bearer selection screen. */
public record OpenClassSelectPacket() {

    public void encode(FriendlyByteBuf buf) {
    }

    public static OpenClassSelectPacket decode(FriendlyByteBuf buf) {
        return new OpenClassSelectPacket();
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> com.sofe.client.ClientPacketHandlers::openClassSelect);
    }
}
