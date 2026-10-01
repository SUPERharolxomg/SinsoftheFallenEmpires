package com.sofe.world.region;

/** The round biome zones inside a region (docs/Mundo.md, W1), with a wavy edge. Pure math. */
public final class BiomeZones {

    private BiomeZones() {
    }

    public static boolean contains(int centerX, int centerZ, int radius, int x, int z) {
        double dx = x - centerX, dz = z - centerZ;
        double wobble = 40 * Math.sin(x / 83.0) * Math.cos(z / 71.0) + 20 * Math.sin((x - z) / 47.0);
        return Math.sqrt(dx * dx + dz * dz) + wobble < radius;
    }
}
