package com.sofe.network;

import com.sofe.progression.CharacterAttribute;
import com.sofe.progression.ProgressionHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client → server: put an attribute point into this attribute. */
public record SpendAttributePacket(CharacterAttribute attribute) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(attribute);
    }

    public static SpendAttributePacket decode(FriendlyByteBuf buf) {
        return new SpendAttributePacket(buf.readEnum(CharacterAttribute.class));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player != null) {
            ProgressionHandler.spendAttribute(player, attribute);
        }
    }
}
