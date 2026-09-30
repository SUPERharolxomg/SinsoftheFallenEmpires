package com.sofe.network;

import com.sofe.player.PlayerClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

/** Server → client: the player's current Bearer (empty when none has been chosen). */
public record SyncClassPacket(Optional<PlayerClass> playerClass) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeOptional(playerClass, FriendlyByteBuf::writeEnum);
    }

    public static SyncClassPacket decode(FriendlyByteBuf buf) {
        return new SyncClassPacket(buf.readOptional(b -> b.readEnum(PlayerClass.class)));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientPacketHandlers.syncClass(playerClass));
    }
}
