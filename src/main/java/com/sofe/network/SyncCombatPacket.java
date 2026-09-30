package com.sofe.network;

import com.sofe.combat.Rune;
import com.sofe.player.ResourceType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Server → client: what the HUD shows. Resource value and maximum, the runes waiting, and for
 * each skill slot when its cooldown ends (in game ticks) and how long it lasts; the Thief's Marks
 * and the Necromancer's souls.
 */
public record SyncCombatPacket(ResourceType resource, float current, int max, List<Rune> runes, List<SlotCooldown> slots,
                               int marks, int souls) {

    public record SlotCooldown(String skill, long endTick, int durationTicks) {
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(resource);
        buf.writeFloat(current);
        buf.writeVarInt(max);
        buf.writeCollection(runes, FriendlyByteBuf::writeEnum);
        buf.writeCollection(slots, (b, s) -> {
            b.writeUtf(s.skill());
            b.writeVarLong(s.endTick());
            b.writeVarInt(s.durationTicks());
        });
        buf.writeVarInt(marks);
        buf.writeVarInt(souls);
    }

    public static SyncCombatPacket decode(FriendlyByteBuf buf) {
        ResourceType resource = buf.readEnum(ResourceType.class);
        float current = buf.readFloat();
        int max = buf.readVarInt();
        List<Rune> runes = buf.readList(b -> b.readEnum(Rune.class));
        List<SlotCooldown> slots = buf.readList(b -> new SlotCooldown(b.readUtf(), b.readVarLong(), b.readVarInt()));
        return new SyncCombatPacket(resource, current, max, runes, slots, buf.readVarInt(), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientCombatData.update(this));
    }
}
