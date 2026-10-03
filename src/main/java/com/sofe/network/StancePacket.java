package com.sofe.network;

import com.sofe.skill.ClassMechanics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client to server: the Knight switches between Shield Stance and Charge Stance. */
public record StancePacket() {

    public void encode(FriendlyByteBuf buf) {
    }

    public static StancePacket decode(FriendlyByteBuf buf) {
        return new StancePacket();
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) ClassMechanics.toggleStance(player);
    }
}
