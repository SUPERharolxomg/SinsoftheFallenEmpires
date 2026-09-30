package com.sofe.travel;

import com.sofe.world.lock.RegionLocks;
import com.sofe.world.region.RegionMap;

/** When Waystone travel is allowed (docs/Jugabilidad.md, G2), without Minecraft. */
public final class TravelRules {
    /** Hurting or being hurt within this many ticks counts as being in combat. */
    public static final int COMBAT_TICKS = 200;

    public enum Result { OK, IN_COMBAT, IN_ARENA, SEALED_REGION, NOT_ACTIVATED }

    private TravelRules() {
    }

    public static Result check(boolean activated, int ticksSinceCombat, boolean inArena, RegionMap map, int targetX, int targetZ, int act, boolean bypass) {
        if (!activated) return Result.NOT_ACTIVATED;
        if (bypass) return Result.OK;
        if (ticksSinceCombat < COMBAT_TICKS) return Result.IN_COMBAT;
        if (inArena) return Result.IN_ARENA;
        if (map != null && !RegionLocks.canBeAt(map, targetX, targetZ, act)) return Result.SEALED_REGION;
        return Result.OK;
    }
}
