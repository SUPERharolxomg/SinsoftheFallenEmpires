package com.sofe.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server → client: level, experience toward the next level and unspent points, for the HUD and the screens. */
public record SyncProgressPacket(int level, long xp, long xpToNext, int skillPoints, int attributePoints) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(level);
        buf.writeVarLong(xp);
        buf.writeVarLong(xpToNext);
        buf.writeVarInt(skillPoints);
        buf.writeVarInt(attributePoints);
    }

    public static SyncProgressPacket decode(FriendlyByteBuf buf) {
        return new SyncProgressPacket(buf.readVarInt(), buf.readVarLong(), buf.readVarLong(), buf.readVarInt(), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientProgressData.update(this));
    }
}
