package com.sofe.network;

import com.sofe.skill.SkillCaster;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client → server: the player pressed a Combat Bar slot. The server checks everything before casting. */
public record CastSkillPacket(int slot) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(slot);
    }

    public static CastSkillPacket decode(FriendlyByteBuf buf) {
        return new CastSkillPacket(buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            SkillCaster.cast(player, slot);
        }
    }
}
