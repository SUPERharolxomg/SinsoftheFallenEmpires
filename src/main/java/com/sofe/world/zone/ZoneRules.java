package com.sofe.world.zone;

import java.util.List;
import java.util.Optional;

/**
 * Decides whether a block may change, as a chain of rules (docs/Arquitectura.md, Chain of
 * Responsibility): the Homestead allows everything even when it sits inside a bigger zone; any other
 * zone applies its own kind's rule; outside every zone, yes. No one gets round the rules: not an
 * operator, not a player in creative, not a mob.
 */
public final class ZoneRules {

    private ZoneRules() {
    }

    public static boolean allowed(List<ProtectedZone> zones, int x, int y, int z, ZoneAction action) {
        if (zoneAt(zones, x, y, z, ProtectedZone.Kind.HOMESTEAD).isPresent()) return true;
        for (ProtectedZone zone : zones) {
            if (zone.contains(x, y, z) && !zone.kind().allows(action)) return false;
        }
        return true;
    }

    public static Optional<ProtectedZone> zoneAt(List<ProtectedZone> zones, int x, int y, int z) {
        return zones.stream().filter(zone -> zone.contains(x, y, z)).findFirst();
    }

    private static Optional<ProtectedZone> zoneAt(List<ProtectedZone> zones, int x, int y, int z, ProtectedZone.Kind kind) {
        return zones.stream().filter(zone -> zone.kind() == kind && zone.contains(x, y, z)).findFirst();
    }
}
