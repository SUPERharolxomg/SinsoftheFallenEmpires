package com.sofe.network;

import com.sofe.world.region.Region;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server → client: the player just entered a region, show its title. */
public record RegionEnteredPacket(Region region) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(region);
    }

    public static RegionEnteredPacket decode(FriendlyByteBuf buf) {
        return new RegionEnteredPacket(buf.readEnum(Region.class));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientPacketHandlers.showRegionTitle(region));
    }
}
