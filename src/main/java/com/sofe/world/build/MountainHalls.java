package com.sofe.world.build;

import com.sofe.registry.EntityRegistry;
import com.sofe.registry.SoFEBlocks;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Random;

import static com.sofe.world.build.SultharisBuilder.set;

/**
 * The boss halls of Nordrath, cut into mountains (art/concepts: the Forge of Kaleth, the Arena of Serath, the Burning
 * Citadel of Vorath). Outside: a crag of grey rock with the hall's front carved into its southern face, an arch with
 * its Sealed Gate, a plaque with its emblem, pillars of glowing runes, braziers and the stone warriors who guard it,
 * on a terrace with a parapet and lights, and a broad stair down to the land. Inside: a passage with the hall's
 * guards (two spawners), then the hall itself:
 * <ul>
 *   <li>the Forge: lava running in channels across the floor with bridges over it, a giant anvil on a stepped dais in
 *   the middle, forges along the walls, a gallery above, and a fall of lava into a pit;</li>
 *   <li>the Arena: a round pit with tiers of seats about it, the dais in the middle over a floor dark with blood, red
 *   lights;</li>
 *   <li>the Citadel: a round hall where rings of lava surround the dais, four bridges across them, tiers of seats, red
 *   banners and a roof of iron trusses hung with chains; falls of lava down the front outside.</li>
 * </ul>
 * The gate, the Waystone, the runes before it and the boss's place (boss_lairs.json) stay where they were.
 */
final class MountainHalls {
    enum Kind { FORGE, ARENA, CITADEL }

    /** The land's rock, the dressed stone and timber of the hall, its floor, its glowing runes and its flames. */
    record Look(List<BlockState> rock, BlockState top, BlockState wall, BlockState trim, BlockState floor, BlockState rune, BlockState flame,
                BlockState stairs) {
    }

    private static BlockState b(Block block) {
        return block.defaultBlockState();
    }

    private MountainHalls() {
    }

    static boolean has(String piece) {
        return kind(piece) != null;
    }

    static Kind kind(String piece) {
        return switch (piece) {
            case "nordrath/forge" -> Kind.FORGE;
            case "nordrath/arena" -> Kind.ARENA;
            case "nordrath/burning_citadel" -> Kind.CITADEL;
            default -> null;
        };
    }

    private static Look look(Kind kind) {
        BlockState runestone = b(SoFEBlocks.NORDRATH_RUNESTONE_BRICKS.get()), timber = b(SoFEBlocks.NORDRATH_DARK_TIMBER.get());
        BlockState runestoneStairs = b(SoFEBlocks.NORDRATH_RUNESTONE_BRICK_STAIRS.get());
        return switch (kind) {
            case FORGE -> new Look(List.of(b(Blocks.STONE), b(Blocks.STONE), b(Blocks.ANDESITE), b(Blocks.TUFF), b(Blocks.COBBLESTONE)), b(Blocks.STONE),
                    runestone, timber, b(Blocks.POLISHED_BLACKSTONE_BRICKS), b(Blocks.MAGMA_BLOCK), Blocks.CAMPFIRE.defaultBlockState(), runestoneStairs);
            case ARENA -> new Look(List.of(b(Blocks.STONE), b(Blocks.ANDESITE), b(Blocks.COBBLESTONE), b(Blocks.TUFF), b(Blocks.STONE)), b(Blocks.SNOW_BLOCK),
                    runestone, timber, b(Blocks.POLISHED_DEEPSLATE), b(Blocks.RED_NETHER_BRICKS), Blocks.CAMPFIRE.defaultBlockState(), runestoneStairs);
            case CITADEL -> new Look(List.of(b(Blocks.STONE), b(Blocks.ANDESITE), b(Blocks.STONE), b(Blocks.NETHERRACK), b(Blocks.COBBLESTONE)), b(Blocks.STONE),
                    b(SoFEBlocks.CORRUPTED_NORDRATH_RUNESTONE_BRICKS.get()), b(SoFEBlocks.CORRUPTED_NORDRATH_DARK_TIMBER.get()), b(Blocks.POLISHED_BLACKSTONE_BRICKS),
                    b(Blocks.MAGMA_BLOCK), Blocks.CAMPFIRE.defaultBlockState(), b(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS));
        };
    }

    /** Builds the hall of a piece; false for any other. */
    static boolean build(ServerLevel level, StructurePositions.Structure s, String piece) {
        Kind kind = kind(piece);
        if (kind == null) return false;
        Look look = look(kind);
        Random random = new Random(s.x() * 131L + s.z());
        StructurePositions.Gate gate = StructurePositions.get().gates().get(s.id());
        int cx = s.x(), cz = s.z();
        int front = gate != null ? gate.z() : cz + s.sizeZ() / 2 - 1; // the line of the hall's front, its gate in the middle
        int ground = SultharisBuilder.surfaceY(level, cx, front + 20);
        int floor = Math.max(ground, SultharisBuilder.surfaceY(level, cx, cz)) + 4; // the hall's floor: up the crag, above the land
        int height = switch (kind) { case FORGE -> 32; case ARENA -> 34; case CITADEL -> 42; };
        int hallHeight = switch (kind) { case FORGE -> 15; case ARENA -> 16; case CITADEL -> 20; };
        Island island = new Island(cx, cz - 2, s.sizeX() / 2 + 18, s.sizeZ() / 2 + 14, front);
        mountain(level, island, ground, floor, height, look, random, floor + hallHeight + 4);
        moat(level, island, ground, kind, look);

        // the way through the dungeon to the boss: passages, a guards' room with its spawners and chests, then the hall
        List<int[]> rooms = new java.util.ArrayList<>(); // x0, z0, x1, z1, height; a guards' room has a sixth value
        switch (kind) {
            case FORGE -> {
                rooms.add(new int[]{cx - 2, cz + 22, cx + 2, front - 1, 6});
                rooms.add(new int[]{cx - 7, cz + 11, cx + 7, cz + 21, 8, 1});
                rooms.add(new int[]{cx + 8, cz + 14, cx + 11, cz + 18, 5}); // a niche with treasure
                rooms.add(new int[]{cx - 2, cz - 5, cx + 2, cz + 10, 6});
            }
            case ARENA -> {
                rooms.add(new int[]{cx - 2, cz + 24, cx + 2, front - 1, 6});
                rooms.add(new int[]{cx - 27, cz + 24, cx - 3, cz + 28, 6});
                rooms.add(new int[]{cx - 32, cz + 11, cx - 22, cz + 23, 8, 1});
                rooms.add(new int[]{cx - 29, cz - 2, cx - 25, cz + 10, 6});
                rooms.add(new int[]{cx - 24, cz - 2, cx - 21, cz + 2, 6}); // into the arena from the west
            }
            default -> {
                rooms.add(new int[]{cx - 2, cz + 40, cx + 2, front - 1, 6});
                rooms.add(new int[]{cx - 8, cz + 29, cx + 8, cz + 39, 9, 1});
                rooms.add(new int[]{cx - 12, cz + 32, cx - 9, cz + 36, 5}); // a niche with treasure
                rooms.add(new int[]{cx - 2, cz + 21, cx + 2, cz + 28, 6});
            }
        }
        switch (kind) {
            case FORGE -> room(level, cx - 18, floor, cz - 30, cx + 18, cz - 6, hallHeight, look);
            default -> round(level, cx, cz, floor, 22, hallHeight, look);
        }
        for (int[] r : rooms) room(level, r[0], floor, r[1], r[2], r[3], r[4], look);
        for (int[] r : rooms) open(level, r, floor);
        switch (kind) {
            case FORGE -> forge(level, cx, cz, floor, look, random);
            case ARENA -> arena(level, cx, cz, floor, look, random);
            default -> citadel(level, cx, cz, floor, look, random);
        }
        for (int[] r : rooms) if (r.length > 5) guards(level, r, floor, kind, random);
        for (int[] r : rooms) if (r[2] - r[0] == 3 && r[3] - r[1] == 4) DungeonBuilder.chest(level, (r[0] + r[2]) / 2, floor, (r[1] + r[3]) / 2, "sofe:chests/dungeon_medium", random);
        terrace(level, cx, front, floor, ground, look, kind);
        facade(level, cx, front, floor, look, kind, random);
        if (gate != null) StructureBuilder.placeGate(level, gate, floor);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the island

    /** The island the hall stands on: an oval of rock about (x, z), cut flat where the hall's front stands. */
    record Island(int x, int z, int rx, int rz, int front) {
        double r(int px, int pz) {
            double dx = (px - x) / (double) rx, dz = (pz - z) / (double) rz;
            return Math.sqrt(dx * dx + dz * dz) + 0.06 * Math.sin(px * 0.31) * Math.cos(pz * 0.27);
        }
    }

    /**
     * A crag of the land's rock over the hall: a high mass in the middle falling in cliffs to its shore, jagged and with
     * spires, cut flat where the hall's front stands; before the front, a shore of rock at the land's height.
     */
    private static void mountain(ServerLevel level, Island is, int ground, int floor, int height, Look look, Random random, int roof) {
        for (int x = is.x() - is.rx(); x <= is.x() + is.rx(); x++) {
            for (int z = is.z() - is.rz(); z <= is.z() + is.rz(); z++) {
                double r = is.r(x, z);
                if (r > 1) continue;
                int top;
                if (z > is.front()) top = ground - 1; // the shore before the front
                else {
                    double fall = Math.min(1, Math.max(0, (1 - r) / 0.42));
                    // the slope in strata: ledges three blocks high, their edges wandering, and a broken crest on top
                    double rise = (height - 4) * Math.pow(fall, 0.55) + 2.5 * Precinct.patches(x * 2, z * 2) + 1.5 * Precinct.patches(z * 5, x * 5);
                    int ledge = 3 + (Precinct.patches(x, z * 2) > 0.2 ? 1 : 0);
                    top = floor + 4 + (int) (Math.floor(rise / ledge) * ledge) + (Precinct.patches(x * 7, z * 7) > 0.45 ? 1 : 0);
                    if (fall > 0.85) top += (int) Math.round(4 * Math.max(0, Precinct.patches(x * 6, z * 6))); // the crest's teeth
                    if (r < 0.82) top = Math.max(top, roof); // the halls and passages never show through
                    if (Math.abs(x - is.x()) <= 14 && z > is.front() - 12) top = Math.max(top, floor + 14); // the face over the front
                }
                for (int y = ground - 9; y <= top; y++) set(level, x, y, z, y == top && z <= is.front() && random.nextInt(3) > 0 ? look.top() : rock(look, x, y, z, random));
                for (int y = top + 1; y < top + 10; y++) if (!level.getBlockState(new BlockPos(x, y, z)).isAir()) set(level, x, y, z, Blocks.AIR.defaultBlockState());
            }
        }
        // the crag's face beside the hall's front: not a cut wall but rock standing out of it, ledges and boulders
        for (int x = is.x() - is.rx(); x <= is.x() + is.rx(); x++) {
            if (Math.abs(x - is.x()) <= 11 || is.r(x, is.front()) > 1) continue;
            int top = SultharisBuilder.surfaceY(level, x, is.front()) - 1;
            for (int y = ground; y <= top; y++) {
                double n = Precinct.patches(x * 3 + y, y * 2 - x);
                int out = n > 0.35 ? 3 : n > 0.05 ? 2 : n > -0.25 ? 1 : 0;
                if (y > top - 2) out = Math.min(out, 1);
                for (int dz = 1; dz <= out; dz++) set(level, x, y, is.front() + dz, rock(look, x, y, is.front() + dz, random));
            }
        }
        // spires on the crag, thin and thick
        for (int i = 0; i < 18; i++) {
            int x = is.x() + random.nextInt(is.rx() * 3 / 2) - is.rx() * 3 / 4, z = is.z() + random.nextInt(is.rz() * 3 / 2) - is.rz() * 3 / 4;
            if (z > is.front() - 6 || is.r(x, z) > 0.9) continue;
            spire(level, x, SultharisBuilder.surfaceY(level, x, z), z, 4 + random.nextInt(10), 1 + random.nextInt(3), look, random);
        }
        // boulders fallen on the slopes and ledges
        for (int i = 0; i < 40; i++) {
            int x = is.x() + random.nextInt(is.rx() * 2) - is.rx(), z = is.z() + random.nextInt(is.rz() * 2) - is.rz();
            if (z > is.front() - 2 || is.r(x, z) > 0.97 || Math.abs(x - is.x()) <= 11 && z > is.front() - 14) continue;
            int base = SultharisBuilder.surfaceY(level, x, z), size = 1 + random.nextInt(2);
            for (int dx = 0; dx <= size; dx++) for (int dz = 0; dz <= size; dz++) for (int dy = 0; dy <= size - (dx + dz) / 2; dy++) {
                if (random.nextInt(5) > 0) set(level, x + dx, base + dy, z + dz, rock(look, x + dx, base + dy + 3, z + dz, random));
            }
        }
    }

    /** A spire of rock, this wide at its foot, narrowing to a point. */
    static void spire(ServerLevel level, int x, int base, int z, int height, int width, Look look, Random random) {
        for (int dy = 0; dy < height; dy++) {
            int w = Math.max(0, width - dy * (width + 1) / Math.max(1, height));
            for (int dx = -w; dx <= w; dx++) for (int dz = -w; dz <= w; dz++) {
                if (Math.abs(dx) + Math.abs(dz) > w + 1 || dy > 0 && random.nextInt(6) == 0) continue;
                set(level, x + dx, base + dy, z + dz, rock(look, x + dx, base + dy, z + dz, random));
            }
        }
    }

    /** A block of the crag: mostly its first rock, with veins and patches of the others, never in rings. */
    private static BlockState rock(Look look, int x, int y, int z, Random random) {
        double n = Precinct.patches(x + y * 2, z - y) + 0.35 * Precinct.patches(z * 3, x * 3 + y);
        int pick = n > 0.55 ? 2 : n > 0.3 ? 1 : n < -0.6 ? 3 : 0;
        if (random.nextInt(12) == 0) pick = random.nextInt(look.rock().size());
        return look.rock().get(Math.min(pick, look.rock().size() - 1));
    }

    /**
     * The moat round the island, eight blocks wide and deep: lava at the Forge and the Citadel, dark water at the
     * Arena. The stair before the front crosses it as a bridge.
     */
    private static void moat(ServerLevel level, Island is, int ground, Kind kind, Look look) {
        BlockState liquid = kind == Kind.ARENA ? Blocks.WATER.defaultBlockState() : Blocks.LAVA.defaultBlockState();
        double out = 1 + 8.0 / Math.min(is.rx(), is.rz());
        for (int x = is.x() - is.rx() - 10; x <= is.x() + is.rx() + 10; x++) {
            for (int z = is.z() - is.rz() - 10; z <= is.z() + is.rz() + 10; z++) {
                double r = is.r(x, z);
                if (r <= 1 || r > out) continue;
                int top = Math.max(ground, SultharisBuilder.surfaceY(level, x, z));
                set(level, x, ground - 9, z, look.rock().get(0));
                for (int y = ground - 8; y <= top + 4; y++) {
                    set(level, x, y, z, y <= ground - 7 ? liquid : Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    /** Opens a room into the ones it touches: a wall block with air on both sides across it is a doorway between rooms. */
    private static void open(ServerLevel level, int[] r, int y) {
        for (int x = r[0] - 1; x <= r[2] + 1; x++) {
            for (int z = r[1] - 1; z <= r[3] + 1; z++) {
                boolean edge = x < r[0] || x > r[2] || z < r[1] || z > r[3];
                if (!edge) continue;
                boolean throughX = level.getBlockState(new BlockPos(x - 1, y, z)).isAir() && level.getBlockState(new BlockPos(x + 1, y, z)).isAir();
                boolean throughZ = level.getBlockState(new BlockPos(x, y, z - 1)).isAir() && level.getBlockState(new BlockPos(x, y, z + 1)).isAir();
                if (!throughX && !throughZ) continue;
                for (int dy = 0; dy < 5; dy++) set(level, x, y + dy, z, Blocks.AIR.defaultBlockState());
            }
        }
    }

    /** A guards' room: its two spawners, a chest, braziers in its corners, weapon racks, a light hung from the roof. */
    private static void guards(ServerLevel level, int[] r, int y, Kind kind, Random random) {
        int mx = (r[0] + r[2]) / 2, mz = (r[1] + r[3]) / 2;
        EntityType<?> first = EntityRegistry.DRAUGR.get(), second = kind == Kind.ARENA ? EntityType.STRAY : EntityType.SKELETON;
        DungeonBuilder.spawner(level, r[0] + 2, y, mz, first);
        DungeonBuilder.spawner(level, r[2] - 2, y, mz, second);
        DungeonBuilder.chest(level, mx + 2, y, r[1], "sofe:chests/dungeon_small", random);
        for (int[] c : new int[][]{{r[0], r[1]}, {r[2], r[1]}, {r[0], r[3]}, {r[2], r[3]}}) set(level, c[0], y, c[1], SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
        for (int x = r[0] + 2; x <= r[2] - 2; x += 3) {
            if (Math.abs(x - mx) <= 2) continue;
            set(level, x, y, r[1], Blocks.SPRUCE_FENCE.defaultBlockState());
            set(level, x, y + 1, r[1], Blocks.LIGHTNING_ROD.defaultBlockState());
        }
        set(level, mx, y + r[4] - 1, mz, Blocks.SHROOMLIGHT.defaultBlockState());
    }

    /** A room carved out: walls of dressed stone with timber posts every six, its floor, air inside. */
    private static void room(ServerLevel level, int x0, int y, int z0, int x1, int z1, int height, Look look) {
        for (int x = x0 - 1; x <= x1 + 1; x++) {
            for (int z = z0 - 1; z <= z1 + 1; z++) {
                boolean edge = x < x0 || x > x1 || z < z0 || z > z1;
                boolean post = edge && (Math.floorMod(x - x0, 6) == 0 || Math.floorMod(z - z0, 6) == 0);
                set(level, x, y - 1, z, look.floor());
                for (int dy = 0; dy < height; dy++) set(level, x, y + dy, z, edge ? (post ? look.trim() : look.wall()) : Blocks.AIR.defaultBlockState());
                boolean lamp = !edge && Math.floorMod(x - x0, 5) == 2 && Math.floorMod(z - z0, 5) == 2;
                set(level, x, y + height, z, lamp ? Blocks.SHROOMLIGHT.defaultBlockState() : look.wall());
            }
        }
    }

    /** A round hall carved out, its wall of dressed stone. */
    private static void round(ServerLevel level, int cx, int cz, int y, int r, int height, Look look) {
        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r + 1.2) continue;
                boolean edge = d > r;
                set(level, cx + dx, y - 1, cz + dz, look.floor());
                for (int dy = 0; dy < height; dy++) {
                    boolean post = edge && Math.floorMod((int) Math.round(Math.atan2(dz, dx) * 12), 3) == 0;
                    set(level, cx + dx, y + dy, cz + dz, edge ? (post ? look.trim() : look.wall()) : Blocks.AIR.defaultBlockState());
                }
                boolean lamp = !edge && Math.floorMod(dx, 6) == 3 && Math.floorMod(dz, 6) == 3;
                set(level, cx + dx, y + height, cz + dz, lamp ? Blocks.SHROOMLIGHT.defaultBlockState() : look.wall());
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ outside

    /**
     * The hall's front, carved into the face of the crag (the concept's "main entrance") and standing out of it in
     * layers: buttresses at its sides that step back as they rise, pillars of runes with their capitals and braziers,
     * an arch in three planes about the gate with the hall's emblem on its keystone, the plaque over it, and a cornice
     * of upturned steps that throws its shadow.
     */
    private static void facade(ServerLevel level, int cx, int front, int floor, Look look, Kind kind, Random random) {
        int z = front;
        BlockState up = look.stairs().setValue(StairBlock.FACING, Direction.NORTH); // a step rising towards the face
        BlockState overhang = look.stairs().setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP);
        // the face itself, with its plinth and bands
        for (int x = cx - 10; x <= cx + 10; x++) {
            for (int dy = -1; dy <= 15; dy++) {
                boolean band = dy == 0 || dy == 8 || dy == 15;
                set(level, x, floor + dy, z, band ? look.trim() : look.wall());
            }
        }
        for (int dy = 0; dy <= 3; dy++) for (int dx = -1; dx <= 1; dx++) set(level, cx + dx, floor + dy, z, Blocks.AIR.defaultBlockState()); // the gate's opening
        for (int dx = -1; dx <= 1; dx++) set(level, cx + dx, floor + 4, z, Blocks.IRON_BARS.defaultBlockState()); // the portcullis drawn up
        // the buttresses: two blocks out, the outer one stepping back at two thirds of the height
        for (int side : new int[]{-1, 1}) {
            for (int w = 9; w <= 10; w++) {
                int x = cx + side * w;
                for (int dy = 0; dy <= 14; dy++) set(level, x, floor + dy, z + 1, dy == 0 || dy == 14 ? look.trim() : look.wall());
                for (int dy = 0; dy <= 9; dy++) set(level, x, floor + dy, z + 2, dy == 0 ? look.trim() : look.wall());
                set(level, x, floor + 10, z + 2, up);
                set(level, x, floor + 15, z + 1, up);
            }
        }
        // the pillars of runes, out from the face, their capitals and braziers
        for (int side : new int[]{-1, 1}) {
            int x = cx + side * 6;
            set(level, x, floor, z + 2, up);
            for (int dy = 0; dy <= 8; dy++) set(level, x, floor + dy, z + 1, dy == 0 ? look.trim() : dy % 2 == 1 && dy < 8 ? look.rune() : look.wall());
            set(level, x, floor + 9, z + 1, look.trim());
            set(level, x, floor + 9, z + 2, overhang);
            set(level, x, floor + 10, z + 1, SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
        }
        // the arch in three planes: the posts of the gate, a frame out from them, and the outer arch with its keystone
        for (int side : new int[]{-1, 1}) {
            for (int dy = 0; dy <= 4; dy++) set(level, cx + side * 2, floor + dy, z + 1, look.trim());
            for (int dy = 0; dy <= 6; dy++) set(level, cx + side * 3, floor + dy, z + 2, look.wall());
            set(level, cx + side * 3, floor + 7, z + 2, overhang);
            set(level, cx + side * 2, floor + 7, z + 2, look.wall());
        }
        for (int dx = -2; dx <= 2; dx++) set(level, cx + dx, floor + 5, z + 1, look.trim()); // the lintel
        for (int dx = -1; dx <= 1; dx++) set(level, cx + dx, floor + 7, z + 2, look.wall());
        for (int dx = -2; dx <= 2; dx++) set(level, cx + dx, floor + 8, z + 2, overhang); // the arch's crown
        emblem(level, cx, floor + 10, z + 1, kind, look);
        for (int dx = -2; dx <= 2; dx++) set(level, cx + dx, floor + 12, z + 2, overhang); // the emblem's hood
        // the plaque, out from the face, with the hall's flames at its ends
        for (int dx = -7; dx <= 7; dx++) {
            if (Math.abs(dx) <= 2) continue;
            set(level, cx + dx, floor + 12, z + 1, look.trim());
            set(level, cx + dx, floor + 11, z + 1, overhang);
        }
        for (int dx : new int[]{-7, 7}) set(level, cx + dx, floor + 13, z + 1, flame(look, kind));
        // the cornice: a row of upturned steps along the top, and the crag over it
        for (int x = cx - 11; x <= cx + 11; x++) set(level, x, floor + 16, z + 1, overhang);
        if (kind == Kind.CITADEL) { // falls of lava down the face, beside the front
            for (int dx : new int[]{-12, 12}) {
                for (int dy = 0; dy <= 16; dy++) set(level, cx + dx, floor + dy, z, look.wall());
                set(level, cx + dx, floor + 17, z, Blocks.LAVA.defaultBlockState());
                set(level, cx + dx, floor + 16, z + 1, Blocks.LAVA.defaultBlockState());
            }
        }
    }

    /** The hall's sign over its gate: the hammer and the forge, the maiden's shield, the burning horned skull. */
    private static void emblem(ServerLevel level, int cx, int y, int z, Kind kind, Look look) {
        for (int dx = -2; dx <= 2; dx++) for (int dy = -1; dy <= 1; dy++) set(level, cx + dx, y + dy, z, look.trim());
        switch (kind) {
            case FORGE -> {
                set(level, cx, y, z, Blocks.ANVIL.defaultBlockState());
                set(level, cx - 1, y + 1, z, Blocks.MAGMA_BLOCK.defaultBlockState());
                set(level, cx + 1, y + 1, z, Blocks.MAGMA_BLOCK.defaultBlockState());
                set(level, cx, y - 1, z, Blocks.MAGMA_BLOCK.defaultBlockState());
            }
            case ARENA -> {
                set(level, cx, y, z, Blocks.RED_NETHER_BRICKS.defaultBlockState());
                set(level, cx, y + 1, z, Blocks.SKELETON_SKULL.defaultBlockState());
                set(level, cx - 1, y, z, Blocks.RED_WOOL.defaultBlockState());
                set(level, cx + 1, y, z, Blocks.RED_WOOL.defaultBlockState());
            }
            case CITADEL -> {
                set(level, cx, y, z, Blocks.WITHER_SKELETON_SKULL.defaultBlockState());
                set(level, cx - 1, y + 1, z, Blocks.MAGMA_BLOCK.defaultBlockState());
                set(level, cx + 1, y + 1, z, Blocks.MAGMA_BLOCK.defaultBlockState());
                set(level, cx, y - 1, z, Blocks.RED_NETHER_BRICKS.defaultBlockState());
            }
        }
    }

    private static BlockState flame(Look look, Kind kind) {
        return kind == Kind.ARENA ? Blocks.RED_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 4).setValue(CandleBlock.LIT, true)
                : look.flame().setValue(CampfireBlock.SIGNAL_FIRE, false);
    }

    /**
     * The terrace before the front (where the Waystone and its runes stand), its parapet and lights, its stone
     * warriors and their weapons, and a broad stair down to the land.
     */
    private static void terrace(ServerLevel level, int cx, int front, int floor, int ground, Look look, Kind kind) {
        int deep = 17, half = 11;
        for (int x = cx - half; x <= cx + half; x++) {
            for (int z = front + 1; z <= front + deep; z++) {
                for (int y = Math.min(ground, floor) - 6; y < floor; y++) set(level, x, y, z, y == floor - 1 ? look.floor() : look.rock().get(0));
                for (int y = floor; y < floor + 8; y++) set(level, x, y, z, Blocks.AIR.defaultBlockState());
                boolean rim = Math.abs(x - cx) == half || z == front + deep;
                boolean stair = z == front + deep && Math.abs(x - cx) <= 2;
                if (rim && !stair) set(level, x, floor, z, Math.floorMod(x + z, 2) == 0 ? look.wall() : SoFEBlocks.NORDRATH_RUNESTONE_BRICK_WALL.get().defaultBlockState());
            }
        }
        for (int[] c : new int[][]{{cx - half, front + deep}, {cx + half, front + deep}, {cx - half, front + 1}, {cx + half, front + 1}, {cx - 3, front + deep}, {cx + 3, front + deep}}) {
            set(level, c[0], floor, c[1], look.wall());
            set(level, c[0], floor + 1, c[1], look.wall());
            set(level, c[0], floor + 2, c[1], flame(look, kind));
        }
        // the stone warriors on either side of the gate, and their weapons
        for (int dx : new int[]{-8, 8}) {
            warrior(level, cx + dx, floor, front + 5, look, kind);
            for (int i = 0; i < 2; i++) {
                set(level, cx + dx, floor, front + 8 + i * 2, Blocks.SPRUCE_FENCE.defaultBlockState());
                set(level, cx + dx, floor + 1, front + 8 + i * 2, Blocks.LIGHTNING_ROD.defaultBlockState());
            }
        }
        // the stair down to the land, five wide, with lights on its walls
        int steps = Math.max(0, floor - ground);
        for (int i = 0; i < steps; i++) {
            int z = front + deep + 1 + i, y = floor - 1 - i;
            for (int dx = -3; dx <= 3; dx++) {
                int below = Math.abs(dx) == 3 && i % 4 == 0 ? ground - 9 : y - 3; // a pier down to the moat's bed every four steps
                for (int yy = below; yy < y; yy++) set(level, cx + dx, yy, z, Math.abs(dx) == 3 ? look.wall() : look.rock().get(0));
                if (Math.abs(dx) == 3) {
                    set(level, cx + dx, y, z, look.wall());
                    set(level, cx + dx, y + 1, z, i % 4 == 0 ? flame(look, kind) : Blocks.AIR.defaultBlockState());
                } else {
                    set(level, cx + dx, y, z, SoFEBlocks.NORDRATH_RUNESTONE_BRICK_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
                    for (int yy = y + 1; yy < y + 5; yy++) set(level, cx + dx, yy, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    /** A stone warrior on a plinth: legs, a broad body, arms with an axe, a helmed head. */
    private static void warrior(ServerLevel level, int x, int y, int z, Look look, Kind kind) {
        BlockState stone = b(Blocks.POLISHED_ANDESITE), dark = b(Blocks.POLISHED_DEEPSLATE);
        set(level, x, y, z, look.trim());
        set(level, x, y + 1, z, stone);
        set(level, x, y + 2, z, stone);
        for (int dx = -1; dx <= 1; dx++) set(level, x + dx, y + 3, z, dx == 0 ? dark : stone);
        set(level, x, y + 4, z, b(Blocks.CHISELED_STONE_BRICKS));
        set(level, x, y + 5, z, kind == Kind.ARENA ? b(Blocks.RED_NETHER_BRICK_WALL) : b(Blocks.STONE_BRICK_WALL)); // the helmet's crest
        set(level, x + 1, y + 4, z, b(Blocks.LIGHTNING_ROD)); // the haft of the axe
    }

    // ------------------------------------------------------------------------------------------------ the halls

    /** Kaleth's forge: lava in channels with bridges, the anvil on its dais, forges, a gallery and a fall of lava. */
    private static void forge(ServerLevel level, int cx, int cz, int y, Look look, Random random) {
        int zc = cz - 18;
        BlockState lava = b(Blocks.LAVA), rim = b(Blocks.POLISHED_BLACKSTONE), bridge = b(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB);
        // the channels: across the hall and down its length, two wide, their banks of black stone
        for (int x = cx - 17; x <= cx + 17; x++) {
            for (int z = cz - 29; z <= cz - 7; z++) {
                boolean across = Math.abs(z - zc) <= 1 && Math.abs(x - cx) > 6, along = Math.abs(x - cx) == 11 || Math.abs(x - cx) == 12;
                if (!(across || along)) continue;
                boolean over = across && Math.abs(x - cx) % 6 == 0 || along && Math.floorMod(z - zc, 7) == 0; // the bridges
                set(level, x, y - 2, z, rim);
                set(level, x, y - 1, z, over ? rim : lava);
                if (over) set(level, x, y, z, bridge);
            }
        }
        // the dais: three steps up to the giant anvil
        for (int step = 0; step < 3; step++) {
            int r = 6 - step * 2;
            for (int dx = -r; dx <= r; dx++) for (int dz = -r + 1; dz <= r - 1; dz++) set(level, cx + dx, y + step, zc + dz, step == 2 ? look.floor() : rim);
        }
        int a = y + 3;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++) set(level, cx + dx, a, zc + dz, b(Blocks.IRON_BLOCK)); // the foot
        set(level, cx, a + 1, zc, b(Blocks.IRON_BLOCK)); // the waist
        for (int dx = -3; dx <= 3; dx++) for (int dz = -1; dz <= 1; dz++) set(level, cx + dx, a + 2, zc + dz, dx == -3 || dx == 3 ? b(Blocks.ANVIL) : b(Blocks.IRON_BLOCK)); // the face and horn
        set(level, cx + 4, a + 2, zc, b(Blocks.ANVIL));
        // forges along the side walls: a hearth of lava behind bars, a chimney hood
        for (int z = cz - 27; z <= cz - 9; z += 6) {
            for (int side : new int[]{-1, 1}) {
                int x = cx + side * 17;
                set(level, x, y, z, b(Blocks.BLAST_FURNACE));
                set(level, x, y + 1, z, b(Blocks.IRON_BARS));
                set(level, x + side, y + 1, z, lava);
                set(level, x, y + 2, z, look.trim());
                set(level, x - side, y, z, b(Blocks.ANVIL));
            }
        }
        // the gallery round the south and side walls, and its rail
        for (int x = cx - 17; x <= cx + 17; x++) {
            for (int z = cz - 29; z <= cz - 7; z++) {
                boolean ledge = z >= cz - 9 || Math.abs(x - cx) >= 15;
                if (!ledge) continue;
                set(level, x, y + 7, z, b(SoFEBlocks.NORDRATH_DARK_PLANKS.get()));
                boolean edge = z == cz - 9 && Math.abs(x - cx) < 15 || Math.abs(x - cx) == 15 && z < cz - 9;
                if (edge) set(level, x, y + 8, z, b(Blocks.SPRUCE_FENCE));
            }
        }
        // the fall of lava from the north wall into its pit
        for (int dx = -2; dx <= 2; dx++) {
            set(level, cx + dx, y + 13, cz - 30, lava);
            set(level, cx + dx, y - 1, cz - 29, lava);
            set(level, cx + dx, y - 2, cz - 29, rim);
        }
        // runes on the walls, chains from the roof, braziers
        for (int z = cz - 27; z <= cz - 9; z += 6) for (int side : new int[]{-1, 1}) set(level, cx + side * 18, y + 5, z, look.rune());
        for (int i = 0; i < 10; i++) {
            int x = cx - 15 + random.nextInt(31), z = cz - 28 + random.nextInt(20);
            for (int dy = 14; dy > 10; dy--) set(level, x, y + dy, z, b(Blocks.CHAIN));
        }
        for (int[] c : new int[][]{{cx - 8, cz - 26}, {cx + 8, cz - 26}, {cx - 8, cz - 10}, {cx + 8, cz - 10}}) set(level, c[0], y, c[1], SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
    }

    /** Serath's arena: tiers of seats round a pit, the dais in the middle over a floor dark with blood. */
    private static void arena(ServerLevel level, int cx, int cz, int y, Look look, Random random) {
        for (int dx = -21; dx <= 21; dx++) {
            for (int dz = -21; dz <= 21; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 21.5) continue;
                int x = cx + dx, z = cz + dz;
                boolean way = dx < 0 && Math.abs(dz) <= 2; // the way in from the passage, on the west
                if (d <= 4) { // the dais, its tiles in rings
                    set(level, x, y, z, d > 3 ? look.trim() : Math.floorMod(dx + dz, 2) == 0 ? b(Blocks.POLISHED_BLACKSTONE) : b(Blocks.GILDED_BLACKSTONE));
                } else if (d <= 12) { // the floor of blood
                    BlockState blood = Precinct.patches(x * 2, z * 2) > 0.2 ? b(Blocks.RED_CONCRETE) : Precinct.patches(x, z) > -0.3 ? b(Blocks.RED_TERRACOTTA) : b(Blocks.NETHER_WART_BLOCK);
                    set(level, x, y - 1, z, d > 11.3 ? look.trim() : blood);
                } else if (!way) { // the tiers, rising to the wall
                    int tier = Math.min(6, (int) ((d - 12) * 0.8));
                    for (int dy = 0; dy <= tier; dy++) set(level, x, y + dy, z, dy == tier ? b(SoFEBlocks.NORDRATH_RUNESTONE_BRICK_SLAB.get()) : look.wall());
                    if (tier >= 2 && random.nextInt(14) == 0) set(level, x, y + tier + 1, z, Blocks.SKELETON_SKULL.defaultBlockState()); // the dead who watch
                }
            }
        }
        for (int i = 0; i < 10; i++) { // red lights round the pit
            double ang = i * Math.PI / 5;
            int x = cx + (int) Math.round(Math.cos(ang) * 12.5), z = cz + (int) Math.round(Math.sin(ang) * 12.5);
            if (x < cx && Math.abs(z - cz) <= 3) continue;
            set(level, x, y, z, look.wall());
            set(level, x, y + 1, z, Blocks.RED_CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 4).setValue(CandleBlock.LIT, true));
        }
        for (int i = 0; i < 6; i++) { // banners of the maiden high on the wall
            double ang = i * Math.PI / 3 + 0.3;
            int x = cx + (int) Math.round(Math.cos(ang) * 21), z = cz + (int) Math.round(Math.sin(ang) * 21);
            for (int dy = 9; dy <= 12; dy++) set(level, x, y + dy, z, b(Blocks.RED_WOOL));
            set(level, x, y + 13, z, SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
        }
    }

    /** Vorath's hall: rings of lava about the dais, four bridges, tiers of seats, red banners, iron trusses with chains. */
    private static void citadel(ServerLevel level, int cx, int cz, int y, Look look, Random random) {
        for (int dx = -21; dx <= 21; dx++) {
            for (int dz = -21; dz <= 21; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 21.5) continue;
                int x = cx + dx, z = cz + dz;
                boolean bridge = Math.abs(dx) <= 1 || Math.abs(dz) <= 1;
                if (d <= 6) set(level, x, y - 1, z, d > 5 ? b(Blocks.MAGMA_BLOCK) : Math.floorMod((int) d, 2) == 0 ? b(Blocks.BLACKSTONE) : b(Blocks.POLISHED_BLACKSTONE));
                else if (d <= 9.5) { // the ring of lava, bridged four ways
                    set(level, x, y - 2, z, b(Blocks.BLACKSTONE));
                    set(level, x, y - 1, z, bridge ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : b(Blocks.LAVA));
                } else if (d <= 13) set(level, x, y - 1, z, d > 12.3 ? b(Blocks.MAGMA_BLOCK) : look.floor());
                else if (!(dz > 0 && Math.abs(dx) <= 2)) { // the tiers
                    int tier = Math.min(7, (int) ((d - 13) * 0.9));
                    for (int dy = 0; dy <= tier; dy++) set(level, x, y + dy, z, dy == tier ? b(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB) : look.wall());
                }
            }
        }
        // the trusses of iron across the roof, chains hanging from them
        for (int i = -18; i <= 18; i++) {
            set(level, cx + i, y + 18, cz, b(Blocks.IRON_BARS));
            set(level, cx, y + 18, cz + i, b(Blocks.IRON_BARS));
            if (i % 6 == 0) for (int dy = 17; dy > 12; dy--) {
                set(level, cx + i, y + dy, cz, b(Blocks.CHAIN));
                set(level, cx, y + dy, cz + i, b(Blocks.CHAIN));
            }
        }
        for (int i = 0; i < 8; i++) { // red banners and braziers on the wall
            double ang = i * Math.PI / 4 + 0.2;
            int x = cx + (int) Math.round(Math.cos(ang) * 21), z = cz + (int) Math.round(Math.sin(ang) * 21);
            for (int dy = 10; dy <= 14; dy++) set(level, x, y + dy, z, b(Blocks.RED_WOOL));
            set(level, x, y + 15, z, SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
        }
        for (int i = 0; i < 4; i++) { // fire on the dais' corners
            double ang = i * Math.PI / 2 + Math.PI / 4;
            int x = cx + (int) Math.round(Math.cos(ang) * 4), z = cz + (int) Math.round(Math.sin(ang) * 4);
            set(level, x, y, z, b(Blocks.NETHERRACK));
            set(level, x, y + 1, z, b(Blocks.FIRE));
        }
    }
}
