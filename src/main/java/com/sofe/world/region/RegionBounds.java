package com.sofe.world.region;

/**
 * A rectangle of the map that belongs to one region.
 * Bounds are half-open ([min, max)) so regions that share a border never both claim it.
 */
public record RegionBounds(Region region, int minX, int maxX, int minZ, int maxZ) {

    public RegionBounds {
        if (minX >= maxX || minZ >= maxZ) {
            throw new IllegalArgumentException("Empty bounds for " + region + ": x " + minX + ".." + maxX + ", z " + minZ + ".." + maxZ);
        }
    }

    public boolean contains(int x, int z) {
        return x >= minX && x < maxX && z >= minZ && z < maxZ;
    }

    public boolean overlaps(RegionBounds other) {
        return minX < other.maxX && other.minX < maxX && minZ < other.maxZ && other.minZ < maxZ;
    }
}
