package com.sofe.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Server → client: what the Journal and the Quest Compass show: the act, every quest the player
 * has started, the fates chosen so far, the regions still to choose, the compass target (with the NPC
 * to talk to there, if any) and the
 * player's latest body in the overworld (the compass points there first);
 * and whether this player bypasses the locks (an operator with opsBypass), for the Seal Veil.
 */
public record SyncStoryPacket(int act, List<Quest> quests, Optional<String> tracked, Map<String, String> fates,
                              List<String> openFates, Optional<Target> compass, Optional<Target> corpse, boolean bypassLocks,
                              List<String> bosses) {

    /**
     * @param type     "main", "bearer" or "side"
     * @param step     index of the current step (the last one when completed)
     * @param required how many the current objective asks for (the count bar in the Journal)
     */
    public record Quest(String id, String type, int step, int steps, int count, int required, boolean completed) {
    }

    /** @param npc the person to talk to there, marked in the world (empty: a place) */
    public record Target(int x, int z, String npc) {
        public Target(int x, int z) {
            this(x, z, "");
        }
    }

    private static void writeTarget(FriendlyByteBuf b, Target t) {
        b.writeInt(t.x());
        b.writeInt(t.z());
        b.writeUtf(t.npc());
    }

    private static Target readTarget(FriendlyByteBuf b) {
        return new Target(b.readInt(), b.readInt(), b.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(act);
        buf.writeCollection(quests, (b, q) -> {
            b.writeUtf(q.id());
            b.writeUtf(q.type());
            b.writeVarInt(q.step());
            b.writeVarInt(q.steps());
            b.writeVarInt(q.count());
            b.writeVarInt(q.required());
            b.writeBoolean(q.completed());
        });
        buf.writeOptional(tracked, FriendlyByteBuf::writeUtf);
        buf.writeMap(fates, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
        buf.writeCollection(openFates, FriendlyByteBuf::writeUtf);
        buf.writeOptional(compass, SyncStoryPacket::writeTarget);
        buf.writeOptional(corpse, SyncStoryPacket::writeTarget);
        buf.writeBoolean(bypassLocks);
        buf.writeCollection(bosses, FriendlyByteBuf::writeUtf); // the bosses beaten, for the Codex's bestiary
    }

    public static SyncStoryPacket decode(FriendlyByteBuf buf) {
        int act = buf.readVarInt();
        List<Quest> quests = buf.readList(b -> new Quest(b.readUtf(), b.readUtf(), b.readVarInt(), b.readVarInt(),
                b.readVarInt(), b.readVarInt(), b.readBoolean()));
        Optional<String> tracked = buf.readOptional(FriendlyByteBuf::readUtf);
        Map<String, String> fates = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf);
        List<String> openFates = buf.readList(FriendlyByteBuf::readUtf);
        Optional<Target> compass = buf.readOptional(SyncStoryPacket::readTarget);
        Optional<Target> corpse = buf.readOptional(SyncStoryPacket::readTarget);
        boolean bypass = buf.readBoolean();
        return new SyncStoryPacket(act, quests, tracked, fates, openFates, compass, corpse, bypass, buf.readList(FriendlyByteBuf::readUtf));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientStoryData.update(this));
    }
}
