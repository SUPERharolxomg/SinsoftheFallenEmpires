package com.sofe.network;

import com.sofe.progression.ProgressionHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client → server: put a skill point into this skill. The server checks the tree rules. */
public record LearnSkillPacket(String skill) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(skill);
    }

    public static LearnSkillPacket decode(FriendlyByteBuf buf) {
        return new LearnSkillPacket(buf.readUtf(64));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            ProgressionHandler.learnSkill(player, skill);
        }
    }
}
