package com.sofe.network;

import com.sofe.world.lock.RegionStatus;
import com.sofe.world.region.Region;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Server → client: the player just entered a region (or the Burning Deep beneath it), show its title and lock status. */
public record RegionEnteredPacket(Region region, RegionStatus status, boolean deep) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(region);
        buf.writeEnum(status);
        buf.writeBoolean(deep);
    }

    public static RegionEnteredPacket decode(FriendlyByteBuf buf) {
        return new RegionEnteredPacket(buf.readEnum(Region.class), buf.readEnum(RegionStatus.class), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientPacketHandlers.showRegionTitle(region, status, deep));
    }
}
