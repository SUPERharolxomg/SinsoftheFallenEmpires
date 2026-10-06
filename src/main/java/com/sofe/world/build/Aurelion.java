package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static com.sofe.world.build.CapitalCity.AIR;
import static com.sofe.world.build.CapitalCity.b;
import static com.sofe.world.build.CapitalCity.pillarY;
import static com.sofe.world.build.CapitalCity.stair;

/**
 * Aurelion, the capital of the Aureum Republic, after its concept (art/concepts/city_aureum.png): a city of white marble
 * under royal blue roofs edged with gold. North of the plaza the Imperial Palace stands on a raised terrace, a Byzantine
 * hall under a great golden dome with golden half-domes on its apses, four lesser domes and four bell towers, behind a
 * portico of columns under a blue pediment; inside, a nave between colonnades, a blue carpet between legionaries up to
 * the throne. North-east, on its own hill, the Colosseum, three storeys of arches round the sand. North-west, the
 * Pantheon: a rotunda under a white ribbed dome open to the sky. Long basilicas with blue roofs line the east-west
 * avenue; temples with columns all round stand south of it, little round temples further south; a triumphal arch and a
 * column of victory mark the forum; the aqueduct comes over the hills from the west on two storeys of arches; the
 * south gate is a gatehouse crested with gold; outside the walls, fields of wheat.
 */
final class Aurelion {
    private final CapitalCity c;
    private final BlockState marble = b(SoFEBlocks.AUREUM_POLISHED_MARBLE), bricks = b(SoFEBlocks.AUREUM_MARBLE_BRICKS);
    private final BlockState pillar = pillarY(b(SoFEBlocks.AUREUM_MARBLE_PILLAR)), gold = b(SoFEBlocks.AUREUM_GOLD_MOSAIC);
    private final BlockState blue = b(SoFEBlocks.AUREUM_ROYAL_TILES), blueStairs = b(SoFEBlocks.AUREUM_ROYAL_TILE_STAIRS);
    private final BlockState blueSlab = b(SoFEBlocks.AUREUM_ROYAL_TILE_SLAB), rail = b(SoFEBlocks.AUREUM_MARBLE_BRICK_WALL);
    private final BlockState goldBlock = Blocks.GOLD_BLOCK.defaultBlockState(), quartz = Blocks.SMOOTH_QUARTZ.defaultBlockState();

    private Aurelion(CapitalCity c) {
        this.c = c;
    }

    static void landmarks(CapitalCity c) {
        Aurelion a = new Aurelion(c);
        a.palace(c.cx, c.cz - 56);
        a.colosseum(c.cx + 88, c.cz - 100);
        a.pantheon(c.cx - 84, c.cz - 94);
        a.basilica(c.cx - 78, c.cz - 26, 38, 8);
        a.basilica(c.cx + 78, c.cz - 26, 38, 8);
        a.temple(c.cx - 56, c.cz + 48);
        a.temple(c.cx + 56, c.cz + 48);
        a.tholos(c.cx - 44, c.cz + 112);
        a.tholos(c.cx + 44, c.cz + 112);
        a.column(c.cx, c.cz);
        a.arch(c.cx, c.cz + 26);
        a.aqueduct(c.cz - 152);
        a.statues();
    }

    static void southGate(CapitalCity c) {
        new Aurelion(c).gatehouse();
    }

    static void outside(CapitalCity c) {
        new Aurelion(c).fields();
    }

    // ------------------------------------------------------------------ the Imperial Palace

    private void palace(int x0, int z0) {
        int top = c.top, hw = 22, hd = 15, h = 14;
        c.platform(x0, z0, 34, 26, 4, bricks, gold, marble, rail, 6, Direction.SOUTH, Direction.EAST, Direction.WEST);
        int floor = top + 4;
        // the floor: polished marble, a ring of gold and royal blue under the dome
        for (int x = x0 - 33; x <= x0 + 33; x++) {
            for (int z = z0 - 25; z <= z0 + 25; z++) {
                double d = Math.hypot(x - x0, z - z0);
                BlockState f = d < 12 ? (Math.floorMod((int) d, 3) == 0 ? gold : Math.floorMod(x + z, 2) == 0 ? blue : marble)
                        : Math.floorMod(x - x0, 6) == 0 || Math.floorMod(z - z0, 6) == 0 ? quartz : marble;
                c.put(x, floor - 1, z, f);
            }
        }
        // the walls: marble between engaged columns, a band of gold under a cornice, tall windows
        for (int x = x0 - hw; x <= x0 + hw; x++) {
            for (int z = z0 - hd; z <= z0 + hd; z++) {
                boolean edge = Math.abs(x - x0) == hw || Math.abs(z - z0) == hd;
                for (int dy = 0; dy < h; dy++) {
                    if (!edge) {
                        c.put(x, floor + dy, z, AIR);
                        continue;
                    }
                    int along = Math.abs(x - x0) == hw ? z - z0 : x - x0;
                    boolean col = Math.floorMod(along, 4) == 0;
                    boolean window = !col && dy >= 3 && dy <= h - 4 && Math.floorMod(along, 4) == 2;
                    c.put(x, floor + dy, z, col ? pillar : window ? Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState()
                            : dy == h - 2 ? gold : dy == h - 1 ? quartz : bricks);
                }
                c.put(x, floor + h, z, edge ? gold : blue); // the roof of royal blue
                if (edge && Math.floorMod(x + z, 2) == 0) c.put(x, floor + h + 1, z, rail);
            }
        }
        // the apses east and west, under golden half-domes, open to the hall
        for (int side : new int[]{-1, 1}) apse(x0 + side * hw, z0, side, 9, floor, h - 2);
        // the great dome on its drum of windows, and the lesser domes at the corners
        Architecture.openRoof(c.level, x0, floor + h, z0, 12);
        Architecture.drum(c.level, x0, floor + h + 1, z0, 12, 5, bricks, gold);
        int crown = Architecture.dome(c.level, x0, floor + h + 6, z0, 12, goldBlock, gold, goldBlock);
        c.put(x0, crown + 3, z0, Blocks.LIGHTNING_ROD.defaultBlockState());
        for (int sx : new int[]{-15, 15}) {
            for (int sz : new int[]{-9, 9}) {
                Architecture.drum(c.level, x0 + sx, floor + h + 1, z0 + sz, 4, 2, bricks, gold);
                Architecture.dome(c.level, x0 + sx, floor + h + 3, z0 + sz, 4, goldBlock, gold, goldBlock);
            }
        }
        // four bell towers at the corners, under blue pyramids with gold finials
        for (int sx : new int[]{-1, 1}) {
            for (int sz : new int[]{-1, 1}) {
                int tx = x0 + sx * (hw + 3), tz = z0 + sz * (hd + 3);
                for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                    boolean edge = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                    for (int dy = 0; dy < 26; dy++) {
                        boolean arch = !edge || dy >= 19 && dy <= 22 && (dx == 0 || dz == 0); // the belfry's openings
                        c.put(tx + dx, floor + dy, tz + dz, arch ? AIR : dy % 8 == 7 ? gold : Math.abs(dx) == 2 && Math.abs(dz) == 2 ? pillar : bricks);
                    }
                }
                c.put(tx, floor + 21, tz, c.hanging());
                int peak = c.hipRoof(tx - 2, tz - 2, tx + 2, tz + 2, floor + 26, blueStairs, blue, 9);
                c.put(tx, peak + 1, tz, goldBlock);
                c.put(tx, peak + 2, tz, Blocks.LIGHTNING_ROD.defaultBlockState());
            }
        }
        // the portico: two rows of columns under a blue pediment, the great door behind
        int pz0 = z0 + hd + 1, pz1 = z0 + hd + 7;
        for (int x = x0 - 11; x <= x0 + 11; x++) {
            for (int z = pz0; z <= pz1; z++) {
                boolean col = Math.floorMod(x - x0 + 11, 3) == 0 && (z == pz1 || z == pz1 - 3);
                for (int dy = 0; dy < h - 1; dy++) c.put(x, floor + dy, z, col ? pillar : AIR);
                c.put(x, floor + h - 1, z, z == pz1 ? gold : bricks);
            }
        }
        c.gable(x0 - 11, pz0, x0 + 11, pz1, floor + h, blueStairs, blueSlab, marble, true);
        for (int x = x0 - 3; x <= x0 + 3; x += 3) c.put(x, floor + h + 4, pz1 + 1, gold); // gold medallions in the pediment
        c.statue(x0, floor + h + 13, pz1, 0, "gold", null);
        for (int dx = -3; dx <= 3; dx++) for (int dy = 0; dy < 9; dy++) {
            c.put(x0 + dx, floor + dy, z0 + hd, Math.abs(dx) == 3 || dy == 8 ? gold : AIR);
        }
        for (int side : new int[]{-1, 1}) c.greatBanner(x0 + side * 16, floor + h - 2, z0 + hd + 1, Direction.SOUTH);
        palaceInside(x0, z0, hw, hd, h, floor);
        c.claim(x0 - 36, z0 - 28, x0 + 36, z0 + 28);
    }

    /** A semicircular apse on the side of a hall, under a golden half-dome, opening into the hall. */
    private void apse(int ax, int az, int side, int r, int floor, int h) {
        for (int dx = 1; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.hypot(dx, dz);
                if (d > r + 0.3) continue;
                int x = ax + side * dx, z = az + dz;
                c.put(x, floor - 1, z, marble);
                for (int dy = 0; dy < h; dy++) {
                    boolean wall = d > r - 1;
                    boolean window = wall && dy >= 3 && dy <= h - 3 && Math.abs(Math.sin(Math.atan2(dz, dx) * 5)) < 0.3;
                    c.put(x, floor + dy, z, !wall ? AIR : window ? Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState() : dy == h - 1 ? gold : bricks);
                }
            }
        }
        int height = Math.round(r * 0.95f); // the half-dome
        for (int dy = 0; dy <= height; dy++) {
            double t = dy / (height + 0.5), radius = r * Math.sqrt(Math.max(0, 1 - t * t));
            double above = r * Math.sqrt(Math.max(0, 1 - Math.pow((dy + 1) / (height + 0.5), 2)));
            for (int dx = 0; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    double d = Math.hypot(dx, dz);
                    if (d > radius + 0.3) continue;
                    boolean surface = d > radius - 1.3 || d > above - 0.5 || dy == height;
                    if (surface) c.put(ax + side * dx, floor + h + dy, az + dz, dy == 0 ? gold : goldBlock);
                }
            }
        }
        for (int dz = -r + 2; dz <= r - 2; dz++) { // the opening into the hall
            for (int dy = 0; dy < h - 1; dy++) c.put(ax, floor + dy, az + dz, Math.abs(dz) == r - 2 ? pillar : AIR);
        }
    }

    /**
     * Inside the palace: a nave between two colonnades, a blue carpet edged in gold from the door to a dais of gold at
     * the north end, the throne under banners, legionaries along the carpet, gold statues by the throne, benches in the
     * aisles, braziers, plants, banners between the windows and chandeliers.
     */
    private void palaceInside(int x0, int z0, int hw, int hd, int h, int floor) {
        for (int sx : new int[]{-8, 8}) {
            for (int z = z0 - hd + 3; z <= z0 + hd - 3; z += 4) {
                if (Math.hypot(sx, z - z0) < 12.5 && Math.abs(z - z0) < 8) continue; // the space under the dome stays open
                for (int dy = 0; dy < h; dy++) c.put(x0 + sx, floor + dy, z, dy == h - 1 ? gold : pillar);
            }
        }
        // the dais and the throne
        for (int step = 0; step < 3; step++) {
            for (int x = x0 - 6 + step; x <= x0 + 6 - step; x++) {
                for (int z = z0 - hd + 1; z <= z0 - hd + 5 - step; z++) c.put(x, floor + step, z, step == 2 ? blue : gold);
            }
        }
        int tz = z0 - hd + 2;
        c.put(x0, floor + 3, tz, goldBlock);
        c.put(x0, floor + 3, tz + 1, stair(Blocks.QUARTZ_STAIRS.defaultBlockState(), Direction.NORTH));
        c.put(x0 - 1, floor + 3, tz + 1, stair(blueStairs, Direction.EAST));
        c.put(x0 + 1, floor + 3, tz + 1, stair(blueStairs, Direction.WEST));
        c.put(x0, floor + 4, tz, goldBlock);
        c.put(x0, floor + 5, tz, Blocks.LIGHTNING_ROD.defaultBlockState());
        for (int dx = -4; dx <= 4; dx += 2) c.wallBanner(x0 + dx, floor + 7, z0 - hd + 1, Direction.SOUTH, dx == 0 ? Blocks.YELLOW_WALL_BANNER : Blocks.BLUE_WALL_BANNER);
        for (int sx : new int[]{-5, 5}) {
            c.statue(x0 + sx, floor + 2, tz + 1, 0, "gold", null);
            c.brazier(x0 + sx + Integer.signum(sx) * 2, floor, z0 - hd + 6, gold);
        }
        // the carpet up the nave, and legionaries along it
        for (int z = z0 - hd + 6; z <= z0 + hd - 1; z++) {
            for (int dx = -2; dx <= 2; dx++) {
                c.put(x0 + dx, floor, z, Math.abs(dx) == 2 ? CapitalCity.carpet("yellow") : CapitalCity.carpet("blue"));
            }
            if (Math.floorMod(z - z0, 6) == 3 && Math.abs(z - z0) > 4) {
                c.statue(x0 - 4, floor, z, -90, "iron", null);
                c.statue(x0 + 4, floor, z, 90, "iron", null);
            }
        }
        // benches in the aisles, facing the nave, and plants by the columns
        for (int sx : new int[]{-1, 1}) {
            for (int z = z0 - hd + 4; z <= z0 + hd - 3; z += 3) {
                if (Math.abs(z - z0) < 8) continue;
                for (int k = 11; k <= 17; k++) c.put(x0 + sx * k, floor, z, stair(b(SoFEBlocks.AUREUM_MARBLE_BRICK_STAIRS), sx > 0 ? Direction.EAST : Direction.WEST));
            }
            c.put(x0 + sx * 20, floor, z0 + hd - 2, Blocks.POTTED_FLOWERING_AZALEA.defaultBlockState());
            c.put(x0 + sx * 20, floor, z0 - hd + 2, Blocks.POTTED_FLOWERING_AZALEA.defaultBlockState());
            for (int z = z0 - hd + 2; z <= z0 + hd - 2; z += 4) { // banners on the side walls, between the windows
                c.wallBanner(x0 + sx * (hw - 1), floor + 6, z, sx > 0 ? Direction.WEST : Direction.EAST,
                        Math.floorMod(z, 8) < 4 ? Blocks.BLUE_WALL_BANNER : Blocks.YELLOW_WALL_BANNER);
            }
        }
        // a table of state in each apse, with candles and chairs
        for (int side : new int[]{-1, 1}) {
            int ax = x0 + side * (hw + 4);
            for (int dz = -2; dz <= 2; dz++) {
                c.put(ax, floor, z0 + dz, Furniture.table("spruce"));
                c.put(ax, floor + 1, z0 + dz, dz % 2 == 0 ? CapitalCity.candles(3) : AIR);
                c.put(ax - 1, floor, z0 + dz, Furniture.chair("spruce", Direction.EAST));
                c.put(ax + 1, floor, z0 + dz, Furniture.chair("spruce", Direction.WEST));
            }
            Architecture.chandelier(c.level, ax, floor + h + 4, floor + h - 3, z0);
        }
        // the light: a great chandelier from the dome, chandeliers over the nave
        Architecture.grandChandelier(c.level, x0, floor + h + 12, floor + h - 1, z0, 4);
        for (int z : new int[]{z0 - hd + 7, z0 + hd - 5}) Architecture.chandelier(c.level, x0, floor + h - 1, floor + h - 5, z);
    }

    // ------------------------------------------------------------------ the Colosseum on its hill

    private void colosseum(int x0, int z0) {
        int top = c.top, rx = 46, rz = 37, rise = 5, apron = 10;
        // the hill: a mound paved round the walls, grassy on its slopes
        for (int dx = -rx - apron; dx <= rx + apron; dx++) {
            for (int dz = -rz - apron; dz <= rz + apron; dz++) {
                double e = Math.sqrt(dx * dx / Math.pow(rx + apron, 2) + dz * dz / Math.pow(rz + apron, 2));
                if (e > 1) continue;
                double eWall = Math.sqrt(dx * dx / Math.pow(rx + 4, 2) + dz * dz / Math.pow(rz + 4, 2));
                int hgt = eWall <= 1 ? rise : (int) Math.round(rise * (1 - e) / Math.max(0.01, 1 - (rx + 4.0) / (rx + apron)));
                hgt = Math.max(0, Math.min(rise, hgt));
                int x = x0 + dx, z = z0 + dz;
                for (int y = top - 1; y < top - 1 + hgt; y++) c.put(x, y, z, c.fill());
                c.put(x, top - 1 + hgt, z, eWall <= 1 ? (Math.floorMod(dx + dz, 2) == 0 ? marble : quartz) : Blocks.GRASS_BLOCK.defaultBlockState());
                for (int y = top + hgt; y < top + hgt + 4; y++) c.put(x, y, z, AIR);
            }
        }
        // the processional stair down the south side and the west side
        for (int i = 0; i <= rise; i++) {
            for (int w = -3; w <= 3; w++) {
                for (int[] p : new int[][]{{x0 + w, z0 + rz + 4 + 2 * i, 0}, {x0 + w, z0 + rz + 5 + 2 * i, 0},
                        {x0 - rx - 4 - 2 * i, z0 + w, 1}, {x0 - rx - 5 - 2 * i, z0 + w, 1}}) {
                    Direction up = p[2] == 0 ? Direction.NORTH : Direction.EAST;
                    for (int y = top - 1; y < top - 1 + rise - i; y++) c.put(p[0], y, p[1], c.fill());
                    c.put(p[0], top - 1 + rise - i, p[1], stair(b(SoFEBlocks.AUREUM_MARBLE_BRICK_STAIRS), up));
                    for (int y = top + rise - i; y < top + rise + 3; y++) c.put(p[0], y, p[1], AIR);
                }
            }
        }
        Colosseum.build(c.level, x0, top + rise, z0, rx, rz, 24, 17, 3, EnumSet.of(Direction.EAST, Direction.WEST, Direction.SOUTH), false,
                (x, y, z) -> bricks, (x, y, z) -> pillar);
        // the Colossus by its side: a golden emperor on a high pedestal
        int sx = x0 - rx - 2, sz = z0 + rz - 2;
        for (int dy = -rise - 1; dy < 6; dy++) for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            c.put(sx + dx, top + rise + dy, sz + dz, dy == 5 ? gold : bricks);
        }
        c.statue(sx, top + rise + 6, sz, 135, "gold", null);
        c.claim(x0 - rx - apron - 2, z0 - rz - apron - 2, x0 + rx + apron + 2, z0 + rz + apron + 14);
    }

    // ------------------------------------------------------------------ the Pantheon

    private void pantheon(int x0, int z0) {
        int top = c.top, r = 19, h = 16;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.hypot(dx, dz);
                if (d > r + 0.3) continue;
                int x = x0 + dx, z = z0 + dz;
                // the floor of squares and circles, porphyry and marble
                c.put(x, top - 1, z, Math.floorMod(dx, 6) < 3 == Math.floorMod(dz, 6) < 3 ? blue : marble);
                for (int dy = 0; dy < h; dy++) {
                    boolean wall = d > r - 2;
                    double angle = Math.atan2(dz, dx);
                    boolean niche = wall && d <= r - 1 && dy >= 1 && dy <= 6 && Math.abs(Math.sin(angle * 4)) < 0.12;
                    c.put(x, top + dy, z, !wall || niche ? AIR : dy == h - 1 || dy == 8 ? gold
                            : Math.abs(Math.sin(angle * 16)) < 0.2 && d > r - 1 ? pillar : bricks);
                }
            }
        }
        int crown = Architecture.dome(c.level, x0, top + h, z0, r, quartz, Blocks.QUARTZ_BRICKS.defaultBlockState(), AIR);
        for (int y = crown - 3; y <= crown + 2; y++) { // the oculus
            for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
                if (dx * dx + dz * dz <= 9) c.put(x0 + dx, y, z0 + dz, dx * dx + dz * dz >= 7 && y == crown - 3 ? gold : AIR);
            }
        }
        // the block between the rotunda and the portico, as in Rome
        for (int x = x0 - 10; x <= x0 + 10; x++) {
            for (int z = z0 + 10; z <= z0 + r + 1; z++) {
                if (Math.hypot(x - x0, z - z0) <= r - 0.5) continue;
                c.put(x, top - 1, z, marble);
                for (int dy = 0; dy < h; dy++) c.put(x, top + dy, z, dy == h - 1 || dy == 8 ? gold : bricks);
            }
        }
        // the portico on the south side
        int pz0 = z0 + r + 2, pz1 = z0 + r + 11;
        for (int x = x0 - 10; x <= x0 + 10; x++) {
            for (int z = pz0; z <= pz1; z++) {
                c.put(x, top - 1, z, marble);
                boolean col = Math.floorMod(x - x0 + 10, 3) == 0 && (z == pz1 || z == pz1 - 4);
                for (int dy = 0; dy < h - 3; dy++) c.put(x, top + dy, z, col ? pillar : AIR);
                c.put(x, top + h - 3, z, z == pz1 ? gold : bricks);
            }
        }
        c.gable(x0 - 10, pz0, x0 + 10, pz1, top + h - 2, blueStairs, blueSlab, marble, true);
        for (int z = z0 + r - 2; z <= z0 + r + 1; z++) { // the great bronze door, open
            for (int dx = -3; dx <= 3; dx++) for (int dy = 0; dy < 10; dy++) c.put(x0 + dx, top + dy, z, Math.abs(dx) == 3 || dy == 9 ? gold : AIR);
        }
        // inside: gods in the niches, an altar under the oculus, braziers
        for (int k = 0; k < 8; k++) {
            double angle = k * Math.PI / 4 + Math.PI / 8 * 0;
            if (Math.abs(angle - Math.PI / 2) < 0.1) continue; // the door
            int nx = x0 + (int) Math.round(Math.cos(angle) * (r - 2)), nz = z0 + (int) Math.round(Math.sin(angle) * (r - 2));
            float yaw = (float) Math.toDegrees(Math.atan2(-(x0 - nx), z0 - nz));
            c.put(nx, top, nz, gold);
            c.statue(nx, top + 1, nz, yaw, "gold", null);
            int bx = x0 + (int) Math.round(Math.cos(angle + Math.PI / 8) * (r - 4)), bz = z0 + (int) Math.round(Math.sin(angle + Math.PI / 8) * (r - 4));
            c.brazier(bx, top, bz, bricks);
        }
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) c.put(x0 + dx, top, z0 + dz, Math.abs(dx) + Math.abs(dz) == 0 ? goldBlock : gold);
        c.put(x0, top + 1, z0, Blocks.CAMPFIRE.defaultBlockState());
        // the light: lanterns hanging round the drum from a gold ring, candles on every niche's pedestal
        for (int k = 0; k < 16; k++) {
            double angle = k * Math.PI / 8;
            int lx = x0 + (int) Math.round(Math.cos(angle) * (r - 5)), lz = z0 + (int) Math.round(Math.sin(angle) * (r - 5));
            Architecture.chandelier(c.level, lx, top + h - 1, top + h - 5, lz);
        }
        c.claim(x0 - r - 3, z0 - r - 3, x0 + r + 3, pz1 + 4);
    }

    // ------------------------------------------------------------------ basilicas, temples, monuments

    /**
     * A civic basilica: a long hall along the avenue under a royal blue gable, colonnades outside on the long sides,
     * two rows of columns inside, the magistrate's tribunal at the far end, benches, banners and chandeliers.
     */
    private void basilica(int x0, int z0, int hw, int hd) {
        int top = c.top, h = 10;
        for (int x = x0 - hw - 2; x <= x0 + hw + 2; x++) {
            for (int z = z0 - hd - 2; z <= z0 + hd + 2; z++) {
                boolean wall = Math.abs(x - x0) == hw && Math.abs(z - z0) <= hd || Math.abs(z - z0) == hd && Math.abs(x - x0) <= hw;
                boolean porch = Math.abs(x - x0) > hw || Math.abs(z - z0) > hd;
                boolean col = porch && (Math.abs(z - z0) == hd + 2 || Math.abs(x - x0) == hw + 2) && Math.floorMod(x + z, 3) == 0;
                c.put(x, top - 1, z, porch ? quartz : Math.floorMod(x, 4) < 2 == Math.floorMod(z, 4) < 2 ? marble : gold);
                for (int dy = 0; dy < h; dy++) {
                    BlockState s = AIR;
                    if (wall) {
                        boolean door = Math.abs(z - z0) == hd && Math.floorMod(x - x0, 10) == 0 && dy < 4 && Math.abs(x - x0) < hw;
                        boolean window = dy >= 4 && dy <= 7 && Math.floorMod(x - x0 + z - z0, 3) == 1;
                        s = door ? AIR : window ? Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState() : dy == h - 1 ? gold : bricks;
                    } else if (col) s = pillar;
                    c.put(x, top + dy, z, s);
                }
                c.put(x, top + h, z, Math.abs(x - x0) == hw + 2 || Math.abs(z - z0) == hd + 2 ? gold : bricks);
            }
        }
        c.gable(x0 - hw - 2, z0 - hd - 2, x0 + hw + 2, z0 + hd + 2, top + h + 1, blueStairs, blueSlab, marble, false);
        // inside
        for (int x = x0 - hw + 4; x <= x0 + hw - 8; x += 4) {
            for (int sz : new int[]{-3, 3}) for (int dy = 0; dy < h; dy++) c.put(x, top + dy, z0 + sz, pillar);
        }
        int far = x0 + (x0 < c.cx ? -hw + 2 : hw - 2), in = x0 < c.cx ? 1 : -1; // the tribunal at the end away from the plaza
        for (int dz = -4; dz <= 4; dz++) {
            for (int k = 0; k < 4; k++) c.put(far + in * k, top, z0 + dz, gold);
            c.put(far + in * 3, top + 1, z0 + dz, Math.abs(dz) % 2 == 0 ? Furniture.desk("spruce", in > 0 ? Direction.EAST : Direction.WEST) : AIR);
        }
        c.put(far + in, top + 1, z0, Furniture.chair("spruce", in > 0 ? Direction.EAST : Direction.WEST));
        c.wallBanner(far - in, top + 5, z0, in > 0 ? Direction.EAST : Direction.WEST, Blocks.BLUE_WALL_BANNER);
        for (int x = x0 - hw + 6; x <= x0 + hw - 10; x += 3) {
            if (Math.abs(x - far) < 6) continue;
            for (int sz : new int[]{-6, -5, 5, 6}) c.put(x, top, z0 + sz, stair(b(SoFEBlocks.AUREUM_MARBLE_BRICK_STAIRS), in > 0 ? Direction.EAST : Direction.WEST));
        }
        for (int x = x0 - hw + 2; x <= x0 + hw - 2; x++) {
            if (Math.abs(x - far) < 5) continue;
            c.put(x, top, z0, Math.floorMod(x, 6) == 0 ? Blocks.POTTED_FERN.defaultBlockState() : CapitalCity.carpet("blue"));
        }
        for (int x = x0 - hw + 6; x <= x0 + hw - 6; x += 10) Architecture.chandelier(c.level, x, top + h - 1, top + h - 4, z0);
        for (int x = x0 - hw + 3; x <= x0 + hw - 3; x += 6) {
            c.wallBanner(x, top + 6, z0 - hd + 1, Direction.SOUTH, Blocks.YELLOW_WALL_BANNER);
            c.wallBanner(x, top + 6, z0 + hd - 1, Direction.NORTH, Blocks.YELLOW_WALL_BANNER);
        }
        c.claim(x0 - hw - 4, z0 - hd - 4, x0 + hw + 4, z0 + hd + 4);
    }

    /**
     * A temple with columns all round on a stepped podium, a blue roof with pediments at both ends, the cella inside
     * with the god's golden statue, an altar and braziers before the steps.
     */
    private void temple(int x0, int z0) {
        int top = c.top, hw = 10, hd = 15, h = 11, rise = 3;
        c.platform(x0, z0, hw + 2, hd + 2, rise, bricks, gold, marble, null, 4, Direction.NORTH);
        int floor = top + rise;
        for (int x = x0 - hw; x <= x0 + hw; x++) {
            for (int z = z0 - hd; z <= z0 + hd; z++) {
                boolean colonnade = (Math.abs(x - x0) == hw || Math.abs(z - z0) == hd) && Math.floorMod(x + z, 3) == 0;
                boolean cella = (Math.abs(x - x0) == hw - 4 && Math.abs(z - z0) <= hd - 4) || (Math.abs(z - z0) == hd - 4 && Math.abs(x - x0) <= hw - 4);
                boolean door = Math.abs(z - z0) == hd - 4 && z < z0 && Math.abs(x - x0) <= 1;
                for (int dy = 0; dy < h; dy++) {
                    c.put(x, floor + dy, z, colonnade ? pillar : cella && !(door && dy < 5) ? (dy == h - 1 ? gold : bricks) : AIR);
                }
                c.put(x, floor + h, z, Math.abs(x - x0) == hw || Math.abs(z - z0) == hd ? gold : bricks);
            }
        }
        c.gable(x0 - hw, z0 - hd, x0 + hw, z0 + hd, floor + h + 1, blueStairs, blueSlab, marble, true);
        for (int dx = -2; dx <= 2; dx += 2) c.put(x0 + dx, floor + h + 4, z0 - hd - 1, gold);
        // the god inside, on a pedestal of gold, between braziers and offerings
        c.put(x0, floor, z0 + hd - 6, goldBlock);
        c.statue(x0, floor + 1, z0 + hd - 6, 180, "gold", null);
        for (int sx : new int[]{-3, 3}) {
            c.brazier(x0 + sx, floor, z0 + hd - 6, bricks);
            c.put(x0 + sx, floor, z0, Blocks.DECORATED_POT.defaultBlockState());
            c.wallBanner(x0 + sx, floor + 6, z0 + hd - 5, Direction.NORTH, Blocks.BLUE_WALL_BANNER);
        }
        for (int z = z0 - hd + 5; z <= z0 + hd - 8; z++) c.put(x0, floor, z, CapitalCity.carpet("red"));
        Architecture.chandelier(c.level, x0, floor + h - 1, floor + h - 4, z0);
        // the altar before the steps
        int az = z0 - hd - 9;
        c.put(x0, top, az, gold);
        c.put(x0, top + 1, az, Blocks.CAMPFIRE.defaultBlockState());
        for (int sx : new int[]{-4, 4}) c.brazier(x0 + sx, top, az, bricks);
        c.claim(x0 - hw - 4, z0 - hd - 12, x0 + hw + 4, z0 + hd + 4);
    }

    /** A little round temple: a ring of columns round a statue, under a white dome. */
    private void tholos(int x0, int z0) {
        c.platform(x0, z0, 8, 8, 2, bricks, gold, marble, null, 2, Direction.NORTH, Direction.SOUTH);
        int r = 6, h = 8, floor = c.top + 2;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.hypot(dx, dz);
                if (d > r + 0.3) continue;
                boolean col = d > r - 1 && Math.abs(Math.sin(Math.atan2(dz, dx) * 5)) < 0.3;
                for (int y = floor; y < floor + h; y++) c.put(x0 + dx, y, z0 + dz, col ? pillar : AIR);
                c.put(x0 + dx, floor + h, z0 + dz, d > r - 1 ? gold : bricks);
            }
        }
        Architecture.dome(c.level, x0, floor + h + 1, z0, r, quartz, Blocks.QUARTZ_BRICKS.defaultBlockState(), goldBlock);
        c.put(x0, floor, z0, gold);
        c.statue(x0, floor + 1, z0, 180, "gold", null);
        c.put(x0, floor + h - 1, z0, c.hanging());
        c.claim(x0 - 10, z0 - 10, x0 + 10, z0 + 10);
    }

    /** The column of victory in the plaza's fountain: a fluted shaft banded with gold, a golden emperor on top. */
    private void column(int x0, int z0) {
        int top = c.top;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
            for (int y = top - 2; y < top + 3; y++) c.put(x0 + dx, y, z0 + dz, y == top + 2 ? gold : bricks);
        }
        for (int dy = 3; dy < 21; dy++) c.put(x0, top + dy, z0, dy % 6 == 2 ? gold : pillar);
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) c.put(x0 + dx, top + 21, z0 + dz, gold);
        c.statue(x0, top + 22, z0, 0, "gold", null);
        for (Direction d : Direction.Plane.HORIZONTAL) { // the water spouts
            c.put(x0 + d.getStepX() * 2, top + 1, z0 + d.getStepZ() * 2, Blocks.WATER.defaultBlockState());
        }
    }

    /** The triumphal arch over the south avenue at the forum: a great arch and two lesser ones, a golden quadriga on top. */
    private void arch(int x0, int z0) {
        int top = c.top, hw = 13, hd = 3, h = 20;
        for (int x = x0 - hw; x <= x0 + hw; x++) {
            for (int z = z0 - hd; z <= z0 + hd; z++) {
                int dx = Math.abs(x - x0);
                for (int dy = 0; dy < h; dy++) {
                    boolean main = dx <= 4 && dy < 13 - (dx == 4 ? 1 : 0);
                    boolean side = dx >= 7 && dx <= 9 && dy < 7 - (dx == 7 || dx == 9 ? 1 : 0);
                    boolean face = Math.abs(z - z0) == hd;
                    BlockState s = main || side ? AIR
                            : face && (dx == 5 || dx == 11) && dy < 14 ? pillar
                            : dy == 14 || dy == h - 1 ? gold
                            : face && dy >= 15 && dy <= 17 && dx <= 8 ? (dx % 3 == 0 ? goldBlock : gold) // the inscription
                            : bricks;
                    c.put(x, top + dy, z, s);
                }
            }
        }
        // the quadriga: a golden charioteer behind four golden horses (blocks of gold)
        for (int dx = -3; dx <= 3; dx += 2) {
            c.put(x0 + dx, top + h, z0 + 1, goldBlock);
            c.put(x0 + dx, top + h + 1, z0 + 1, goldBlock);
            c.put(x0 + dx, top + h + 1, z0 + 2, gold);
        }
        c.statue(x0, top + h, z0 - 1, 180, "gold", null);
        for (int sx : new int[]{-hw, hw}) c.statue(x0 + sx, top + h, z0, 180, "iron", null);
        c.claim(x0 - hw - 2, z0 - hd - 2, x0 + hw + 2, z0 + hd + 2);
    }

    /** Statues of emperors and legionaries along the south avenue between the forum and the gate. */
    private void statues() {
        int half = c.avenueHalf();
        for (int t = 40; t < c.a - 26; t += 18) {
            for (int side : new int[]{-1, 1}) {
                int x = c.cx + side * (half + 1), z = c.cz + t + 6;
                if (!c.level.getBlockState(new BlockPos(x, c.top, z)).isAir()) continue;
                c.statue(x, c.top, z, side > 0 ? 90 : -90, (t / 18) % 2 == 0 ? "gold" : "iron", gold);
            }
        }
    }

    /**
     * The aqueduct: from the hills west of the city on two storeys of arches, over the wall, along the north of the city
     * to the water tower by the palace. Its piers go down to the ground wherever the land falls.
     */
    private void aqueduct(int z0) {
        int top = c.top, channel = top + 18, from = c.cx - c.a - 110, to = c.cx + 22;
        for (int x = from; x <= to; x++) {
            int off = x - c.cx;
            boolean pier = Math.floorMod(off, 6) == 0 && Math.abs(off) > 6;
            boolean inside = Math.abs(off) < c.a && Math.abs(z0 - c.cz) < c.a;
            for (int w = -1; w <= 1; w++) {
                int z = z0 + w;
                int ground = inside ? top - 1 : SultharisBuilder.surfaceY(c.level, x, z) - 1;
                for (int y = ground + 1; y <= channel + 1; y++) {
                    int dy = y - top;
                    BlockState s;
                    if (y == channel) s = w == 0 ? Blocks.WATER.defaultBlockState() : bricks;
                    else if (y == channel + 1) s = w == 0 ? AIR : rail;
                    else if (y == channel - 1) s = gold;
                    else if (pier) s = bricks;
                    else {
                        int k = Math.floorMod(off, 6);
                        boolean lower = dy < 10 - (k == 1 || k == 5 ? 1 : 0); // the arches of the lower storey
                        boolean upper = dy > 10 && dy < 16 - (k == 1 || k == 5 ? 1 : 0);
                        s = lower || upper ? AIR : bricks;
                        if (dy == 10) s = marble;
                    }
                    if (s.isAir() && (inside || y < top)) continue; // the city goes on under the arches; below, only the piers go down
                    c.put(x, y, z, s);
                }
            }
        }
        // the water tower where it ends: a square cistern of marble with a blue roof
        int tx = to + 4;
        for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
            boolean edge = Math.abs(dx) == 3 || Math.abs(dz) == 3;
            for (int y = top; y <= channel + 1; y++) c.put(tx + dx, y, z0 + dz, edge ? (y % 6 == 0 ? gold : bricks) : y >= channel - 2 ? Blocks.WATER.defaultBlockState() : bricks);
        }
        int peak = c.hipRoof(tx - 3, z0 - 3, tx + 3, z0 + 3, channel + 2, blueStairs, blue, 4);
        c.put(tx, peak + 1, z0, goldBlock);
        c.claim(c.cx - c.a, z0 - 4, to + 8, z0 + 4);
    }

    // ------------------------------------------------------------------ the south gate and the fields

    /** The south gate: a gatehouse crested with gold between two towers under blue pyramids, banners over the arch. */
    private void gatehouse() {
        int top = c.top, gz = c.cz + c.a, hw = 13, h = 16;
        for (int x = c.cx - hw; x <= c.cx + hw; x++) {
            for (int z = gz - 4; z <= gz + 4; z++) {
                int dx = Math.abs(x - c.cx);
                for (int dy = 0; dy < h; dy++) {
                    boolean passage = dx <= 3 && dy < 10 - (dx == 3 ? 1 : 0);
                    boolean face = Math.abs(z - gz) == 4;
                    c.put(x, top + dy, z, passage ? AIR : dy == h - 1 || dy == 11 ? gold : face && dx == 5 ? pillar
                            : face && dy == 13 ? blue : bricks);
                }
                if (Math.floorMod(x, 2) == 0) c.put(x, top + h, z, gold);
            }
        }
        for (int side : new int[]{-1, 1}) {
            int tx = c.cx + side * (hw + 2);
            for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
                boolean edge = Math.abs(dx) == 4 || Math.abs(dz) == 4;
                for (int dy = 0; dy < 24; dy++) c.put(tx + dx, top + dy, gz + dz, edge ? (dy % 8 == 7 ? gold
                        : Math.abs(dx) == 4 && Math.abs(dz) == 4 ? pillar : dy % 8 == 4 && (dx == 0 || dz == 0) ? Blocks.WHITE_STAINED_GLASS_PANE.defaultBlockState() : bricks) : AIR);
            }
            int peak = c.hipRoof(tx - 4, gz - 4, tx + 4, gz + 4, top + 24, blueStairs, blue, 9);
            c.put(tx, peak + 1, gz, goldBlock);
            c.put(tx, peak + 2, gz, Blocks.LIGHTNING_ROD.defaultBlockState());
            c.wallBanner(tx, top + 18, gz + 5, Direction.SOUTH, Blocks.BLUE_WALL_BANNER);
            c.wallBanner(tx, top + 18, gz - 5, Direction.NORTH, Blocks.BLUE_WALL_BANNER);
        }
        for (int dx = -2; dx <= 2; dx++) { // banners over the arch, both faces, and the eagle of gold
            c.wallBanner(c.cx + dx, top + 12, gz + 5, Direction.SOUTH, dx == 0 ? Blocks.YELLOW_WALL_BANNER : Blocks.BLUE_WALL_BANNER);
            c.wallBanner(c.cx + dx, top + 12, gz - 5, Direction.NORTH, dx == 0 ? Blocks.YELLOW_WALL_BANNER : Blocks.BLUE_WALL_BANNER);
        }
        for (int side : new int[]{-1, 1}) { // the great banners of Aureum either side of the arch, outside and in
            c.greatBanner(c.cx + side * 7, top + 15, gz + 5, Direction.SOUTH);
            c.greatBanner(c.cx + side * 7, top + 15, gz - 5, Direction.NORTH);
        }
        for (int dx = -2; dx <= 2; dx++) c.put(c.cx + dx, top + h + 1 + (Math.abs(dx) == 2 ? 1 : 0), gz, goldBlock);
        c.put(c.cx, top + h + 2, gz, goldBlock);
        c.statue(c.cx - 6, top + h, gz, 0, "gold", null);
        c.statue(c.cx + 6, top + h, gz, 0, "gold", null);
    }

    /** Fields of wheat and carrots outside the walls where the land is flat and dry, fenced, with a channel of water. */
    private void fields() {
        int out = c.a + CapitalCity.SHORE + 14;
        List<int[]> spots = new ArrayList<>();
        for (int k : new int[]{-80, -40, 40, 80}) {
            spots.add(new int[]{c.cx - out, c.cz + k});
            spots.add(new int[]{c.cx + out, c.cz + k});
            spots.add(new int[]{c.cx + k, c.cz + out});
        }
        for (int[] s : spots) {
            int[] hs = new int[9];
            int i = 0;
            boolean dry = true;
            for (int dx = -9; dx <= 9; dx += 9) for (int dz = -9; dz <= 9; dz += 9) {
                int y = SultharisBuilder.surfaceY(c.level, s[0] + dx, s[1] + dz);
                hs[i++] = y;
                if (!c.level.getFluidState(new BlockPos(s[0] + dx, y - 1, s[1] + dz)).isEmpty()) dry = false;
            }
            java.util.Arrays.sort(hs);
            if (!dry || hs[8] - hs[0] > 3) continue;
            int y = hs[4];
            BlockState crop = (s[0] + s[1]) % 3 == 0 ? Blocks.CARROTS.defaultBlockState() : Blocks.WHEAT.defaultBlockState();
            for (int dx = -10; dx <= 10; dx++) {
                for (int dz = -10; dz <= 10; dz++) {
                    int x = s[0] + dx, z = s[1] + dz;
                    boolean edge = Math.abs(dx) == 10 || Math.abs(dz) == 10;
                    for (int yy = y - 4; yy < y - 1; yy++) if (c.level.getBlockState(new BlockPos(x, yy, z)).isAir()) c.put(x, yy, z, Blocks.DIRT.defaultBlockState());
                    for (int yy = y; yy < y + 8; yy++) c.put(x, yy, z, AIR);
                    if (edge) {
                        c.put(x, y - 1, z, Blocks.GRASS_BLOCK.defaultBlockState());
                        boolean gap = dz == 10 && Math.abs(dx) <= 1;
                        c.put(x, y, z, gap ? AIR : Math.abs(dx) == 10 && Math.abs(dz) == 10 ? Blocks.SPRUCE_LOG.defaultBlockState() : Blocks.SPRUCE_FENCE.defaultBlockState());
                        if (Math.abs(dx) == 10 && Math.abs(dz) == 10) c.put(x, y + 1, z, Blocks.LANTERN.defaultBlockState());
                    } else if (dz == 0) {
                        c.put(x, y - 1, z, Blocks.WATER.defaultBlockState());
                    } else {
                        c.put(x, y - 1, z, Blocks.FARMLAND.defaultBlockState().setValue(net.minecraft.world.level.block.FarmBlock.MOISTURE, 7));
                        c.put(x, y, z, crop.setValue(CropBlock.AGE, 3 + c.random.nextInt(5)));
                    }
                }
            }
            c.put(s[0] + 3, y, s[1] - 3, Blocks.HAY_BLOCK.defaultBlockState());
        }
    }
}
