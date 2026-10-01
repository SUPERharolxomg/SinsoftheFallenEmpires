package com.sofe.network;

import com.sofe.economy.MerchantService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client → server: buy, sell, buy back or exchange at the merchant the player is trading with. */
public record MerchantActionPacket(String npc, MerchantService.Action action, int index) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(npc);
        buf.writeEnum(action);
        buf.writeVarInt(index);
    }

    public static MerchantActionPacket decode(FriendlyByteBuf buf) {
        return new MerchantActionPacket(buf.readUtf(), buf.readEnum(MerchantService.Action.class), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) MerchantService.act(player, npc, action, index);
    }
}
