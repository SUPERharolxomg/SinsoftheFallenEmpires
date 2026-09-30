package com.sofe.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Server → client: the player's activated Waystones, to open the travel screen.
 *
 * @param nameKey empty for Waystones placed by players (the screen shows their coordinates)
 */
public record OpenWaystonesPacket(List<Entry> entries) {

    public record Entry(BlockPos pos, String nameKey, boolean here, boolean reachable) {
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(entries, (b, e) -> {
            b.writeBlockPos(e.pos());
            b.writeUtf(e.nameKey());
            b.writeBoolean(e.here());
            b.writeBoolean(e.reachable());
        });
    }

    public static OpenWaystonesPacket decode(FriendlyByteBuf buf) {
        return new OpenWaystonesPacket(buf.readList(b -> new Entry(b.readBlockPos(), b.readUtf(), b.readBoolean(), b.readBoolean())));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientPacketHandlers.openWaystones(entries));
    }
}
