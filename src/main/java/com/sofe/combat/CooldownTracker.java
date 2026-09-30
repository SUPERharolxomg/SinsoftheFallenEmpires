package com.sofe.combat;

import java.util.HashMap;
import java.util.Map;

/** Skill cooldowns of one player, measured in game ticks (the world's game time). */
public final class CooldownTracker {
    private record Cooldown(long start, long end) {
    }

    private final Map<String, Cooldown> cooldowns = new HashMap<>();

    public void start(String skill, long now, int durationTicks) {
        if (durationTicks > 0) {
            cooldowns.put(skill, new Cooldown(now, now + durationTicks));
        }
    }

    public boolean isReady(String skill, long now) {
        return remaining(skill, now) == 0;
    }

    public long remaining(String skill, long now) {
        Cooldown c = cooldowns.get(skill);
        return c == null ? 0 : Math.max(0, c.end - now);
    }

    public long end(String skill) {
        Cooldown c = cooldowns.get(skill);
        return c == null ? 0 : c.end;
    }

    public long duration(String skill) {
        Cooldown c = cooldowns.get(skill);
        return c == null ? 0 : c.end - c.start;
    }

    public void clear() {
        cooldowns.clear();
    }
}
