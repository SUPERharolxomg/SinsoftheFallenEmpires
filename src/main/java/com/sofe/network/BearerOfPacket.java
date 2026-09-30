package com.sofe.network;

import com.sofe.player.PlayerClass;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

/** Server → client: the Bearer of another player, so their outfit can be drawn. */
public record BearerOfPacket(int entityId, Optional<PlayerClass> playerClass) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeOptional(playerClass, FriendlyByteBuf::writeEnum);
    }

    public static BearerOfPacket decode(FriendlyByteBuf buf) {
        return new BearerOfPacket(buf.readVarInt(), buf.readOptional(b -> b.readEnum(PlayerClass.class)));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientBearers.set(entityId, playerClass));
    }
}
