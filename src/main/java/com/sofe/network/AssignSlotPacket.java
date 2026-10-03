package com.sofe.network;

import com.sofe.progression.ProgressionHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client to server: put this learned skill in a Combat Bar slot. The server checks the skill is the player's. */
public record AssignSlotPacket(String skill, int slot) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(skill);
        buf.writeVarInt(slot);
    }

    public static AssignSlotPacket decode(FriendlyByteBuf buf) {
        return new AssignSlotPacket(buf.readUtf(64), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) ProgressionHandler.assignSlot(player, skill, slot);
    }
}
