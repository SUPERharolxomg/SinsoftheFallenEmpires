package com.sofe.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server to client: a Bearer is casting; hold their arms in this pose for so many ticks (SkillFx). */
public record CastPosePacket(int entityId, int pose, int ticks) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeByte(pose);
        buf.writeVarInt(ticks);
    }

    public static CastPosePacket decode(FriendlyByteBuf buf) {
        return new CastPosePacket(buf.readVarInt(), buf.readByte(), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.CastPoses.start(entityId, pose, ticks));
    }
}
