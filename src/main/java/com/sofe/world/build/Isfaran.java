package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import static com.sofe.world.build.CapitalCity.AIR;
import static com.sofe.world.build.CapitalCity.b;
import static com.sofe.world.build.CapitalCity.stair;

/**
 * Isfaran, the court of Parsivan, after its concept (art/concepts/city_parsivan.png): a city of dusky violet and teal at
 * dusk. North of the plaza the palace on its terrace: violet walls banded with turquoise, a great iwan (a pointed arch
 * in a tall frame) for its door, a great violet dome on a drum of windows, teal domes beside it, four minarets with
 * purple flags; inside, a hall of carpets and divans round a fountain under the dome, the shah's throne at the end.
 * Before it, a long pool glowing violet from below. East and west, hanging gardens on terraces of dark teal, overgrown
 * with flowers and vines, a violet pavilion on top. Domed kiosks in the streets, a bazaar along the south avenue, and
 * the south gate between two white towers with pointed spires.
 */
final class Isfaran {
    private final CapitalCity c;
    private final BlockState violetWall = Blocks.PURPLE_TERRACOTTA.defaultBlockState(), violet = b(SoFEBlocks.PARSIVAN_VIOLET_TILES);
    private final BlockState tiles = b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES), lapis = b(SoFEBlocks.PARSIVAN_LAPIS_MOSAIC);
    private final BlockState plaster = b(SoFEBlocks.PARSIVAN_WHITE_PLASTER), teal = Blocks.CYAN_TERRACOTTA.defaultBlockState();
    private final BlockState darkTeal = Blocks.DARK_PRISMARINE.defaultBlockState(), gold = Blocks.GOLD_BLOCK.defaultBlockState();
    private final BlockState amethyst = Blocks.AMETHYST_BLOCK.defaultBlockState();

    private Isfaran(CapitalCity c) {
        this.c = c;
    }

    static void landmarks(CapitalCity c) {
        Isfaran i = new Isfaran(c);
        i.palace(c.cx, c.cz - 62);
        i.pool(c.cx, c.cz - 26);
        for (int sx : new int[]{-1, 1}) i.gardens(c.cx + sx * 64, c.cz - 44);
        int[][] kiosks = {{-34, 40}, {34, 40}, {-70, 64}, {70, 64}, {-60, -88}, {60, -88}};
        for (int k = 0; k < kiosks.length; k++) {
            boolean v = k % 2 == 0;
            c.pavilion(c.cx + kiosks[k][0], c.cz + kiosks[k][1], 4, 5, b(SoFEBlocks.PARSIVAN_WHITE_PLASTER_WALL), i.tiles,
                    v ? i.violet : i.teal, i.tiles, i.gold);
        }
    }

    static void southGate(CapitalCity c) {
        new Isfaran(c).gate();
    }

    // ------------------------------------------------------------------ the palace

    private void palace(int x0, int z0) {
        int top = c.top, hw = 20, hd = 13, h = 13;
        c.platform(x0, z0, 30, 22, 2, plaster, tiles, lapis, b(SoFEBlocks.PARSIVAN_WHITE_PLASTER_WALL), 5, Direction.SOUTH);
        int floor = top + 2;
        // the walls: violet with bands of turquoise, pointed windows of violet glass between pilasters of plaster
        for (int x = x0 - hw; x <= x0 + hw; x++) {
            for (int z = z0 - hd; z <= z0 + hd; z++) {
                boolean edge = Math.abs(x - x0) == hw || Math.abs(z - z0) == hd;
                c.put(x, floor - 1, z, Math.floorMod(x + z, 4) == 0 ? tiles : lapis);
                for (int dy = 0; dy < h; dy++) {
                    if (!edge) {
                        c.put(x, floor + dy, z, AIR);
                        continue;
                    }
                    int along = Math.abs(x - x0) == hw ? z - z0 : x - x0;
                    boolean pilaster = Math.floorMod(along, 5) == 0;
                    int k = Math.floorMod(along, 5);
                    boolean window = !pilaster && dy >= 3 && dy <= 8 && (k == 2 || k == 3) && !(dy == 8 && k == 3);
                    c.put(x, floor + dy, z, pilaster ? (dy % 4 == 0 ? tiles : plaster) : window ? Blocks.MAGENTA_STAINED_GLASS_PANE.defaultBlockState()
                            : dy == 1 || dy == h - 2 ? tiles : dy == h - 1 ? lapis : dy < 3 ? violetWall : violet);
                }
                c.put(x, floor + h, z, edge ? tiles : darkTeal);
                if (edge && Math.floorMod(x + z, 2) == 0) c.put(x, floor + h + 1, z, plaster);
            }
        }
        // the great dome, violet with ribs of amethyst, on a drum of windows; teal domes on the wings
        Architecture.openRoof(c.level, x0, floor + h, z0, 10);
        Architecture.drum(c.level, x0, floor + h + 1, z0, 11, 5, plaster, tiles);
        int crown = Architecture.dome(c.level, x0, floor + h + 6, z0, 11, violet, amethyst, gold);
        c.put(x0, crown + 3, z0, Blocks.LIGHTNING_ROD.defaultBlockState());
        for (int sx : new int[]{-16, 16}) {
            Architecture.drum(c.level, x0 + sx, floor + h + 1, z0 + 4, 4, 3, plaster, tiles);
            Architecture.dome(c.level, x0 + sx, floor + h + 4, z0 + 4, 4, teal, tiles, gold);
            Architecture.drum(c.level, x0 + sx, floor + h + 1, z0 - 7, 3, 2, plaster, tiles);
            Architecture.dome(c.level, x0 + sx, floor + h + 3, z0 - 7, 3, violet, amethyst, gold);
        }
        // four minarets with purple flags
        for (int sx : new int[]{-1, 1}) {
            for (int sz : new int[]{-1, 1}) {
                int mx = x0 + sx * (hw + 4), mz = z0 + sz * (hd + 4);
                Architecture.minaret(c.level, mx, floor, mz, 38, violet, tiles, teal);
                c.flag(mx, floor + 41, mz, Direction.EAST);
            }
        }
        // the iwan: a tall frame of turquoise round a deep pointed arch, the door at its back
        int fz = z0 + hd;
        for (int dx = -7; dx <= 7; dx++) {
            for (int dy = 0; dy <= h + 6; dy++) {
                for (int dz = 0; dz <= 3; dz++) {
                    int ax = Math.abs(dx);
                    boolean arch = ax <= 4 && dy <= 11 - Math.max(0, ax - 1) && dz >= 1; // the pointed niche
                    boolean frame = ax == 7 || dy == h + 6 || ax == 5 && dy <= 12 || dy == 13 && ax <= 5;
                    BlockState s = arch ? AIR : frame ? tiles : dy > 13 ? (Math.floorMod(dx + dy, 2) == 0 ? lapis : violet) : violet;
                    c.put(x0 + dx, floor + dy, fz + dz, s);
                }
            }
            if (Math.floorMod(dx, 2) == 0) c.put(x0 + dx, floor + h + 7, fz + 1, plaster);
        }
        for (int dx = -2; dx <= 2; dx++) for (int dy = 0; dy < 6; dy++) c.put(x0 + dx, floor + dy, fz, Math.abs(dx) == 2 || dy == 5 ? lapis : AIR);
        c.put(x0, floor + 10, fz + 2, c.hanging());
        for (int side : new int[]{-1, 1}) {
            c.wallBanner(x0 + side * 6, floor + 8, fz + 4, Direction.SOUTH, Blocks.PURPLE_WALL_BANNER);
            c.greatBanner(x0 + side * 12, floor + h - 1, z0 + hd + 1, Direction.SOUTH); // on the palace front, either side of the iwan
        }
        palaceInside(x0, z0, hw, hd, h, floor);
        c.claim(x0 - 32, z0 - 24, x0 + 32, z0 + 24);
    }

    /**
     * Inside: a carpet of purple edged in cyan from the door to the throne, a fountain under the dome, divans along the
     * walls with low tables, the shah's throne of gold and purple on a dais of lapis under banners, the astronomer's
     * shelves, plants, a great chandelier and lanterns.
     */
    private void palaceInside(int x0, int z0, int hw, int hd, int h, int floor) {
        for (int z = z0 - hd + 5; z <= z0 + hd - 1; z++) {
            if (Math.abs(z - z0) <= 4) continue; // the fountain
            for (int dx = -2; dx <= 2; dx++) c.put(x0 + dx, floor, z, CapitalCity.carpet(Math.abs(dx) == 2 ? "cyan" : "purple"));
        }
        // the fountain: an octagon of turquoise with a jet
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
            if (Math.abs(dx) + Math.abs(dz) > 6) continue;
            boolean rim = Math.abs(dx) == 4 || Math.abs(dz) == 4 || Math.abs(dx) + Math.abs(dz) == 6;
            c.put(x0 + dx, floor - 1, z0 + dz, rim ? tiles : Blocks.WATER.defaultBlockState());
            c.put(x0 + dx, floor - 2, z0 + dz, lapis);
            if (rim) c.put(x0 + dx, floor, z0 + dz, b(SoFEBlocks.PARSIVAN_WHITE_PLASTER_SLAB));
        }
        c.put(x0, floor - 1, z0, tiles);
        c.put(x0, floor, z0, tiles);
        c.put(x0, floor + 1, z0, Blocks.WATER.defaultBlockState());
        // the dais and the throne
        for (int step = 0; step < 2; step++) {
            for (int x = x0 - 5 + step; x <= x0 + 5 - step; x++) for (int z = z0 - hd + 1; z <= z0 - hd + 4 - step; z++) c.put(x, floor + step, z, step == 1 ? lapis : tiles);
        }
        int tz = z0 - hd + 2;
        c.put(x0, floor + 2, tz, gold);
        c.put(x0, floor + 2, tz + 1, stair(Blocks.PURPUR_STAIRS.defaultBlockState(), Direction.NORTH));
        c.put(x0, floor + 3, tz, Blocks.PURPLE_WOOL.defaultBlockState());
        c.put(x0, floor + 4, tz, gold);
        for (int sx : new int[]{-1, 1}) {
            c.put(x0 + sx, floor + 2, tz + 1, Furniture.divan("purple", Direction.SOUTH));
            c.put(x0 + sx * 3, floor + 2, tz, Blocks.POTTED_FLOWERING_AZALEA.defaultBlockState());
            c.statue(x0 + sx * 5, floor, z0 - hd + 6, 0, "chain", null); // the shah's guards
        }
        for (int dx = -4; dx <= 4; dx += 2) c.wallBanner(x0 + dx, floor + 6, z0 - hd + 1, Direction.SOUTH, dx == 0 ? Blocks.CYAN_WALL_BANNER : Blocks.PURPLE_WALL_BANNER);
        // divans and low tables along both side walls, banners and plants between them
        for (int sx : new int[]{-1, 1}) {
            int wx = x0 + sx * (hw - 1);
            Direction looking = sx > 0 ? Direction.WEST : Direction.EAST;
            for (int z = z0 - hd + 2; z <= z0 + hd - 2; z++) {
                int k = Math.floorMod(z - z0, 6);
                if (k == 0) c.put(wx, floor, z, Blocks.POTTED_ALLIUM.defaultBlockState());
                else c.put(wx, floor, z, Furniture.divan(k % 2 == 0 ? "purple" : "cyan", looking));
                if (k == 3) {
                    c.put(wx - sx * 2, floor, z, Furniture.table("dark_oak"));
                    c.put(wx - sx * 2, floor + 1, z, CapitalCity.candles(2));
                }
                if (k == 0) c.wallBanner(wx, floor + 6, z, looking, Math.floorMod(z, 12) == 0 ? Blocks.PURPLE_WALL_BANNER : Blocks.CYAN_WALL_BANNER);
            }
            c.rug(x0 + sx * 6, z0 - hd + 6, x0 + sx * 14, z0 + hd - 3, floor, CapitalCity.carpet("purple"), CapitalCity.carpet("magenta"));
        }
        // the astronomer's corner: shelves, a lectern and a spyglass on a desk
        for (int dy = 0; dy < 3; dy++) for (int dx = 0; dx < 3; dx++) c.put(x0 - hw + 1 + dx, floor + dy, z0 + hd - 1, Blocks.BOOKSHELF.defaultBlockState());
        c.put(x0 - hw + 4, floor, z0 + hd - 2, Blocks.LECTERN.defaultBlockState());
        Architecture.grandChandelier(c.level, x0, floor + h + 12, floor + h - 1, z0, 4);
        for (int z : new int[]{z0 - hd + 6, z0 + hd - 4}) for (int sx : new int[]{-8, 8}) {
            Architecture.chandelier(c.level, x0 + sx, floor + h - 1, floor + h - 4, z);
        }
    }

    // ------------------------------------------------------------------ the pool, the gardens, the gate

    /** The long pool before the palace, glowing violet from below, a jet in the middle, flowers and lamps round it. */
    private void pool(int x0, int z0) {
        int top = c.top, hw = 11, hd = 8;
        for (int x = x0 - hw - 2; x <= x0 + hw + 2; x++) {
            for (int z = z0 - hd - 2; z <= z0 + hd + 2; z++) {
                int dx = Math.abs(x - x0), dz = Math.abs(z - z0);
                boolean inside = dx < hw && dz < hd, rim = !inside && dx <= hw && dz <= hd;
                if (inside) {
                    c.put(x, top - 4, z, lapis);
                    c.put(x, top - 3, z, Math.floorMod(x + z, 3) == 0 ? Blocks.SEA_LANTERN.defaultBlockState() : lapis);
                    c.put(x, top - 2, z, Blocks.PURPLE_STAINED_GLASS.defaultBlockState());
                    c.put(x, top - 1, z, Blocks.WATER.defaultBlockState());
                    c.put(x, top, z, AIR);
                } else if (rim) {
                    c.put(x, top - 1, z, tiles);
                    c.put(x, top, z, Math.floorMod(x + z, 4) == 0 ? Blocks.FLOWERING_AZALEA.defaultBlockState() : b(SoFEBlocks.PARSIVAN_WHITE_PLASTER_SLAB));
                } else {
                    c.put(x, top - 1, z, Math.floorMod(x + z, 2) == 0 ? plaster : Blocks.SMOOTH_STONE.defaultBlockState()); // the walk round it
                }
            }
        }
        for (int dy = -2; dy < 2; dy++) c.put(x0, top + dy, z0, tiles);
        c.put(x0, top + 2, z0, Blocks.WATER.defaultBlockState());
        for (int sx : new int[]{-1, 1}) for (int sz : new int[]{-1, 1}) c.lamp(x0 + sx * (hw + 2), z0 + sz * (hd + 2));
        c.claim(x0 - hw - 3, z0 - hd - 3, x0 + hw + 3, z0 + hd + 3);
    }

    /**
     * Hanging gardens: three terraces of dark teal stone with turquoise bands and pointed arches, overflowing with
     * azaleas, alliums, lilacs and glow berries, vines down their walls, a violet pavilion on the top.
     */
    private void gardens(int x0, int z0) {
        int top = c.top;
        int[] half = {15, 11, 7};
        for (int t = 0; t < 3; t++) {
            int base = top + t * 5, r = half[t];
            for (int x = x0 - r; x <= x0 + r; x++) {
                for (int z = z0 - r; z <= z0 + r; z++) {
                    boolean edge = Math.abs(x - x0) == r || Math.abs(z - z0) == r;
                    int along = Math.abs(x - x0) == r ? z - z0 : x - x0;
                    boolean arch = edge && t == 0 && Math.floorMod(along, 6) == 3 && Math.abs(along) < r - 1;
                    for (int y = base; y < base + 4; y++) {
                        BlockState s = edge ? (y == base + 3 ? tiles : arch && y < base + 3 ? (y == base + 2 ? violet : AIR) : darkTeal)
                                : t == 0 && y < base + 3 ? AIR : Blocks.DIRT.defaultBlockState();
                        c.put(x, y, z, s);
                    }
                    if (t == 0 && !edge) c.put(x, base + 2, z, darkTeal); // the vault under the first terrace
                    c.put(x, base + 4, z, edge ? b(SoFEBlocks.PARSIVAN_WHITE_PLASTER_WALL) : Blocks.GRASS_BLOCK.defaultBlockState());
                    if (!edge && t < 2 && Math.abs(x - x0) <= half[t + 1] && Math.abs(z - z0) <= half[t + 1]) continue; // the next terrace stands here
                    if (!edge) plant(x, base + 5, z);
                    boolean corner = Math.abs(x - x0) == r && Math.abs(z - z0) == r;
                    if (edge && !corner && Math.floorMod(x + z, 3) == 0) { // vines down the walls
                        int ox = Math.abs(x - x0) == r ? Integer.signum(x - x0) : 0, oz = Math.abs(z - z0) == r ? Integer.signum(z - z0) : 0;
                        for (int y = base + 3; y > base; y--) {
                            if (c.level.getBlockState(new net.minecraft.core.BlockPos(x + ox, y, z + oz)).isAir()) c.put(x + ox, y, z + oz, vine(ox, oz));
                        }
                    }
                    if (t == 0 && !edge && Math.floorMod(x * 3 + z * 5, 11) == 0) { // glow berries hanging in the vault
                        c.put(x, top + 1, z, Blocks.CAVE_VINES.defaultBlockState().setValue(CaveVines.BERRIES, true));
                    }
                }
            }
        }
        // the lights under the vault
        for (int dx = -9; dx <= 9; dx += 6) for (int dz = -9; dz <= 9; dz += 6) c.put(x0 + dx, top + 1, z0 + dz, c.hanging());
        // the pavilion on the top terrace
        int pt = top + 15;
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
            boolean col = Math.abs(dx) == 3 && Math.abs(dz) == 3;
            c.put(x0 + dx, pt - 1, z0 + dz, tiles);
            for (int y = pt; y < pt + 4; y++) c.put(x0 + dx, y, z0 + dz, col ? b(SoFEBlocks.PARSIVAN_WHITE_PLASTER_WALL) : AIR);
            c.put(x0 + dx, pt + 4, z0 + dz, Math.abs(dx) == 3 || Math.abs(dz) == 3 ? tiles : plaster);
        }
        Architecture.dome(c.level, x0, pt + 5, z0, 3, violet, amethyst, gold);
        c.put(x0, pt + 3, z0, c.hanging());
        c.put(x0, pt, z0, Furniture.divan("purple", Direction.SOUTH));
        // a stair up the south face, all the way to the top
        for (int i = 0; i < 15; i++) {
            for (int w = -1; w <= 1; w++) {
                c.put(x0 + w, top + i, z0 + 15 + 1 - i, stair(b(SoFEBlocks.PARSIVAN_WHITE_PLASTER_STAIRS), Direction.NORTH));
                for (int y = top + i + 1; y <= top + i + 3; y++) c.put(x0 + w, y, z0 + 15 + 1 - i, AIR);
            }
        }
        c.claim(x0 - 17, z0 - 17, x0 + 17, z0 + 19);
    }

    private BlockState vine(int ox, int oz) {
        BlockState v = Blocks.VINE.defaultBlockState();
        if (ox > 0) return v.setValue(VineBlock.WEST, true);
        if (ox < 0) return v.setValue(VineBlock.EAST, true);
        if (oz > 0) return v.setValue(VineBlock.NORTH, true);
        return v.setValue(VineBlock.SOUTH, true);
    }

    private void plant(int x, int y, int z) {
        int roll = c.random.nextInt(14);
        if (roll == 0) { // a flowering azalea bush
            for (int dy = 0; dy < 2; dy++) c.put(x, y + dy, z, Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
        } else if (roll == 1) {
            c.put(x, y, z, Blocks.LILAC.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
            c.put(x, y + 1, z, Blocks.LILAC.defaultBlockState().setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        } else if (roll < 4) {
            c.put(x, y, z, Blocks.ALLIUM.defaultBlockState());
        } else if (roll < 6) {
            c.put(x, y, z, Blocks.FLOWERING_AZALEA.defaultBlockState());
        } else if (roll == 6) {
            c.put(x, y, z, Blocks.PINK_TULIP.defaultBlockState());
        } else if (roll == 7) {
            c.put(x, y, z, Blocks.BLUE_ORCHID.defaultBlockState());
        } else if (roll == 8) {
            c.put(x, y, z, Blocks.PINK_PETALS.defaultBlockState());
        }
    }

    /**
     * The south gate: two white towers banded with turquoise under pointed spires, a high gate of violet tiles framed in
     * white between them, a pointed arch, purple banners.
     */
    private void gate() {
        int top = c.top, gz = c.cz + c.a;
        BlockState white = Blocks.POLISHED_DIORITE.defaultBlockState(), calcite = Blocks.CALCITE.defaultBlockState();
        for (int side : new int[]{-1, 1}) {
            int tx = c.cx + side * 10;
            for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
                boolean edge = Math.abs(dx) == 3 || Math.abs(dz) == 3;
                for (int dy = 0; dy < 24; dy++) {
                    boolean window = edge && (dx == 0 || dz == 0) && dy % 6 == 3;
                    c.put(tx + dx, top + dy, gz + dz, !edge ? AIR : window ? Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState()
                            : dy % 6 == 5 ? tiles : Math.abs(dx) == 3 && Math.abs(dz) == 3 ? calcite : white);
                }
            }
            int peak = c.hipRoof(tx - 3, gz - 3, tx + 3, gz + 3, top + 24, Blocks.POLISHED_DIORITE_STAIRS.defaultBlockState(), white, 9);
            for (int dy = 1; dy <= 3; dy++) c.put(tx, peak + dy, gz, dy == 3 ? gold : Blocks.DIORITE_WALL.defaultBlockState()); // the spire
            c.flag(tx, peak + 4, gz, Direction.EAST);
        }
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy < 18; dy++) {
                    int ax = Math.abs(dx);
                    boolean arch = ax <= 3 && dy <= 10 - Math.max(0, ax - 1);
                    boolean frame = ax == 6 || dy == 17 || ax == 4 && dy <= 12 || dy == 12 && ax <= 4;
                    c.put(c.cx + dx, top + dy, gz + dz, arch ? AIR : frame ? white : Math.floorMod(dx + dy, 3) == 0 ? lapis : violet);
                }
                if (Math.floorMod(dx, 2) == 0) c.put(c.cx + dx, top + 18, gz + dz, white);
            }
        }
        for (int dx = -2; dx <= 2; dx += 2) {
            c.wallBanner(c.cx + dx, top + 14, gz + 3, Direction.SOUTH, Blocks.PURPLE_WALL_BANNER);
            c.wallBanner(c.cx + dx, top + 14, gz - 3, Direction.NORTH, Blocks.PURPLE_WALL_BANNER);
        }
        c.put(c.cx, top + 9, gz, c.hanging());
        for (int side : new int[]{-1, 1}) { // the great banners of Parsivan either side of the arch, outside and in
            c.greatBanner(c.cx + side * 5, top + 16, gz + 3, Direction.SOUTH);
            c.greatBanner(c.cx + side * 5, top + 16, gz - 3, Direction.NORTH);
        }
    }

}
