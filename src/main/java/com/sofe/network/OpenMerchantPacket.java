package com.sofe.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Server → client: a merchant's screen, or a refresh of it after a trade. Indexes refer to the
 * server's lists, which the server checks again on every action.
 */
public record OpenMerchantPacket(String npc, long dinars, List<Offer> offers, List<Sellable> sellables, List<Sellable> buyback,
                                 boolean moneyChanger) {

    public record Offer(int index, ItemStack stack, int price, int left) {
    }

    /** An inventory slot (or a buyback index) and its price. */
    public record Sellable(int index, ItemStack stack, int price) {
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(npc);
        buf.writeVarLong(dinars);
        buf.writeCollection(offers, (b, o) -> {
            b.writeVarInt(o.index());
            b.writeItem(o.stack());
            b.writeVarInt(o.price());
            b.writeVarInt(o.left());
        });
        buf.writeCollection(sellables, OpenMerchantPacket::writeSellable);
        buf.writeCollection(buyback, OpenMerchantPacket::writeSellable);
        buf.writeBoolean(moneyChanger);
    }

    private static void writeSellable(FriendlyByteBuf b, Sellable s) {
        b.writeVarInt(s.index());
        b.writeItem(s.stack());
        b.writeVarInt(s.price());
    }

    private static Sellable readSellable(FriendlyByteBuf b) {
        return new Sellable(b.readVarInt(), b.readItem(), b.readVarInt());
    }

    public static OpenMerchantPacket decode(FriendlyByteBuf buf) {
        return new OpenMerchantPacket(buf.readUtf(), buf.readVarLong(),
                buf.readList(b -> new Offer(b.readVarInt(), b.readItem(), b.readVarInt(), b.readVarInt())),
                buf.readList(OpenMerchantPacket::readSellable), buf.readList(OpenMerchantPacket::readSellable), buf.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientPacketHandlers.openMerchant(this));
    }
}
