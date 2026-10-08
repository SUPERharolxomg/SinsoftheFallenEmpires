package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

import static com.sofe.world.build.SultharisBuilder.set;

/**
 * The bosses' places that are no islands (the user's concepts): the Caverns of Nordrath, Fenrath's lair of ice and
 * basalt; the Feast Halls under them, Gularth's eternal feast; the Celestial Spire over Sulthari, where Prython waits on
 * its summit; and the Inverted Throne beneath the city of ash, where Nahrazel sits among the seven pillars of the sins.
 * Each keeps its shape, its gates and its boss's floor (EmpireBuilder); these are their dress, outside and in.
 */
final class DeepHalls {
    private DeepHalls() {
    }

    private static BlockState b(Block block) {
        return block.defaultBlockState();
    }

    /** The ground at the Caverns' entrance, south of its arch: where its tunnel starts, whatever stands over the arch. */
    static int cavernsGround(ServerLevel level, StructurePositions.Structure caverns) {
        int z = caverns.z() + SultharisBuilder.half(caverns.sizeZ()) + EmpireBuilder.TUNNEL;
        return SultharisBuilder.surfaceY(level, caverns.x(), z + 10);
    }

    /** The floor of the tunnel at step i from the arch (the same in every builder that touches it). */
    static int tunnelFloor(int top, int i) {
        return top - (int) Math.round((double) EmpireBuilder.CAVERNS_DEPTH * i / (EmpireBuilder.TUNNEL + 1));
    }

    // ------------------------------------------------------------------------------------------------ the Caverns' entrance

    /**
     * The way down to the Caverns: a crag of deepslate and basalt with an iron-bound door carved in its face, spires of
     * ice on either side, the Devourer's skull and crossed bones over it, bones and the dead of the hunts on the snow, and
     * a broken bridge over a chasm beside the path. The tunnel behind the door goes down into the crag.
     */
    static void cavernsEntrance(ServerLevel level, StructurePositions.Structure s) {
        var caverns = StructurePositions.get().structure("sofe:nordrath/caverns").orElse(null);
        int top = caverns != null ? cavernsGround(level, caverns) : SultharisBuilder.surfaceY(level, s.x(), s.z() + 10);
        int cx = s.x(), z0 = s.z(); // the door's line; the crag stands north of it
        Random random = new Random(cx * 7L + z0);
        SultharisBuilder.pad(level, cx - 12, z0, cx + 12, z0 + 12, top, b(Blocks.SNOW_BLOCK), 10);
        // the crag
        for (int x = cx - 18; x <= cx + 18; x++) {
            for (int z = z0 - 22; z <= z0 - 1; z++) {
                double dx = (x - cx) / 18.0, dz = (z - (z0 - 8)) / 14.0;
                double r = Math.sqrt(dx * dx + dz * dz) + 0.08 * Precinct.patches(x * 3, z * 3);
                if (r > 1 && z < z0 - 2) continue;
                int h = (int) Math.round(22 * Math.max(0.3, 1 - r * r)) + (int) Math.round(3 * Precinct.patches(x * 5, z * 5));
                if (Math.abs(x - cx) <= 7) h = Math.max(h, 14);
                for (int y = top - 3; y < top + h; y++) {
                    double n = Precinct.patches(x + y * 2, z - y);
                    set(level, x, y, z, y == top + h - 1 && random.nextInt(3) > 0 ? b(Blocks.SNOW_BLOCK) : n > 0.35 ? b(Blocks.BASALT) : n < -0.4 ? b(Blocks.COBBLED_DEEPSLATE) : b(Blocks.DEEPSLATE));
                }
            }
        }
        // the face: a frame of dark stone, the door's lintel, the skull and crossed bones
        BlockState frame = b(Blocks.POLISHED_DEEPSLATE), dark = b(Blocks.DEEPSLATE_BRICKS);
        for (int x = cx - 5; x <= cx + 5; x++) for (int dy = 0; dy <= 9; dy++) {
            boolean edge = Math.abs(x - cx) >= 4 || dy >= 6;
            set(level, x, top + dy, z0, edge ? (Math.abs(x - cx) == 5 || dy == 9 ? frame : dark) : b(Blocks.AIR));
            if (Math.abs(x - cx) >= 4 || dy >= 7) set(level, x, top + dy, z0 + 1, Math.abs(x - cx) == 5 ? frame : b(Blocks.AIR));
        }
        for (int x = cx - 3; x <= cx + 3; x++) set(level, x, top + 6, z0 + 1, b(Blocks.DEEPSLATE_TILE_STAIRS).setValue(StairBlock.FACING, Direction.NORTH)
                .setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP));
        int sy = top + 10; // the skull: bone, with eyes of soul fire and crossed bones behind it
        for (int dx = -2; dx <= 2; dx++) for (int dy = 0; dy <= 3; dy++) {
            if (Math.abs(dx) == 2 && dy == 3) continue;
            set(level, cx + dx, sy + dy, z0 + 1, b(Blocks.BONE_BLOCK));
        }
        set(level, cx - 1, sy + 2, z0 + 1, b(Blocks.SOUL_LANTERN));
        set(level, cx + 1, sy + 2, z0 + 1, b(Blocks.SOUL_LANTERN));
        set(level, cx, sy, z0 + 1, b(Blocks.POLISHED_DEEPSLATE));
        for (int i = -4; i <= 4; i++) {
            set(level, cx + i, sy + 2 + i / 2, z0, b(Blocks.BONE_BLOCK).setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            set(level, cx + i, sy + 2 - i / 2, z0, b(Blocks.BONE_BLOCK).setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        }
        // spires of ice on either side of the door
        for (int side : new int[]{-1, 1}) {
            for (int k = 0; k < 3; k++) {
                int x = cx + side * (7 + k * 2), z = z0 + 1 + (k % 2), h = 10 - k * 3 + random.nextInt(3);
                for (int dy = 0; dy < h; dy++) {
                    set(level, x, top + dy, z, dy < h - 2 ? b(Blocks.PACKED_ICE) : b(Blocks.BLUE_ICE));
                    if (dy < h / 2) set(level, x + side, top + dy, z, b(Blocks.PACKED_ICE));
                }
            }
        }
        // the dead of the hunts and their bones on the snow
        for (int i = 0; i < 16; i++) {
            int x = cx - 11 + random.nextInt(23), z = z0 + 2 + random.nextInt(10);
            if (Math.abs(x - cx) <= 1) continue;
            set(level, x, top, z, random.nextInt(3) == 0 ? b(Blocks.SKELETON_SKULL) : b(Blocks.BONE_BLOCK).setValue(RotatedPillarBlock.AXIS,
                    random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z));
        }
        for (int[] c : new int[][]{{cx - 3, z0 + 4}, {cx + 3, z0 + 4}, {cx - 3, z0 + 9}, {cx + 3, z0 + 9}}) {
            set(level, c[0], top, c[1], frame);
            set(level, c[0], top + 1, c[1], SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
        }
        // the chasm beside the path, and the broken bridge over it
        for (int x = cx + 13; x <= cx + 19; x++) for (int z = z0 + 2; z <= z0 + 14; z++) for (int y = top - 14; y < top + 3; y++) set(level, x, y, z, b(Blocks.AIR));
        for (int x = cx + 12; x <= cx + 20; x++) {
            if (x == cx + 16 || x == cx + 17) continue; // broken in the middle
            for (int dz = 0; dz <= 2; dz++) set(level, x, top - 1, z0 + 7 + dz, dz == 1 ? b(Blocks.SPRUCE_PLANKS) : b(Blocks.SPRUCE_SLAB));
        }
        // the tunnel's mouth, carved anew through the crag (the Caverns' builder carves the rest)
        for (int i = 0; i <= 24; i++) {
            int z = z0 - i, floor = tunnelFloor(top, i);
            for (int dx = -2; dx <= 2; dx++) {
                set(level, cx + dx, floor - 1, z, Math.abs(dx) == 2 ? b(Blocks.SMOOTH_BASALT) : b(Blocks.POLISHED_BLACKSTONE_BRICKS));
                for (int h = 0; h < 5; h++) set(level, cx + dx, floor + h, z, b(Blocks.AIR));
            }
            if (i % 6 == 3) set(level, cx - 2, floor + 2, z, b(Blocks.SOUL_LANTERN));
        }
        StructurePositions.get().gates().values().stream()
                .filter(g -> Math.abs(g.x() - cx) <= 4 && Math.abs(g.z() - z0) <= 4)
                .forEach(g -> StructureBuilder.placeGate(level, g, top));
    }

    // ------------------------------------------------------------------------------------------------ the Caverns

    /**
     * Fenrath's lair, after the cave hall is dug (EmpireBuilder.caverns): a floor of snow, ice and basalt, stalactites of
     * ice hanging from the roof and pillars of ice, pools of green acid in rims of rock, the bones of the Devourer's prey,
     * and its nest of branches and eggs at the back.
     */
    static void caverns(ServerLevel level, StructurePositions.Structure s, int cy) {
        Random random = new Random(s.x() * 13L + s.z());
        int rx = SultharisBuilder.half(s.sizeX()) - 1, rz = SultharisBuilder.half(s.sizeZ()) - 1;
        for (int dx = -rx; dx <= rx; dx++) {
            for (int dz = -rz; dz <= rz; dz++) {
                double e = (dx * dx) / (double) (rx * rx) + (dz * dz) / (double) (rz * rz);
                if (e > 1) continue;
                int x = s.x() + dx, z = s.z() + dz;
                BlockState under = level.getBlockState(new BlockPos(x, cy - 1, z));
                if (under.is(Blocks.WATER)) continue; // the acid pools stay
                double n = Precinct.patches(x * 2, z * 2);
                set(level, x, cy - 1, z, n > 0.3 ? b(Blocks.PACKED_ICE) : n > -0.1 ? b(Blocks.SNOW_BLOCK) : n > -0.45 ? b(Blocks.BLUE_ICE) : b(Blocks.SMOOTH_BASALT));
                int ceiling = (int) Math.round(14 * (1 - e)) + 4;
                if (random.nextInt(9) == 0 && ceiling > 7) { // stalactites of ice
                    int len = 2 + random.nextInt(Math.max(1, ceiling / 2));
                    for (int h = 1; h <= len; h++) set(level, x, cy + ceiling - h, z, h == len ? b(Blocks.BLUE_ICE) : b(Blocks.PACKED_ICE));
                }
            }
        }
        for (int i = 0; i < 7; i++) { // pillars of ice from floor to roof
            double a = i * Math.PI * 2 / 7 + 0.3, d = 0.55;
            int x = s.x() + (int) Math.round(Math.cos(a) * rx * d), z = s.z() + (int) Math.round(Math.sin(a) * rz * d);
            for (int h = 0; h < 20; h++) {
                BlockPos p = new BlockPos(x, cy + h, z);
                if (!level.getBlockState(p).isAir() && h > 2) break;
                set(level, x, cy + h, z, h % 5 == 4 ? b(Blocks.BLUE_ICE) : b(Blocks.PACKED_ICE));
                if (h < 4) set(level, x + 1, cy + h, z, b(Blocks.PACKED_ICE));
            }
        }
        for (int i = 0; i < 4; i++) { // pools of acid in rims of rock
            double a = i * Math.PI / 2 + 0.8;
            int px = s.x() + (int) Math.round(Math.cos(a) * rx * 0.7), pz = s.z() + (int) Math.round(Math.sin(a) * rz * 0.7);
            for (int dx = -3; dx <= 3; dx++) for (int dz = -3; dz <= 3; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 3.4) continue;
                if (d > 2.4) set(level, px + dx, cy, pz + dz, b(Blocks.COBBLED_DEEPSLATE));
                else {
                    set(level, px + dx, cy - 2, pz + dz, b(Blocks.VERDANT_FROGLIGHT));
                    set(level, px + dx, cy - 1, pz + dz, b(Blocks.SLIME_BLOCK));
                    set(level, px + dx, cy, pz + dz, b(Blocks.AIR));
                }
            }
        }
        for (int i = 0; i < 40; i++) { // the bones of its prey
            int x = s.x() - rx + 2 + random.nextInt(2 * rx - 3), z = s.z() - rz + 2 + random.nextInt(2 * rz - 3);
            if (!level.getBlockState(new BlockPos(x, cy, z)).isAir() || level.getBlockState(new BlockPos(x, cy - 1, z)).is(Blocks.SLIME_BLOCK)) continue;
            set(level, x, cy, z, random.nextInt(4) == 0 ? b(Blocks.SKELETON_SKULL) : b(Blocks.BONE_BLOCK).setValue(RotatedPillarBlock.AXIS,
                    random.nextBoolean() ? Direction.Axis.X : Direction.Axis.Z));
        }
        // the nest at the back: a ring of branches and its eggs
        int nx = s.x() + rx / 2, nz = s.z() - rz + 6;
        for (int dx = -4; dx <= 4; dx++) for (int dz = -4; dz <= 4; dz++) {
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > 4.4) continue;
            set(level, nx + dx, cy - 1, nz + dz, b(Blocks.COARSE_DIRT));
            if (d > 2.6) {
                set(level, nx + dx, cy, nz + dz, b(Blocks.SPRUCE_LOG).setValue(RotatedPillarBlock.AXIS, Math.abs(dx) > Math.abs(dz) ? Direction.Axis.Z : Direction.Axis.X));
                if (random.nextBoolean()) set(level, nx + dx, cy + 1, nz + dz, b(Blocks.DEAD_BUSH));
            } else if (d < 2) set(level, nx + dx, cy, nz + dz, random.nextInt(3) == 0 ? b(Blocks.SNIFFER_EGG) : b(Blocks.HAY_BLOCK));
        }
    }

    // ------------------------------------------------------------------------------------------------ the Feast Halls

    /**
     * Gularth's eternal feast, in the hall EmpireBuilder.feastHalls digs: a vat of green bile sunk in the middle with
     * chains over it, cauldrons on fires steaming in the corners, the long tables heaped with food, shelves of food along
     * the walls, meat hung from hooks under the iron trusses of the roof, and his boar-skull sign over the south door.
     */
    static void feastHalls(ServerLevel level, StructurePositions.Structure s, int cy, int x0, int z0, int x1, int z1) {
        Random random = new Random(s.x() * 23L + s.z());
        int cx = s.x(), cz = s.z();
        // the vat of bile in the middle
        for (int dx = -5; dx <= 5; dx++) for (int dz = -5; dz <= 5; dz++) {
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > 5.4) continue;
            if (d > 4.4) set(level, cx + dx, cy, cz + dz, b(Blocks.MOSSY_COBBLESTONE));
            else {
                set(level, cx + dx, cy - 3, cz + dz, b(Blocks.VERDANT_FROGLIGHT));
                set(level, cx + dx, cy - 2, cz + dz, b(Blocks.SLIME_BLOCK));
                set(level, cx + dx, cy - 1, cz + dz, b(Blocks.LIME_STAINED_GLASS));
            }
        }
        for (int[] c : new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}, {0, 0}}) for (int h = 9; h >= 4; h--) set(level, cx + c[0], cy + h, cz + c[1], b(Blocks.CHAIN));
        set(level, cx, cy + 3, cz, b(Blocks.CAULDRON));
        // the cauldrons on their fires, steaming, in the corners
        for (int[] c : new int[][]{{x0 + 4, z0 + 4}, {x1 - 4, z0 + 4}, {x0 + 4, z1 - 4}, {x1 - 4, z1 - 4}}) {
            for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) {
                set(level, c[0] + dx, cy, c[1] + dz, b(Blocks.CAMPFIRE));
                boolean rim = Math.abs(dx) == 1 || Math.abs(dz) == 1;
                set(level, c[0] + dx, cy + 1, c[1] + dz, rim ? b(Blocks.TERRACOTTA) : b(Blocks.LIME_STAINED_GLASS));
                if (rim) set(level, c[0] + dx, cy + 2, c[1] + dz, b(Blocks.ORANGE_TERRACOTTA));
            }
        }
        // the tables heaped with food
        BlockState[] food = {b(Blocks.CAKE), b(Blocks.MELON), b(Blocks.PUMPKIN), b(Blocks.HAY_BLOCK), b(Blocks.HONEY_BLOCK), b(Blocks.RED_MUSHROOM_BLOCK),
                b(Blocks.BROWN_MUSHROOM_BLOCK), b(Blocks.SWEET_BERRY_BUSH), b(Blocks.DRIED_KELP_BLOCK), b(Blocks.BONE_BLOCK)};
        for (int x : new int[]{cx - 8, cx + 8}) for (int z = z0 + 5; z < z1 - 5; z++) {
            if (random.nextInt(4) > 0) set(level, x, cy + 1, z, food[random.nextInt(food.length)]);
        }
        // shelves of food along the long walls
        for (int z = z0 + 2; z <= z1 - 2; z++) for (int x : new int[]{x0 + 1, x1 - 1}) {
            if (Math.floorMod(z - z0, 6) == 0) continue;
            for (int h = 0; h < 3; h++) set(level, x, cy + h * 2, z, random.nextInt(3) == 0 ? food[random.nextInt(food.length)] : b(Blocks.BARREL));
            set(level, x, cy + 1, z, b(Blocks.SPRUCE_SLAB));
        }
        // the trusses of the roof and the meat on their hooks
        for (int x = x0 + 1; x < x1; x++) for (int z : new int[]{z0 + 8, cz, z1 - 8}) set(level, x, cy + 9, z, b(Blocks.IRON_BARS));
        for (int i = 0; i < 16; i++) {
            int x = x0 + 3 + random.nextInt(x1 - x0 - 5), z = z0 + 3 + random.nextInt(z1 - z0 - 5);
            if (Math.abs(x - cx) <= 6 && Math.abs(z - cz) <= 6) continue;
            for (int h = 9; h > 6; h--) set(level, x, cy + h, z, b(Blocks.CHAIN));
            set(level, x, cy + 6, z, random.nextBoolean() ? b(Blocks.RED_WOOL) : b(Blocks.PINK_WOOL)); // the meat
            set(level, x, cy + 5, z, b(Blocks.RED_WOOL));
        }
        // the boar's skull over the south door, inside the hall
        int sy = cy + 5;
        for (int dx = -2; dx <= 2; dx++) for (int dy = 0; dy <= 2; dy++) set(level, cx + dx, sy + dy, z1 - 1, b(Blocks.BONE_BLOCK));
        set(level, cx - 1, sy + 1, z1 - 1, b(Blocks.REDSTONE_BLOCK));
        set(level, cx + 1, sy + 1, z1 - 1, b(Blocks.REDSTONE_BLOCK));
        set(level, cx - 3, sy, z1 - 1, b(Blocks.BONE_BLOCK));
        set(level, cx + 3, sy, z1 - 1, b(Blocks.BONE_BLOCK)); // the tusks
        set(level, cx - 3, sy + 1, z1 - 1, b(Blocks.BONE_BLOCK));
        set(level, cx + 3, sy + 1, z1 - 1, b(Blocks.BONE_BLOCK));
    }

    /** The Feast Halls' door in the Caverns: a frame of timber with its boar's skull sign, and falls of water on either side. */
    static void feastDoor(ServerLevel level, int x, int cy, int z) {
        BlockState timber = SoFEBlocks.NORDRATH_DARK_TIMBER.get().defaultBlockState();
        for (int dx = -4; dx <= 4; dx++) for (int dy = 0; dy <= 7; dy++) {
            boolean frame = Math.abs(dx) >= 3 || dy >= 5;
            if (frame) set(level, x + dx, cy + dy, z, Math.abs(dx) == 4 || dy == 7 ? timber : b(Blocks.SPRUCE_PLANKS));
        }
        for (int dx = -1; dx <= 1; dx++) for (int dy = 0; dy <= 2; dy++) set(level, x + dx, cy + 6 + dy, z + 1, b(Blocks.BONE_BLOCK));
        set(level, x, cy + 7, z + 1, b(Blocks.REDSTONE_BLOCK));
        for (int side : new int[]{-3, 3}) set(level, x + side, cy + 2, z + 1, SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
    }

    // ------------------------------------------------------------------------------------------------ the Celestial Spire

    /**
     * Prython's spire, on the tower EmpireBuilder.spire raises: rings of gold orbiting it at three heights, bands of gold
     * up its wall, a golden portal at its door with its emblem and statues, a spiral of light round the outside, an
     * inverted cone of white stone under the summit, and on the summit a disc of marble inlaid with circles of gold,
     * kneeling statues of the Pride and pillars of gold round it.
     */
    static void spire(ServerLevel level, StructurePositions.Structure s, int ground, int top) {
        int cx = s.x(), cz = s.z(), r = 8;
        BlockState gold = b(Blocks.GOLD_BLOCK), quartz = b(Blocks.SMOOTH_QUARTZ), white = b(Blocks.QUARTZ_BRICKS);
        // the rings of gold orbiting the tower
        for (int[] ring : new int[][]{{ground + 40, 14}, {ground + 80, 16}, {top - 30, 13}}) {
            for (int a = 0; a < 360; a += 2) {
                double t = Math.toRadians(a);
                int x = cx + (int) Math.round(Math.cos(t) * ring[1]), z = cz + (int) Math.round(Math.sin(t) * ring[1]);
                set(level, x, ring[0], z, gold);
                set(level, cx + (int) Math.round(Math.cos(t) * (ring[1] - 1)), ring[0], cz + (int) Math.round(Math.sin(t) * (ring[1] - 1)), quartz);
            }
            for (int i = 0; i < 4; i++) { // spokes of gold from the tower to the ring
                double t = i * Math.PI / 2 + Math.PI / 4;
                for (int d = r + 1; d < ring[1]; d++) set(level, cx + (int) Math.round(Math.cos(t) * d), ring[0], cz + (int) Math.round(Math.sin(t) * d), b(Blocks.GOLD_BLOCK));
            }
        }
        // the spiral of light round the outside
        for (int y = ground + 4; y < top; y++) {
            double t = (y - ground) * Math.PI / 14;
            set(level, cx + (int) Math.round(Math.cos(t) * (r + 2)), y, cz + (int) Math.round(Math.sin(t) * (r + 2)),
                    (y - ground) % 3 == 0 ? b(Blocks.SEA_LANTERN) : b(Blocks.WHITE_STAINED_GLASS));
        }
        // the inverted cone under the summit
        for (int dy = 1; dy <= 18; dy++) {
            int rr = 16 - dy * 16 / 18;
            for (int dx = -rr; dx <= rr; dx++) for (int dz = -rr; dz <= rr; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > rr + 0.5 || d <= r + 0.5) continue; // the tower runs through it
                set(level, cx + dx, top - dy, cz + dz, dy % 6 == 0 ? gold : white);
            }
        }
        // the summit: circles of gold in the marble, kneeling statues, pillars of gold
        int pr = 16;
        for (int dx = -pr; dx <= pr; dx++) for (int dz = -pr; dz <= pr; dz++) {
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > pr || dx == 0 && dz == 0) continue;
            boolean ring = Math.abs(d - 5) < 0.5 || Math.abs(d - 10) < 0.5 || Math.abs(d - 14) < 0.5;
            boolean ray = d > 5 && d < 14 && (dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz));
            if (ring || ray) set(level, cx + dx, top, cz + dz, gold);
        }
        for (int i = 0; i < 8; i++) { // statues of Pride kneeling to the middle
            double t = i * Math.PI / 4 + Math.PI / 8;
            int x = cx + (int) Math.round(Math.cos(t) * 12), z = cz + (int) Math.round(Math.sin(t) * 12);
            set(level, x, top + 1, z, b(Blocks.CHISELED_QUARTZ_BLOCK));
            set(level, x, top + 2, z, quartz);
            set(level, x, top + 3, z, b(Blocks.GOLD_BLOCK));
        }
        // the golden portal at the door, its emblem and its statues
        int dz0 = cz + r + 1;
        for (int dx = -3; dx <= 3; dx++) for (int dy = 0; dy <= 6; dy++) {
            boolean frame = Math.abs(dx) >= 2 || dy >= 4;
            if (frame) set(level, cx + dx, ground + dy, dz0, Math.abs(dx) == 3 || dy == 6 ? gold : white);
        }
        set(level, cx, ground + 7, dz0, b(Blocks.GOLD_BLOCK));
        set(level, cx, ground + 8, dz0, b(Blocks.GLOWSTONE));
        for (int side : new int[]{-5, 5}) {
            set(level, cx + side, ground, dz0 + 2, b(Blocks.CHISELED_QUARTZ_BLOCK));
            set(level, cx + side, ground + 1, dz0 + 2, quartz);
            set(level, cx + side, ground + 2, dz0 + 2, quartz);
            set(level, cx + side, ground + 3, dz0 + 2, b(Blocks.GOLD_BLOCK));
        }
    }

    // ------------------------------------------------------------------------------------------------ the Inverted Throne

    /** The colours of the seven sins, in the pillars about Nahrazel: their stone, their glass and their light. */
    private static final Block[][] SINS = {
            {Blocks.RED_CONCRETE, Blocks.RED_STAINED_GLASS, Blocks.SHROOMLIGHT},          // wrath
            {Blocks.MAGENTA_CONCRETE, Blocks.MAGENTA_STAINED_GLASS, Blocks.PEARLESCENT_FROGLIGHT}, // lust
            {Blocks.LIME_CONCRETE, Blocks.LIME_STAINED_GLASS, Blocks.VERDANT_FROGLIGHT},  // sloth
            {Blocks.YELLOW_CONCRETE, Blocks.YELLOW_STAINED_GLASS, Blocks.OCHRE_FROGLIGHT}, // greed
            {Blocks.ORANGE_CONCRETE, Blocks.ORANGE_STAINED_GLASS, Blocks.SHROOMLIGHT},    // gluttony
            {Blocks.GREEN_CONCRETE, Blocks.GREEN_STAINED_GLASS, Blocks.VERDANT_FROGLIGHT}, // envy
            {Blocks.PURPLE_CONCRETE, Blocks.PURPLE_STAINED_GLASS, Blocks.PEARLESCENT_FROGLIGHT}}; // pride

    /**
     * Nahrazel's throne room, in the cavern EmpireBuilder.invertedThrone digs: a floor of ash, cracked; seven pillars of the
     * sins in a ring, each in its colour with its light; the throne hung upside down from the roof on chains, and under it
     * a pillar of light down to the book of the Law on its pedestal; shelves of old books round the walls.
     */
    static void invertedThrone(ServerLevel level, StructurePositions.Structure s, int f) {
        int cx = s.x(), cz = s.z(), r = 30;
        Random random = new Random(cx * 43L + cz);
        for (int dx = -r; dx <= r; dx++) for (int dz = -r; dz <= r; dz++) {
            double d = Math.sqrt(dx * dx + dz * dz);
            if (d > r) continue;
            double n = Precinct.patches((cx + dx) * 3, (cz + dz) * 3), crack = Math.abs(Precinct.patches((cx + dx) * 2 + 7, (cz + dz) * 2 - 3));
            set(level, cx + dx, f - 1, cz + dz, crack < 0.04 ? b(Blocks.MAGMA_BLOCK) : crack < 0.08 ? b(Blocks.CRYING_OBSIDIAN)
                    : n > 0.3 ? b(Blocks.TUFF) : n > -0.2 ? b(Blocks.GRAY_CONCRETE_POWDER) : b(Blocks.BASALT));
            if (Math.abs(d - (r - 1)) < 0.7 && Math.floorMod(dx + dz, 3) != 0) for (int h = 0; h < 4; h++) set(level, cx + dx, f + h, cz + dz, b(Blocks.BOOKSHELF));
        }
        // the old throne at the north end goes: the throne hangs from the roof now
        for (int dx = -3; dx <= 3; dx++) for (int dz = -r + 5; dz <= -r + 9; dz++) for (int h = 0; h < 12; h++) set(level, cx + dx, f + h, cz + dz, b(Blocks.AIR));
        // the seven pillars
        for (int i = 0; i < 7; i++) {
            double a = i * Math.PI * 2 / 7 - Math.PI / 2;
            int x = cx + (int) Math.round(Math.cos(a) * 15), z = cz + (int) Math.round(Math.sin(a) * 15);
            Block[] sin = SINS[i];
            for (int du = -1; du <= 1; du++) for (int dv = -1; dv <= 1; dv++) {
                set(level, x + du, f, z + dv, b(Blocks.POLISHED_BLACKSTONE_BRICKS));
                for (int h = 1; h <= 8; h++) {
                    boolean corner = Math.abs(du) == 1 && Math.abs(dv) == 1;
                    set(level, x + du, f + h, z + dv, corner ? b(Blocks.POLISHED_BLACKSTONE) : h >= 3 && h <= 6 ? b(sin[1]) : b(sin[0]));
                }
                set(level, x + du, f + 9, z + dv, b(Blocks.POLISHED_BLACKSTONE_BRICKS));
            }
            for (int h = 2; h <= 7; h++) set(level, x, f + h, z, b(sin[2]));
            set(level, x, f + 10, z, b(sin[2]));
        }
        // the throne, upside down, hung from the roof on chains
        int roof = f + 26, ty = roof - 6;
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) set(level, cx + dx, ty + 5, cz + dz, b(Blocks.OBSIDIAN)); // its seat, up
        for (int dx = -2; dx <= 2; dx++) for (int h = 0; h <= 5; h++) set(level, cx + dx, ty + h, cz - 2, Math.abs(dx) == 2 || h == 0 ? b(Blocks.OBSIDIAN) : b(Blocks.CRYING_OBSIDIAN)); // its back, hanging down
        for (int dz = -2; dz <= 2; dz++) for (int side : new int[]{-2, 2}) set(level, cx + side, ty + 4, cz + dz, b(Blocks.OBSIDIAN));
        for (int i = 0; i < 4; i++) set(level, cx - 2 + i * 4 / 3, ty - 1, cz - 2, b(Blocks.POINTED_DRIPSTONE).setValue(PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN));
        for (int[] c : new int[][]{{-6, -6}, {6, -6}, {-6, 6}, {6, 6}}) { // the chains, from the throne out to the roof
            for (int t = 0; t <= 10; t++) {
                int x = cx + c[0] * t / 10, z = cz + c[1] * t / 10, y = ty + 5 + t;
                set(level, x, y, z, b(Blocks.CHAIN));
            }
        }
        for (int y = ty + 6; y <= roof + 2; y++) set(level, cx, y, cz, b(Blocks.CHAIN));
        // the pillar of light from the throne down to the book of the Law
        for (int y = f + 2; y < ty; y++) set(level, cx, y, cz, b(Blocks.END_ROD));
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) set(level, cx + dx, f, cz + dz, Math.max(Math.abs(dx), Math.abs(dz)) == 2 ? b(Blocks.GOLD_BLOCK) : b(Blocks.CHISELED_POLISHED_BLACKSTONE));
        set(level, cx, f + 1, cz, b(Blocks.ENCHANTING_TABLE));
        for (int[] c : new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) set(level, cx + c[0], f + 1, cz + c[1], b(Blocks.CANDLE).setValue(net.minecraft.world.level.block.CandleBlock.LIT, true)
                .setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 3));
        for (int i = 0; i < 30; i++) { // the ash of those who came before
            int x = cx - r + 4 + random.nextInt(2 * r - 8), z = cz - r + 4 + random.nextInt(2 * r - 8);
            if (Math.abs(x - cx) < 4 && Math.abs(z - cz) < 4) continue;
            if (level.getBlockState(new BlockPos(x, f, z)).isAir()) set(level, x, f, z, random.nextBoolean() ? b(Blocks.SKELETON_SKULL) : b(Blocks.GRAY_CARPET));
        }
    }

    /** The Inverted Throne's way in on the surface: a door of dark stone with its crowned emblem, in a wall of ash rock. */
    static void throneDoor(ServerLevel level, int x, int top, int z) {
        BlockState dark = b(Blocks.POLISHED_BLACKSTONE_BRICKS), frame = b(Blocks.POLISHED_BLACKSTONE);
        for (int dx = -7; dx <= 7; dx++) for (int dy = 0; dy <= 11; dy++) {
            double n = Precinct.patches((x + dx) * 3, dy * 3);
            boolean door = Math.abs(dx) <= 1 && dy <= 3;
            boolean face = Math.abs(dx) <= 5 && dy <= 8;
            if (door) continue;
            set(level, x + dx, top + dy, z - 4, face ? (Math.abs(dx) == 5 || dy == 8 ? frame : dark) : n > 0 ? b(Blocks.TUFF) : b(Blocks.BASALT));
        }
        for (int dx = -1; dx <= 1; dx++) for (int dy = 5; dy <= 7; dy++) set(level, x + dx, top + dy, z - 3, b(Blocks.CHISELED_POLISHED_BLACKSTONE));
        set(level, x, top + 7, z - 2, b(Blocks.WITHER_SKELETON_SKULL));
        for (int side : new int[]{-4, 4}) {
            set(level, x + side, top, z - 3, frame);
            set(level, x + side, top + 1, z - 3, b(Blocks.SOUL_LANTERN));
        }
        for (int dz = -3; dz <= -1; dz++) for (int dx = -1; dx <= 1; dx++) for (int dy = 0; dy <= 3; dy++) set(level, x + dx, top + dy, z + dz, b(Blocks.AIR));
    }

    /** A hanging lantern (for a builder's roof). */
    static BlockState hanging() {
        return b(Blocks.SOUL_LANTERN).setValue(LanternBlock.HANGING, true);
    }
}
