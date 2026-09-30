package com.sofe.world.region;

/** Where the Ashen Wastes lie inside Sulthari (docs/Mundo.md, W1): the ring around the city. */
public final class AshenWastes {

    private AshenWastes() {
    }

    /**
     * Whether a Sulthari column is in the Ashen Wastes: its distance from the region's center
     * (the larger of |dx| and |dz|) passes the inner radius, pushed in and out by up to 80 blocks
     * so the edge is not a straight line. Pure math, the same on every server.
     */
    public static boolean contains(RegionBounds sulthari, int innerRadius, int x, int z) {
        int cx = (sulthari.minX() + sulthari.maxX()) / 2, cz = (sulthari.minZ() + sulthari.maxZ()) / 2;
        double distance = Math.max(Math.abs(x - cx), Math.abs(z - cz));
        double wobble = 50 * Math.sin(x / 97.0) * Math.cos(z / 113.0) + 30 * Math.sin((x + z) / 61.0);
        return distance + wobble > innerRadius;
    }
}
