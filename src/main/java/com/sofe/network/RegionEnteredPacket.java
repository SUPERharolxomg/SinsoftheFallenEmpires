package com.sofe.network;

import com.sofe.world.lock.RegionStatus;
import com.sofe.world.region.Region;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server → client: the player just entered a region, show its title and its lock status. */
public record RegionEnteredPacket(Region region, RegionStatus status) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(region);
        buf.writeEnum(status);
    }

    public static RegionEnteredPacket decode(FriendlyByteBuf buf) {
        return new RegionEnteredPacket(buf.readEnum(Region.class), buf.readEnum(RegionStatus.class));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientPacketHandlers.showRegionTitle(region, status));
    }
}
