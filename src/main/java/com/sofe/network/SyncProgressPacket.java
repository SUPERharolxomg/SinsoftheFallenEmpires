package com.sofe.network;

import com.sofe.progression.CharacterAttribute;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Server → client: everything the HUD, the skill tree and the character sheet show: level,
 * experience, unspent points, skill ranks and slots, attribute points and the resulting values.
 */
public record SyncProgressPacket(int level, long xp, long xpToNext, int skillPoints, int attributePoints,
                                 Map<String, Integer> ranks, List<String> slots,
                                 Map<CharacterAttribute, Integer> attributes, Derived derived) {

    /** Values computed on the server from the attributes, shown on the character sheet. */
    public record Derived(float maxHealth, float physicalDamage, float magicDamage, float critChance,
                          float dodgeChance, float regen, int maxResource) {
        public static final Derived NONE = new Derived(20, 1, 1, 0, 0, 1, 0);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(level);
        buf.writeVarLong(xp);
        buf.writeVarLong(xpToNext);
        buf.writeVarInt(skillPoints);
        buf.writeVarInt(attributePoints);
        buf.writeMap(ranks, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeVarInt);
        buf.writeCollection(slots, FriendlyByteBuf::writeUtf);
        buf.writeMap(attributes, FriendlyByteBuf::writeEnum, FriendlyByteBuf::writeVarInt);
        buf.writeFloat(derived.maxHealth());
        buf.writeFloat(derived.physicalDamage());
        buf.writeFloat(derived.magicDamage());
        buf.writeFloat(derived.critChance());
        buf.writeFloat(derived.dodgeChance());
        buf.writeFloat(derived.regen());
        buf.writeVarInt(derived.maxResource());
    }

    public static SyncProgressPacket decode(FriendlyByteBuf buf) {
        int level = buf.readVarInt();
        long xp = buf.readVarLong();
        long xpToNext = buf.readVarLong();
        int skillPoints = buf.readVarInt();
        int attributePoints = buf.readVarInt();
        Map<String, Integer> ranks = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readVarInt);
        List<String> slots = buf.readList(FriendlyByteBuf::readUtf);
        Map<CharacterAttribute, Integer> attributes = buf.readMap(
                b -> new EnumMap<CharacterAttribute, Integer>(CharacterAttribute.class), b -> b.readEnum(CharacterAttribute.class), FriendlyByteBuf::readVarInt);
        Derived derived = new Derived(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readVarInt());
        return new SyncProgressPacket(level, xp, xpToNext, skillPoints, attributePoints, ranks, slots, attributes, derived);
    }

    public int rank(String skill) {
        return ranks.getOrDefault(skill, 0);
    }

    public int added(CharacterAttribute attribute) {
        return attributes.getOrDefault(attribute, 0);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientProgressData.update(this));
    }
}
