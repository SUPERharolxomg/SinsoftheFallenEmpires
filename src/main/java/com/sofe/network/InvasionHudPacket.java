package com.sofe.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server → client: the Void's invasion the player is in (VoidInvasion), for the counter in the top right corner: the wave,
 * the fallen, and whether the next wave is still coming. An empty one (waves 0) takes the counter away.
 */
public record InvasionHudPacket(int wave, int waves, int fallen, int total, boolean waiting) {
    public static final InvasionHudPacket NONE = new InvasionHudPacket(0, 0, 0, 0, false);

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(wave);
        buf.writeVarInt(waves);
        buf.writeVarInt(fallen);
        buf.writeVarInt(total);
        buf.writeBoolean(waiting);
    }

    public static InvasionHudPacket decode(FriendlyByteBuf buf) {
        return new InvasionHudPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.hud.InvasionHudOverlay.show(this));
    }
}
