package com.sofe.world.lock;

import com.sofe.world.region.Region;
import com.sofe.world.region.RegionBounds;
import com.sofe.world.region.RegionMap;

import java.util.Optional;

/**
 * The progression locks of docs/Mundo.md (W2) without Minecraft: where a player in a given act
 * may stand, where the Seal Veil stands and whether it lets them through.
 */
public final class RegionLocks {
    /** How far from an open coast a player may sail (docs/Mundo.md, layer 2). */
    public static final int COAST_LIMIT = 300;

    private RegionLocks() {
    }

    public static boolean isOpen(Region region, int act) {
        return region.opensAtAct() > 0 && act >= region.opensAtAct();
    }

    /** Whether a player in this act may stand on this column. The ocean is allowed only near an open coast. */
    public static boolean canBeAt(RegionMap map, int x, int z, int act) {
        Region region = map.regionAt(x, z);
        if (region != Region.OCEAN) return isOpen(region, act);
        long limit = (long) COAST_LIMIT * COAST_LIMIT;
        for (RegionBounds b : map.bounds()) {
            if (isOpen(b.region(), act) && distanceSquared(b, x, z) <= limit) return true;
        }
        return false;
    }

    /** The region that keeps a player out of this column, if any. */
    public static Optional<Region> blockingRegion(RegionMap map, int x, int z, int act) {
        if (canBeAt(map, x, z, act)) return Optional.empty();
        return Optional.of(map.regionAt(x, z));
    }

    /**
     * A Seal Veil column: the first column of a region that touches another region on its west or
     * north side. Borders with the ocean have no Veil (the coast limit handles those).
     */
    public static boolean isVeilColumn(RegionMap map, int x, int z) {
        return isVeilColumn(map, x, z, 1);
    }

    /**
     * The same in a dimension that maps to the overworld at a scale (8 in the Nether, docs/Mundo.md W5):
     * the column (x, z) stands for the overworld column (x * scale, z * scale).
     */
    public static boolean isVeilColumn(RegionMap map, int x, int z, int scale) {
        return otherSide(map, x, z, scale).isPresent();
    }

    /** The region on the other side of the Veil at this column. */
    public static Optional<Region> otherSide(RegionMap map, int x, int z) {
        return otherSide(map, x, z, 1);
    }

    public static Optional<Region> otherSide(RegionMap map, int x, int z, int scale) {
        Region here = map.regionAt(x * scale, z * scale);
        if (here == Region.OCEAN) return Optional.empty();
        Region west = map.regionAt((x - 1) * scale, z * scale);
        if (west != here && west != Region.OCEAN) return Optional.of(west);
        Region north = map.regionAt(x * scale, (z - 1) * scale);
        if (north != here && north != Region.OCEAN) return Optional.of(north);
        return Optional.empty();
    }

    /** The Veil lets a player through when both regions it separates are open for them. */
    public static boolean veilPassable(RegionMap map, int x, int z, int act) {
        return veilPassable(map, x, z, act, 1);
    }

    public static boolean veilPassable(RegionMap map, int x, int z, int act, int scale) {
        Optional<Region> other = otherSide(map, x, z, scale);
        return other.isEmpty() || (isOpen(map.regionAt(x * scale, z * scale), act) && isOpen(other.get(), act));
    }

    /** The sealed region behind a Veil column, for the "The seal of ... holds" message. */
    public static Optional<Region> sealedBehind(RegionMap map, int x, int z, int act) {
        return sealedBehind(map, x, z, act, 1);
    }

    public static Optional<Region> sealedBehind(RegionMap map, int x, int z, int act, int scale) {
        Region here = map.regionAt(x * scale, z * scale);
        if (!isOpen(here, act)) return Optional.of(here);
        return otherSide(map, x, z, scale).filter(r -> !isOpen(r, act));
    }

    private static long distanceSquared(RegionBounds b, int x, int z) {
        long dx = x < b.minX() ? b.minX() - x : (x >= b.maxX() ? x - (b.maxX() - 1) : 0);
        long dz = z < b.minZ() ? b.minZ() - z : (z >= b.maxZ() ? z - (b.maxZ() - 1) : 0);
        return dx * dx + dz * dz;
    }
}
