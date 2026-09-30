package com.sofe.network;

import com.sofe.player.ClassSelectionHandler;
import com.sofe.player.PlayerClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client → server: the player picked a Bearer. The server decides whether it is allowed. */
public record ChooseClassPacket(PlayerClass playerClass) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(playerClass);
    }

    public static ChooseClassPacket decode(FriendlyByteBuf buf) {
        return new ChooseClassPacket(buf.readEnum(PlayerClass.class));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            ClassSelectionHandler.choose(player, playerClass);
        }
    }
}
