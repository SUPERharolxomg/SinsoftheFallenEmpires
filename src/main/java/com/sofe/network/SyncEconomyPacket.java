package com.sofe.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server → client: the player's Dinars, Bearer's Flask charges and known Blueprints, for the HUD and the screens. */
public record SyncEconomyPacket(long dinars, int flaskCharges, int flaskMax, java.util.Set<String> blueprints) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarLong(dinars);
        buf.writeVarInt(flaskCharges);
        buf.writeVarInt(flaskMax);
        buf.writeCollection(blueprints, FriendlyByteBuf::writeUtf);
    }

    public static SyncEconomyPacket decode(FriendlyByteBuf buf) {
        return new SyncEconomyPacket(buf.readVarLong(), buf.readVarInt(), buf.readVarInt(), java.util.Set.copyOf(buf.readList(FriendlyByteBuf::readUtf)));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientEconomyData.update(this));
    }
}
