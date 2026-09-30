package com.sofe.network;

import com.sofe.travel.WaystoneService;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client → server: travel to one of the player's Waystones. The server checks every rule again. */
public record TravelPacket(BlockPos target) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(target);
    }

    public static TravelPacket decode(FriendlyByteBuf buf) {
        return new TravelPacket(buf.readBlockPos());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) WaystoneService.travel(player, target);
    }
}
