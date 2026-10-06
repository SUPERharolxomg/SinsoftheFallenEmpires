package com.sofe.pact;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/** The Pacts of one world, saved with it (data/sofe_pacts.dat), so a Pact lasts across restarts. */
public class PactData extends SavedData {
    private static final String NAME = "sofe_pacts";
    private final Map<UUID, Pact> pacts = new HashMap<>();
    private final Map<UUID, UUID> memberOf = new HashMap<>();

    public static PactData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(PactData::load, PactData::new, NAME);
    }

    public Optional<Pact> of(UUID player) {
        UUID id = memberOf.get(player);
        return id == null ? Optional.empty() : Optional.ofNullable(pacts.get(id));
    }

    public Collection<Pact> all() {
        return pacts.values();
    }

    public Pact create(UUID leader, String name) {
        Pact pact = new Pact(UUID.randomUUID(), leader, name);
        pacts.put(pact.id(), pact);
        memberOf.put(leader, pact.id());
        setDirty();
        return pact;
    }

    public void join(Pact pact, UUID member, String name) {
        pact.add(member, name);
        memberOf.put(member, pact.id());
        setDirty();
    }

    /** Takes a member out; a Pact left with one member is dissolved. Returns the Pact as it is left, if it still stands. */
    public Optional<Pact> leave(UUID member) {
        Optional<Pact> pact = of(member);
        if (pact.isEmpty()) return Optional.empty();
        Pact p = pact.get();
        p.remove(member);
        memberOf.remove(member);
        if (p.size() <= 1) {
            p.members().forEach(memberOf::remove);
            pacts.remove(p.id());
            setDirty();
            return Optional.empty();
        }
        setDirty();
        return Optional.of(p);
    }

    public void rename(UUID member, String name) {
        of(member).ifPresent(p -> {
            p.rename(member, name);
            setDirty();
        });
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Pact p : pacts.values()) {
            CompoundTag t = new CompoundTag();
            t.putUUID("id", p.id());
            t.putUUID("leader", p.leader());
            ListTag members = new ListTag();
            for (UUID m : p.members()) {
                CompoundTag mt = new CompoundTag();
                mt.putUUID("uuid", m);
                mt.putString("name", p.name(m));
                members.add(mt);
            }
            t.put("members", members);
            list.add(t);
        }
        tag.put("pacts", list);
        return tag;
    }

    private static PactData load(CompoundTag tag) {
        PactData data = new PactData();
        for (Tag t : tag.getList("pacts", Tag.TAG_COMPOUND)) {
            CompoundTag pt = (CompoundTag) t;
            UUID leader = pt.getUUID("leader");
            Pact pact = null;
            for (Tag m : pt.getList("members", Tag.TAG_COMPOUND)) {
                CompoundTag mt = (CompoundTag) m;
                UUID uuid = mt.getUUID("uuid");
                if (pact == null) pact = new Pact(pt.getUUID("id"), leader, uuid.equals(leader) ? mt.getString("name") : "?");
                pact.add(uuid, mt.getString("name"));
                data.memberOf.put(uuid, pact.id());
            }
            if (pact != null) data.pacts.put(pact.id(), pact);
        }
        return data;
    }
}
