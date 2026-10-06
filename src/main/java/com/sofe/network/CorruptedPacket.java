package com.sofe.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server → client: this vanilla zombie bears Act V's corruption, draw its violet eyes (MobTraits). */
public record CorruptedPacket(int entity) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entity);
    }

    public static CorruptedPacket decode(FriendlyByteBuf buf) {
        return new CorruptedPacket(buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.render.CorruptedEyesLayer.mark(entity));
    }
}
