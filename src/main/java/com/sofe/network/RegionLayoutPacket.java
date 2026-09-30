package com.sofe.network;

import com.sofe.world.region.Region;
import com.sofe.world.region.RegionBounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Server → client, on joining a journey: the world's region layout, so the client can predict the
 * Seal Veil's collision and color exactly like the server.
 */
public record RegionLayoutPacket(List<RegionBounds> bounds) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(bounds, (b, r) -> {
            b.writeEnum(r.region());
            b.writeInt(r.minX());
            b.writeInt(r.maxX());
            b.writeInt(r.minZ());
            b.writeInt(r.maxZ());
        });
    }

    public static RegionLayoutPacket decode(FriendlyByteBuf buf) {
        return new RegionLayoutPacket(buf.readList(b -> new RegionBounds(b.readEnum(Region.class), b.readInt(), b.readInt(), b.readInt(), b.readInt())));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientLockData.setLayout(bounds));
    }
}
