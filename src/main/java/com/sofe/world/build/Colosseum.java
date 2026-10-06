package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.EnumSet;
import java.util.Set;

import static com.sofe.world.build.SultharisBuilder.set;

/**
 * A Roman amphitheatre after the Flavian one: an ellipse whose outer face is three storeys of arches between engaged
 * columns, each storey closed by a gold cornice, under an attic with windows and masts for the awning; behind the face
 * two vaulted corridors on every storey; the cavea of marble seats rising from the podium round the sand, cut by walkways
 * of royal blue; the emperor's box on the north side under a blue canopy; gates on the long axis (and the south one in a
 * city) through which the fighters come in; and gratings of the cells below the sand.
 * <p>
 * Built from the arena's edge out: for every column the builder finds which of the ellipses between the arena and the
 * outer wall passes through it, so every ring (podium, seats, corridors, face) keeps its width all round.
 */
final class Colosseum {
    /** The marble of a block: intact in a city, partly cracked in the ruined one of the Act IV dungeon. */
    interface Stone {
        BlockState at(int x, int y, int z);
    }

    static final int STOREY = 7, PODIUM = 4, ATTIC = 6;
    private static final net.minecraft.world.level.block.Block FLAG = Furniture.flag("blue", Direction.NORTH).getBlock();

    private final ServerLevel level;
    private final int cx, y0, cz, rx, rz, arx, arz, storeys, height;
    private final double width, seatSlope;
    private final int seatTop, bays;
    private final Set<Direction> gates;
    private final boolean sealed;
    private final int gateHalf, gateHeight; // the passages: 5 by 5 in a city, 3 by 4 where the Sealed Gate closes it
    private final Stone stone, pillar;
    private final BlockState gold = SoFEBlocks.AUREUM_GOLD_MOSAIC.get().defaultBlockState();
    private final BlockState blue = SoFEBlocks.AUREUM_ROYAL_TILES.get().defaultBlockState();
    private final BlockState seat = SoFEBlocks.AUREUM_MARBLE_BRICK_STAIRS.get().defaultBlockState();
    private final BlockState air = Blocks.AIR.defaultBlockState();

    /**
     * @param rx      half the long (x) axis of the outer wall; rz the short one
     * @param arx     half the long axis of the sand; arz the short one
     * @param gates   the ends with a gate (EAST and WEST on the long axis, SOUTH on the short one)
     * @param sealed  the dungeon: the arches of the ground storey are barred, so the only way in is the sealed gate
     */
    private Colosseum(ServerLevel level, int cx, int y0, int cz, int rx, int rz, int arx, int arz, int storeys, Set<Direction> gates,
                      boolean sealed, Stone stone, Stone pillar) {
        this.level = level;
        this.cx = cx;
        this.y0 = y0;
        this.cz = cz;
        this.rx = rx;
        this.rz = rz;
        this.arx = arx;
        this.arz = arz;
        this.storeys = storeys;
        this.height = storeys * STOREY + ATTIC;
        this.width = ((rx - arx) + (rz - arz)) / 2.0;
        this.seatTop = storeys * STOREY - 2; // the highest seats reach the top storey of the corridors
        this.seatSlope = (seatTop - PODIUM) / Math.max(1.0, width - 9.5);
        double h = Math.pow(rx - rz, 2) / Math.pow(rx + rz, 2); // Ramanujan's perimeter of the outer ellipse
        double perimeter = Math.PI * (rx + rz) * (1 + 3 * h / (10 + Math.sqrt(4 - 3 * h)));
        this.bays = Math.max(16, (int) Math.round(perimeter / 5.0)); // one arch every five blocks
        this.gates = gates.isEmpty() ? EnumSet.noneOf(Direction.class) : EnumSet.copyOf(gates);
        this.sealed = sealed;
        this.gateHalf = sealed ? 1 : 2;
        this.gateHeight = sealed ? 4 : 5;
        this.stone = stone;
        this.pillar = pillar;
    }

    static void build(ServerLevel level, int cx, int y0, int cz, int rx, int rz, int arx, int arz, int storeys, Set<Direction> gates,
                      boolean sealed, Stone stone, Stone pillar) {
        Colosseum c = new Colosseum(level, cx, y0, cz, rx, rz, arx, arz, storeys, gates, sealed, stone, pillar);
        c.shell();
        c.box();
        c.lights();
    }

    /** The height of the cavea's seats at a ring. */
    int seatHeight(double u) {
        return PODIUM + (int) Math.floor(Math.max(0, u - 1.5) * seatSlope);
    }

    // ------------------------------------------------------------------ geometry

    /** 0 on the edge of the sand, 1 on the outer wall; below 0 on the sand, above 1 outside. */
    private double ring(int dx, int dz) {
        if (sq(dx, arx) + sq(dz, arz) <= 1) return -1;
        if (sq(dx, rx) + sq(dz, rz) > 1) return 2;
        double lo = 0, hi = 1;
        for (int i = 0; i < 24; i++) {
            double t = (lo + hi) / 2;
            if (sq(dx, arx + t * (rx - arx)) + sq(dz, arz + t * (rz - arz)) > 1) lo = t;
            else hi = t;
        }
        return (lo + hi) / 2;
    }

    private static double sq(double d, double r) {
        return d * d / (r * r);
    }

    /**
     * Where a column falls in its arch: 0 to 5, the pier between 0 and 1.2. The angle is taken on the outer ellipse for
     * every ring, so the piers of the face, the inner arcade and the corridors line up toward the middle.
     */
    private double bayPos(int dx, int dz) {
        double p = (Math.atan2(dz / (double) rz, dx / (double) rx) / (Math.PI * 2) + 1) * bays;
        return (p - Math.floor(p)) * 5;
    }

    private int bayIndex(int dx, int dz) {
        return (int) Math.floor((Math.atan2(dz / (double) rz, dx / (double) rx) / (Math.PI * 2) + 1) * bays) % bays;
    }

    /**
     * How many blocks in from the outside a column is (1 on the face), counted on the square round it, up to 8: the
     * face, the corridors and the inner arcade are rings of whole blocks, closed at the corners.
     */
    private int[][] layers(double[][] rings) {
        int w = rings.length, d = rings[0].length;
        int[][] out = new int[w][d];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < d; j++) {
                out[i][j] = 9;
                if (rings[i][j] > 1.5 || rings[i][j] < 0) continue;
                search:
                for (int k = 1; k <= 8; k++) {
                    for (int a = -k; a <= k; a++) {
                        for (int b = -k; b <= k; b++) {
                            if (Math.max(Math.abs(a), Math.abs(b)) != k) continue;
                            int ii = i + a, jj = j + b;
                            if (ii < 0 || jj < 0 || ii >= w || jj >= d || rings[ii][jj] > 1.5) {
                                out[i][j] = k;
                                break search;
                            }
                        }
                    }
                }
            }
        }
        return out;
    }

    /** The way out from the middle at a column (the side a seat's back faces). */
    private Direction outward(int dx, int dz) {
        double gx = dx / (double) (rx * rx), gz = dz / (double) (rz * rz);
        return Math.abs(gx) > Math.abs(gz) ? (gx > 0 ? Direction.EAST : Direction.WEST) : (gz > 0 ? Direction.SOUTH : Direction.NORTH);
    }

    /** Inside a gate's passage: the long axis for EAST and WEST, the short one for NORTH and SOUTH. */
    private boolean inGate(int dx, int dz) {
        for (Direction d : gates) {
            if (d.getAxis() == Direction.Axis.X && Math.abs(dz) <= gateHalf && Integer.signum(dx) == d.getStepX()) return true;
            if (d.getAxis() == Direction.Axis.Z && Math.abs(dx) <= gateHalf && Integer.signum(dz) == d.getStepZ()) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------ the build

    private void shell() {
        double[][] rings = new double[2 * rx + 3][2 * rz + 3];
        for (int dx = -rx - 1; dx <= rx + 1; dx++) for (int dz = -rz - 1; dz <= rz + 1; dz++) rings[dx + rx + 1][dz + rz + 1] = ring(dx, dz);
        int[][] layer = layers(rings);
        for (int dx = -rx - 1; dx <= rx + 1; dx++) {
            for (int dz = -rz - 1; dz <= rz + 1; dz++) {
                double t = rings[dx + rx + 1][dz + rz + 1];
                if (t > 1.5) continue;
                int x = cx + dx, z = cz + dz;
                if (t < 0) {
                    sand(x, z, dx, dz);
                    continue;
                }
                double u = t * width;
                boolean gate = inGate(dx, dz);
                double pos = bayPos(dx, dz);
                int bay = bayIndex(dx, dz);
                int l = layer[dx + rx + 1][dz + rz + 1];
                set(level, x, y0 - 1, z, gate ? stone.at(x, y0 - 1, z) : SoFEBlocks.AUREUM_POLISHED_MARBLE.get().defaultBlockState());
                for (int h = 0; h <= height + 4; h++) {
                    BlockState s = at(x, z, dx, dz, u, l, h, pos, bay);
                    if (gate && h < gateHeight) s = air; // the passage of the fighters
                    else if (gate && h == gateHeight && s.isAir() && l > 1) s = stone.at(x, y0 + h, z);
                    set(level, x, y0 + h, z, s);
                    if (s.getBlock() == FLAG) EmpireFlags.decorate(level, new net.minecraft.core.BlockPos(x, y0 + h, z), CapitalCity.Empire.AUREUM);
                }
            }
        }
    }

    /** The sand, and the gratings over the cells where the beasts wait. */
    private void sand(int x, int z, int dx, int dz) {
        for (int h = 0; h <= height + 4; h++) set(level, x, y0 + h, z, air);
        boolean grate = (Math.abs(dz) == 3 || Math.abs(dx) == arx / 2) && Math.abs(dx) < arx - 6 && Math.abs(dz) < arz - 4
                && Math.floorMod(dx + dz, 4) == 0;
        if (grate) {
            set(level, x, y0 - 2, z, stone.at(x, y0 - 2, z));
            set(level, x, y0 - 1, z, Blocks.SPRUCE_TRAPDOOR.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP));
        } else {
            set(level, x, y0 - 1, z, Blocks.SAND.defaultBlockState());
            set(level, x, y0 - 2, z, stone.at(x, y0 - 2, z));
        }
    }

    private BlockState at(int x, int z, int dx, int dz, double u, int l, int h, double pos, int bay) {
        int y = y0 + h;
        // the podium round the sand: a wall with a gold coping and a marble railing
        if (u < 1.5) {
            if (h < PODIUM - 1) return stone.at(x, y, z);
            if (h == PODIUM - 1) return gold;
            if (h == PODIUM) return SoFEBlocks.AUREUM_MARBLE_BRICK_WALL.get().defaultBlockState();
            return air;
        }
        // the cavea: a seat on every ring, a walkway of royal blue every eighth
        if (l >= 8) {
            int row = (int) Math.floor(u - 1.5), top = seatHeight(u);
            if (h < top) return stone.at(x, y, z);
            if (h == top) {
                if (row % 8 == 7) return blue;
                if (Math.floorMod(bay, 12) == 0 && pos < 1.6) { // an aisle of steps going up
                    return SoFEBlocks.AUREUM_MARBLE_BRICK_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING, outward(dx, dz));
                }
                return seat.setValue(StairBlock.FACING, outward(dx, dz));
            }
            return air;
        }
        boolean pier = pos < 1.2;
        // the inner wall of the corridors: solid under the top seats, a colonnade above them
        if (l == 7) {
            if (h < seatTop + 1) return stone.at(x, y, z);
            if (h < height - 1) return pier ? pillarY(x, y, z) : air;
            if (h == height - 1) return stone.at(x, y, z);
            return h == height && pier ? SoFEBlocks.AUREUM_MARBLE_BRICK_WALL.get().defaultBlockState() : air;
        }
        // the corridors (rings 2-3 and 5-6): a vaulted floor on every storey
        if (l != 1 && l != 4) {
            if (h < height - 1 && h % STOREY == STOREY - 1 && h / STOREY < storeys) return stone.at(x, y, z);
            if (h == height - 1) return Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP);
            return air;
        }
        // the face (ring 1) with engaged columns and gold cornices, and the plain inner arcade (ring 4)
        boolean outer = l == 1;
        int storey = h / STOREY, hl = h % STOREY;
        if (storey < storeys) {
            if (hl == STOREY - 1) return outer ? gold : stone.at(x, y, z);
            boolean opening = pos >= 1.5 && pos < 4.5 && hl <= 4 || pos >= 2.2 && pos < 3.8 && hl == 5;
            if (opening) {
                if (storey == 0 && sealed) return outer ? Blocks.IRON_BARS.defaultBlockState() : air;
                return air;
            }
            return outer && pier ? pillarY(x, y, z) : stone.at(x, y, z);
        }
        int ha = h - storeys * STOREY; // the attic
        if (ha < ATTIC - 1) {
            if (outer && pier) return pillarY(x, y, z);
            if (outer && bay % 2 == 0 && (ha == 2 || ha == 3) && pos >= 2.2 && pos < 3.8) return air; // its little windows
            return stone.at(x, y, z);
        }
        if (ha == ATTIC - 1) return outer ? gold : stone.at(x, y, z);
        // the masts of the awning, on every other pier, and a crest between them
        if (outer && pier && bay % 2 == 0) {
            if (ha <= ATTIC + 2) return Blocks.SPRUCE_FENCE.defaultBlockState();
            if (ha == ATTIC + 3) return Furniture.flag("blue", outward(dx, dz));
            return air;
        }
        if (outer && ha == ATTIC) return SoFEBlocks.AUREUM_MARBLE_BRICK_WALL.get().defaultBlockState();
        return air;
    }

    private BlockState pillarY(int x, int y, int z) {
        BlockState p = pillar.at(x, y, z);
        return p.hasProperty(RotatedPillarBlock.AXIS) ? p.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y) : p;
    }

    /**
     * The emperor's box on the north side: a gold floor over the first rows of seats, a blue carpet, a throne under a
     * canopy of blue and gold on four columns, and blue banners down the podium.
     */
    private void box() {
        int z0 = cz - arz, half = 5;
        int floor = y0 + PODIUM + 2;
        for (int dx = -half; dx <= half; dx++) {
            for (int k = 0; k <= 6; k++) {
                int x = cx + dx, z = z0 - 1 - k;
                for (int y = y0; y < floor; y++) set(level, x, y, z, stone.at(x, y, z));
                boolean edge = Math.abs(dx) == half || k == 0;
                set(level, x, floor, z, edge ? gold : Blocks.BLUE_CARPET.defaultBlockState());
                if (edge) set(level, x, floor - 1, z, gold);
                else set(level, x, floor - 1, z, SoFEBlocks.AUREUM_POLISHED_MARBLE.get().defaultBlockState());
                for (int y = floor + 1; y <= floor + 7; y++) set(level, x, y, z, air);
                if (k == 0 && dx % 2 == 0) set(level, x, floor + 1, z, SoFEBlocks.AUREUM_MARBLE_BRICK_WALL.get().defaultBlockState());
                if (Math.abs(dx) == half && (k == 1 || k == 6)) {
                    for (int y = floor + 1; y <= floor + 5; y++) set(level, x, y, z, pillarY(x, y, z));
                }
                set(level, x, floor + 6, z, edge || k == 6 ? gold : blue); // the canopy
            }
        }
        int tz = z0 - 6; // the throne, looking south over the sand
        set(level, cx, floor, tz, Blocks.GOLD_BLOCK.defaultBlockState());
        set(level, cx, floor + 1, tz, Blocks.QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
        set(level, cx, floor + 2, tz - 1, Blocks.GOLD_BLOCK.defaultBlockState());
        set(level, cx, floor + 1, tz - 1, Blocks.GOLD_BLOCK.defaultBlockState());
        for (int sx : new int[]{-1, 1}) {
            set(level, cx + sx, floor + 1, tz, Blocks.QUARTZ_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
            set(level, cx + sx * 3, floor + 1, tz + 2, Blocks.POTTED_FLOWERING_AZALEA.defaultBlockState());
        }
        for (int dx = -half + 1; dx <= half - 1; dx += 2) { // banners down the podium, over the sand
            EmpireFlags.banner(level, cx + dx, y0 + PODIUM - 1, z0, CapitalCity.Empire.AUREUM, Direction.SOUTH);
        }
    }

    /** Lanterns on the podium railing and hanging in the corridors. */
    private void lights() {
        BlockState lantern = Blocks.LANTERN.defaultBlockState();
        BlockState hanging = lantern.setValue(LanternBlock.HANGING, true);
        int n = bays;
        for (int i = 0; i < n; i += 2) {
            double theta = (i + 0.5) / n * Math.PI * 2;
            // on the railing
            int px = cx + (int) Math.round(Math.cos(theta) * (arx + 0.6)), pz = cz + (int) Math.round(Math.sin(theta) * (arz + 0.6));
            if (!inGate(px - cx, pz - cz) && level.getBlockState(new net.minecraft.core.BlockPos(px, y0 + PODIUM, pz)).getBlock()
                    instanceof net.minecraft.world.level.block.WallBlock) {
                set(level, px, y0 + PODIUM + 1, pz, lantern);
            }
            // in the corridors, under each storey's vault
            int qx = cx + (int) Math.round(Math.cos(theta) * (rx - 2.5)), qz = cz + (int) Math.round(Math.sin(theta) * (rz - 2.5));
            for (int s = 0; s < storeys; s++) {
                int y = y0 + s * STOREY + STOREY - 2;
                if (level.getBlockState(new net.minecraft.core.BlockPos(qx, y, qz)).isAir()) set(level, qx, y, qz, hanging);
            }
        }
    }
}
