package com.sofe.pact;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** A Pact of the Empires: up to five Bearers who travel together, its leader first. Names are kept for members offline. */
public final class Pact {
    private final UUID id;
    private UUID leader;
    private final Map<UUID, String> members = new LinkedHashMap<>();

    public Pact(UUID id, UUID leader, String leaderName) {
        this.id = id;
        this.leader = leader;
        members.put(leader, leaderName);
    }

    public UUID id() {
        return id;
    }

    public UUID leader() {
        return leader;
    }

    public List<UUID> members() {
        return new ArrayList<>(members.keySet());
    }

    public String name(UUID member) {
        return members.getOrDefault(member, "?");
    }

    public int size() {
        return members.size();
    }

    public boolean has(UUID member) {
        return members.containsKey(member);
    }

    void add(UUID member, String name) {
        members.put(member, name);
    }

    /** Removes a member; when the leader leaves, the next member leads. */
    void remove(UUID member) {
        members.remove(member);
        if (member.equals(leader) && !members.isEmpty()) leader = members.keySet().iterator().next();
    }

    void rename(UUID member, String name) {
        if (members.containsKey(member)) members.put(member, name);
    }
}
