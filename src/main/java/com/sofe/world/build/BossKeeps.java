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
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The keeps of the bosses: each boss's place is an island of the land's rock in a moat, with the boss's hall inside it
 * and a dungeon to go through before it (the user's concepts of the Forge of Kaleth, the Arena of Serath and the
 * Burning Citadel of Vorath, and the same idea for every other boss, after its name and the look of its act).
 * <p>
 * Outside: a crag in strata with a broken crest, spires and fallen boulders, its front carved into its face in the
 * style of the empire (a Norse hall front, a Persian iwan between minarets, an Egyptian pylon with a winged sun, a Roman
 * portico with its pediment, the false prophet's golden temple), on a terrace with what guards it, and a stair that
 * crosses the moat as a bridge. Inside: passages and a guards' room (spawners, chests) and a niche of treasure, then the
 * hall, made after its boss. Some halls are open to the sky, in a crater of the crag (the Gardens, the Marsh, the
 * Caravanserai, the Market, the Colosseum).
 * <p>
 * Everything is laid out in the keep's own frame: v runs out of its front (towards its gate and Waystone), u across it;
 * the frame turns it to face whichever way its gate does. The gate, the Waystone, the runes and the boss's place
 * (boss_lairs.json) stay where they were.
 */
final class BossKeeps {
    enum Front { NORDIC, PERSIAN, EGYPTIAN, ROMAN, TEMPLE }

    enum Hall { FORGE, ARENA, CITADEL, BATHS, CARAVANSERAI, GARDENS, CATACOMBS, MARSH, TREASURY, MARKET, VAULTS, COLOSSEUM, SENATE, TEMPLE }

    /**
     * A keep's look: the land's rock and the crag's top, the walls, trim, floor and stairs of its builders, its glowing
     * accent, its light, its moat (null: a dry chasm), how its front and its hall are made, its height, its hall's size
     * (a radius, or half its width and depth), whether the hall is open to the sky, and whether it is a ruin.
     */
    record Look(List<BlockState> rock, BlockState top, BlockState wall, BlockState trim, BlockState floor, BlockState stairs, BlockState glow,
                BlockState light, BlockState moat, Front front, Hall hall, int height, int radius, int depth, boolean round, boolean open, boolean ruined) {
    }

    private static BlockState b(Block block) {
        return block.defaultBlockState();
    }

    private static BlockState s(net.minecraftforge.registries.RegistryObject<Block> block) {
        return block.get().defaultBlockState();
    }

    private BossKeeps() {
    }

    static boolean has(String piece) {
        return look(piece) != null;
    }

    static Look look(String piece) {
        List<BlockState> grey = List.of(b(Blocks.STONE), b(Blocks.ANDESITE), b(Blocks.TUFF), b(Blocks.COBBLESTONE));
        List<BlockState> pale = List.of(b(Blocks.STONE), b(Blocks.CALCITE), b(Blocks.DIORITE), b(Blocks.ANDESITE));
        List<BlockState> sand = List.of(b(Blocks.SANDSTONE), b(Blocks.SMOOTH_SANDSTONE), b(Blocks.TERRACOTTA), b(Blocks.ORANGE_TERRACOTTA));
        List<BlockState> dark = List.of(b(Blocks.DEEPSLATE), b(Blocks.BLACKSTONE), b(Blocks.TUFF), b(Blocks.COBBLED_DEEPSLATE));
        BlockState lava = b(Blocks.LAVA), water = b(Blocks.WATER);
        BlockState rune = s(SoFEBlocks.NORDRATH_RUNESTONE_BRICKS), timber = s(SoFEBlocks.NORDRATH_DARK_TIMBER), runeStairs = s(SoFEBlocks.NORDRATH_RUNESTONE_BRICK_STAIRS);
        BlockState plaster = s(SoFEBlocks.PARSIVAN_WHITE_PLASTER), turquoise = s(SoFEBlocks.PARSIVAN_TURQUOISE_TILES), lapis = s(SoFEBlocks.PARSIVAN_LAPIS_MOSAIC),
                plasterStairs = s(SoFEBlocks.PARSIVAN_WHITE_PLASTER_STAIRS), violet = s(SoFEBlocks.PARSIVAN_VIOLET_TILES);
        BlockState carved = s(SoFEBlocks.KHEMET_CARVED_SANDSTONE), glyphs = s(SoFEBlocks.KHEMET_GOLD_HIEROGLYPHS), limestone = s(SoFEBlocks.KHEMET_PAINTED_LIMESTONE),
                khemetStairs = s(SoFEBlocks.KHEMET_SANDSTONE_STAIRS);
        BlockState marble = s(SoFEBlocks.AUREUM_MARBLE_BRICKS), mosaic = s(SoFEBlocks.AUREUM_GOLD_MOSAIC), polished = s(SoFEBlocks.AUREUM_POLISHED_MARBLE),
                marbleStairs = s(SoFEBlocks.AUREUM_MARBLE_BRICK_STAIRS), royal = s(SoFEBlocks.AUREUM_ROYAL_TILES);
        BlockState lantern = b(Blocks.LANTERN), soul = b(Blocks.SOUL_LANTERN);
        return switch (piece) {
            case "nordrath/forge" -> new Look(grey, b(Blocks.STONE), rune, timber, b(Blocks.POLISHED_BLACKSTONE_BRICKS), runeStairs, b(Blocks.MAGMA_BLOCK),
                    b(Blocks.CAMPFIRE), lava, Front.NORDIC, Hall.FORGE, 32, 18, 12, false, false, false);
            case "nordrath/arena" -> new Look(List.of(b(Blocks.STONE), b(Blocks.ANDESITE), b(Blocks.COBBLESTONE), b(Blocks.TUFF)), b(Blocks.SNOW_BLOCK), rune, timber,
                    b(Blocks.POLISHED_DEEPSLATE), runeStairs, b(Blocks.RED_NETHER_BRICKS), b(Blocks.CAMPFIRE), water, Front.NORDIC, Hall.ARENA, 34, 22, 0, true, false, false);
            case "nordrath/burning_citadel" -> new Look(List.of(b(Blocks.STONE), b(Blocks.ANDESITE), b(Blocks.NETHERRACK), b(Blocks.COBBLESTONE)), b(Blocks.STONE),
                    s(SoFEBlocks.CORRUPTED_NORDRATH_RUNESTONE_BRICKS), s(SoFEBlocks.CORRUPTED_NORDRATH_DARK_TIMBER), b(Blocks.POLISHED_BLACKSTONE_BRICKS),
                    b(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS), b(Blocks.MAGMA_BLOCK), b(Blocks.CAMPFIRE), lava, Front.NORDIC, Hall.CITADEL, 42, 22, 0, true, false, false);
            case "parsivan/baths" -> new Look(pale, b(Blocks.MOSS_BLOCK), plaster, turquoise, lapis, plasterStairs, b(Blocks.AMETHYST_BLOCK), lantern, water,
                    Front.PERSIAN, Hall.BATHS, 30, 14, 0, true, false, false);
            case "parsivan/silk_road" -> new Look(sand, b(Blocks.SAND), plaster, turquoise, b(Blocks.SMOOTH_SANDSTONE), plasterStairs, b(Blocks.VERDANT_FROGLIGHT), lantern,
                    null, Front.PERSIAN, Hall.CARAVANSERAI, 28, 16, 16, false, true, false);
            case "parsivan/enchanted_gardens" -> new Look(pale, b(Blocks.MOSS_BLOCK), plaster, violet, b(Blocks.GRASS_BLOCK), plasterStairs, b(Blocks.PEARLESCENT_FROGLIGHT),
                    lantern, water, Front.PERSIAN, Hall.GARDENS, 34, 20, 0, true, true, false);
            case "khemet/catacombs" -> new Look(sand, b(Blocks.SAND), carved, glyphs, limestone, khemetStairs, b(Blocks.SOUL_CAMPFIRE), soul, null,
                    Front.EGYPTIAN, Hall.CATACOMBS, 30, 20, 13, false, false, false);
            case "khemet/stagnant_marsh" -> new Look(List.of(b(Blocks.SANDSTONE), b(Blocks.MUD_BRICKS), b(Blocks.PACKED_MUD), b(Blocks.SMOOTH_SANDSTONE)), b(Blocks.MOSS_BLOCK),
                    carved, glyphs, b(Blocks.MUD), khemetStairs, b(Blocks.VERDANT_FROGLIGHT), soul, water, Front.EGYPTIAN, Hall.MARSH, 30, 20, 0, true, true, false);
            case "aureum/treasury" -> new Look(pale, b(Blocks.GRASS_BLOCK), marble, mosaic, polished, marbleStairs, b(Blocks.GOLD_BLOCK), lantern, water,
                    Front.ROMAN, Hall.TREASURY, 30, 17, 15, false, false, false);
            case "aureum/market" -> new Look(pale, b(Blocks.GRASS_BLOCK), marble, mosaic, royal, marbleStairs, b(Blocks.GOLD_BLOCK), lantern, water,
                    Front.ROMAN, Hall.MARKET, 30, 18, 18, false, true, false);
            case "aureum/golden_vaults" -> new Look(pale, b(Blocks.GRASS_BLOCK), marble, mosaic, polished, marbleStairs, b(Blocks.GOLD_BLOCK), lantern, water,
                    Front.ROMAN, Hall.VAULTS, 36, 20, 0, true, false, false);
            case "aureum/colosseum" -> new Look(pale, b(Blocks.GRASS_BLOCK), marble, mosaic, b(Blocks.SAND), marbleStairs, b(Blocks.GOLD_BLOCK), b(Blocks.CAMPFIRE), water,
                    Front.ROMAN, Hall.COLOSSEUM, 30, 32, 0, true, true, false);
            case "aureum/shadow_throne" -> new Look(dark, b(Blocks.DEEPSLATE), b(Blocks.DEEPSLATE_TILES), b(Blocks.OBSIDIAN), b(Blocks.POLISHED_DEEPSLATE),
                    b(Blocks.DEEPSLATE_TILE_STAIRS), b(Blocks.VERDANT_FROGLIGHT), soul, null, Front.ROMAN, Hall.SENATE, 34, 20, 0, true, false, true);
            case "sulthari/temple" -> new Look(sand, b(Blocks.SAND), s(SoFEBlocks.SULTHARI_SANDSTONE_BRICKS), s(SoFEBlocks.SULTHARI_BRASS_PLATING),
                    s(SoFEBlocks.SULTHARI_GLAZED_TILES), s(SoFEBlocks.SULTHARI_SANDSTONE_BRICK_STAIRS), b(Blocks.GOLD_BLOCK), b(Blocks.CAMPFIRE), null,
                    Front.TEMPLE, Hall.TEMPLE, 32, 17, 0, true, false, false);
            default -> null;
        };
    }

    // ------------------------------------------------------------------------------------------------ the frame

    /** The keep's frame: (u, v) about its middle, v out of its front, turned to face the way its gate does. */
    static final class Frame {
        final ServerLevel level;
        final int cx, cz;
        final Rotation rotation;

        Frame(ServerLevel level, int cx, int cz, Direction out) {
            this.level = level;
            this.cx = cx;
            this.cz = cz;
            this.rotation = switch (out) {
                case WEST -> Rotation.CLOCKWISE_90;
                case NORTH -> Rotation.CLOCKWISE_180;
                case EAST -> Rotation.COUNTERCLOCKWISE_90;
                default -> Rotation.NONE;
            };
        }

        int x(int u, int v) {
            return cx + switch (rotation) { case CLOCKWISE_90 -> -v; case CLOCKWISE_180 -> -u; case COUNTERCLOCKWISE_90 -> v; default -> u; };
        }

        int z(int u, int v) {
            return cz + switch (rotation) { case CLOCKWISE_90 -> u; case CLOCKWISE_180 -> -v; case COUNTERCLOCKWISE_90 -> -u; default -> v; };
        }

        /** The frame's (u, v) of a place in the world. */
        int[] local(int x, int z) {
            int dx = x - cx, dz = z - cz;
            return switch (rotation) {
                case CLOCKWISE_90 -> new int[]{dz, -dx};
                case CLOCKWISE_180 -> new int[]{-dx, -dz};
                case COUNTERCLOCKWISE_90 -> new int[]{-dz, dx};
                default -> new int[]{dx, dz};
            };
        }

        void set(int u, int y, int v, BlockState state) {
            SultharisBuilder.set(level, x(u, v), y, z(u, v), state.rotate(rotation));
        }

        BlockState get(int u, int y, int v) {
            return level.getBlockState(new BlockPos(x(u, v), y, z(u, v)));
        }

        boolean air(int u, int y, int v) {
            return get(u, y, v).isAir();
        }

        int surface(int u, int v) {
            return SultharisBuilder.surfaceY(level, x(u, v), z(u, v));
        }
    }

    /** Which way a place's gate looks out: towards its gate from its middle. */
    static Direction out(StructurePositions.Structure s, StructurePositions.Gate gate) {
        if (gate == null) return Direction.SOUTH;
        int dx = gate.x() - s.x(), dz = gate.z() - s.z();
        return Math.abs(dx) > Math.abs(dz) ? (dx > 0 ? Direction.EAST : Direction.WEST) : (dz >= 0 ? Direction.SOUTH : Direction.NORTH);
    }

    // ------------------------------------------------------------------------------------------------ the keep

    /** Builds a boss's keep; false for any other piece. */
    static boolean build(ServerLevel level, StructurePositions.Structure s, String piece) {
        Look look = look(piece);
        if (look == null) return false;
        Random random = new Random(s.x() * 131L + s.z());
        StructurePositions.Gate gate = StructurePositions.get().gates().get(s.id());
        Direction out = out(s, gate);
        Frame f = new Frame(level, s.x(), s.z(), out);
        boolean across = out.getAxis() == Direction.Axis.X; // the frame's u runs along the world's z
        int halfU = (across ? s.sizeZ() : s.sizeX()) / 2, halfV = (across ? s.sizeX() : s.sizeZ()) / 2;
        int front = gate != null ? f.local(gate.x(), gate.z())[1] : halfV - 1;
        int ground = f.surface(0, front + 20);
        int floor = Math.max(ground, f.surface(0, 0)) + 4; // the hall's floor: up the crag, above the land
        // the hall, about its boss's place
        int[] lair = lairOf(s, f);
        int hu = lair[0], hv = Math.min(lair[1], front - look.depth() - 10 - (look.round() ? look.radius() - look.depth() : 0));
        int hallHeight = look.open() ? 0 : switch (look.hall()) { case CITADEL -> 20; case VAULTS, TEMPLE -> 18; case ARENA, SENATE -> 16; default -> 14; };
        int near = look.round() ? hv + look.radius() : hv + look.depth(); // the hall's edge nearest the front
        Island island = new Island(0, -2, halfU + 18, halfV + 14, front);
        mountain(f, island, ground, floor, look, random, floor + Math.max(hallHeight, 12) + 4);
        moat(f, island, ground, look);
        // the hall itself
        if (look.round()) round(f, hu, hv, floor, look.radius(), look.open() ? 0 : hallHeight, look);
        else room(f, hu - look.radius(), floor, hv - look.depth(), hu + look.radius(), hv + look.depth(), look.open() ? 0 : hallHeight, look);
        if (look.open()) crater(f, hu, hv, floor, look);
        hall(f, hu, hv, floor, look, random);
        // the way through the dungeon
        route(f, hu, hv, near, front, floor, look, random);
        terrace(f, front, floor, ground, look, random);
        facade(f, front, floor, look, random);
        if (look.front() == Front.PERSIAN && !look.open()) dome(f, hu, hv, look);
        if (gate != null) StructureBuilder.placeGate(level, gate, floor);
        return true;
    }

    /** The boss's place in the frame (its lair, boss_lairs.json; the middle when it has none). */
    private static int[] lairOf(StructurePositions.Structure s, Frame f) {
        for (var lair : com.sofe.world.lair.BossLairs.lairs()) {
            if (Math.abs(lair.x() - s.x()) <= s.sizeX() / 2 && Math.abs(lair.z() - s.z()) <= s.sizeZ() / 2 && !lair.hasHeight()) return f.local(lair.x(), lair.z());
        }
        return new int[]{0, 0};
    }

    // ------------------------------------------------------------------------------------------------ the island

    /** The island: an oval of rock about (u, v) in the frame, cut flat where the keep's front stands. */
    record Island(int u, int v, int ru, int rv, int front) {
        double r(int pu, int pv) {
            double du = (pu - u) / (double) ru, dv = (pv - v) / (double) rv;
            return Math.sqrt(du * du + dv * dv) + 0.06 * Math.sin(pu * 0.31) * Math.cos(pv * 0.27);
        }
    }

    /** A crag in strata over the hall, with a broken crest, a rough face beside the front, spires and boulders. */
    private static void mountain(Frame f, Island is, int ground, int floor, Look look, Random random, int roof) {
        int height = look.height();
        for (int u = is.u() - is.ru(); u <= is.u() + is.ru(); u++) {
            for (int v = is.v() - is.rv(); v <= is.v() + is.rv(); v++) {
                double r = is.r(u, v);
                if (r > 1) continue;
                int top;
                if (v > is.front()) top = ground - 1; // the shore before the front
                else {
                    double fall = Math.min(1, Math.max(0, (1 - r) / 0.42));
                    double rise = (height - 4) * Math.pow(fall, 0.55) + 2.5 * Precinct.patches(u * 2, v * 2) + 1.5 * Precinct.patches(v * 5, u * 5);
                    int ledge = 3 + (Precinct.patches(u, v * 2) > 0.2 ? 1 : 0);
                    top = floor + 4 + (int) (Math.floor(rise / ledge) * ledge) + (Precinct.patches(u * 7, v * 7) > 0.45 ? 1 : 0);
                    if (fall > 0.85) top += (int) Math.round(4 * Math.max(0, Precinct.patches(u * 6, v * 6))); // the crest's teeth
                    if (r < 0.82) top = Math.max(top, roof); // the halls and passages never show through
                    if (Math.abs(u) <= 14 && v > is.front() - 12) top = Math.max(top, floor + 18); // the face over the front
                }
                for (int y = ground - 9; y <= top; y++) f.set(u, y, v, y == top && v <= is.front() && random.nextInt(3) > 0 ? look.top() : rock(look, u, y, v, random));
                for (int y = top + 1; y < top + 10; y++) if (!f.air(u, y, v)) f.set(u, y, v, Blocks.AIR.defaultBlockState());
            }
        }
        // the face beside the front: rock standing out of it, ledges and boulders
        for (int u = is.u() - is.ru(); u <= is.u() + is.ru(); u++) {
            if (Math.abs(u) <= 12 || is.r(u, is.front()) > 1) continue;
            int top = f.surface(u, is.front()) - 1;
            for (int y = ground; y <= top; y++) {
                double n = Precinct.patches(u * 3 + y, y * 2 - u);
                int out = n > 0.35 ? 3 : n > 0.05 ? 2 : n > -0.25 ? 1 : 0;
                if (y > top - 2) out = Math.min(out, 1);
                for (int dv = 1; dv <= out; dv++) f.set(u, y, is.front() + dv, rock(look, u, y, is.front() + dv, random));
            }
        }
        for (int i = 0; i < 18; i++) { // spires, thin and thick
            int u = is.u() + random.nextInt(is.ru() * 3 / 2) - is.ru() * 3 / 4, v = is.v() + random.nextInt(is.rv() * 3 / 2) - is.rv() * 3 / 4;
            if (v > is.front() - 6 || is.r(u, v) > 0.9) continue;
            spire(f, u, f.surface(u, v), v, 4 + random.nextInt(10), 1 + random.nextInt(3), look, random);
        }
        for (int i = 0; i < 40; i++) { // boulders fallen on the slopes and ledges
            int u = is.u() + random.nextInt(is.ru() * 2) - is.ru(), v = is.v() + random.nextInt(is.rv() * 2) - is.rv();
            if (v > is.front() - 2 || is.r(u, v) > 0.97 || Math.abs(u) <= 12 && v > is.front() - 14) continue;
            int base = f.surface(u, v), size = 1 + random.nextInt(2);
            for (int du = 0; du <= size; du++) for (int dv = 0; dv <= size; dv++) for (int dy = 0; dy <= size - (du + dv) / 2; dy++) {
                if (random.nextInt(5) > 0) f.set(u + du, base + dy, v + dv, rock(look, u + du, base + dy + 3, v + dv, random));
            }
        }
    }

    private static void spire(Frame f, int u, int base, int v, int height, int width, Look look, Random random) {
        for (int dy = 0; dy < height; dy++) {
            int w = Math.max(0, width - dy * (width + 1) / Math.max(1, height));
            for (int du = -w; du <= w; du++) for (int dv = -w; dv <= w; dv++) {
                if (Math.abs(du) + Math.abs(dv) > w + 1 || dy > 0 && random.nextInt(6) == 0) continue;
                f.set(u + du, base + dy, v + dv, rock(look, u + du, base + dy, v + dv, random));
            }
        }
    }

    /** A block of the crag: mostly its first rock, with veins and patches of the others, never in rings. */
    private static BlockState rock(Look look, int u, int y, int v, Random random) {
        double n = Precinct.patches(u + y * 2, v - y) + 0.35 * Precinct.patches(v * 3, u * 3 + y);
        int pick = n > 0.55 ? 2 : n > 0.3 ? 1 : n < -0.6 ? 3 : 0;
        if (random.nextInt(12) == 0) pick = random.nextInt(look.rock().size());
        return look.rock().get(Math.min(pick, look.rock().size() - 1));
    }

    /** The moat round the island, eight blocks wide and deep, of the keep's liquid or a dry chasm; the stair crosses it as a bridge. */
    private static void moat(Frame f, Island is, int ground, Look look) {
        double out = 1 + 8.0 / Math.min(is.ru(), is.rv());
        for (int u = is.u() - is.ru() - 10; u <= is.u() + is.ru() + 10; u++) {
            for (int v = is.v() - is.rv() - 10; v <= is.v() + is.rv() + 10; v++) {
                double r = is.r(u, v);
                if (r <= 1 || r > out) continue;
                int top = Math.max(ground, f.surface(u, v));
                f.set(u, ground - 9, v, look.rock().get(0));
                for (int y = ground - 8; y <= top + 4; y++) {
                    BlockState fill = y <= ground - 7 ? (look.moat() != null ? look.moat() : look.rock().get(1)) : Blocks.AIR.defaultBlockState();
                    f.set(u, y, v, fill);
                }
            }
        }
    }

    /** An open hall's crater: the crag over it taken away to the sky, its inner slopes in ledges. */
    private static void crater(Frame f, int hu, int hv, int floor, Look look) {
        int r = look.round() ? look.radius() : Math.max(look.radius(), look.depth());
        for (int du = -r - 6; du <= r + 6; du++) {
            for (int dv = -r - 6; dv <= r + 6; dv++) {
                double d = look.round() ? Math.sqrt(du * du + dv * dv) : Math.max(Math.abs(du) * r / (double) look.radius(), Math.abs(dv) * r / (double) Math.max(1, look.depth()));
                if (d > r + 6) continue;
                int from = d <= r + 1.2 ? floor + 12 : floor + 12 + (int) ((d - r - 1) * 2.5); // the walls of the crater rise in steps
                for (int y = from; y < floor + look.height() + 14; y++) if (!f.air(hu + du, y, hv + dv)) f.set(hu + du, y, hv + dv, Blocks.AIR.defaultBlockState());
            }
        }
    }

    /** A dome of the keep's tiles over its hall, on the crag's top, with its golden finial. */
    private static void dome(Frame f, int hu, int hv, Look look) {
        int base = f.surface(hu, hv);
        BlockState shell = look.front() == Front.TEMPLE ? b(Blocks.GOLD_BLOCK) : look.trim();
        for (int du = -7; du <= 7; du++) for (int dv = -7; dv <= 7; dv++) for (int dy = 0; dy <= 7; dy++) {
            double d = Math.sqrt(du * du + dv * dv + dy * dy * 1.3);
            if (d <= 7 && d > 6) f.set(hu + du, base + dy, hv + dv, shell);
        }
        for (int dy = 7; dy <= 10; dy++) f.set(hu, base + dy, hv, dy == 10 ? b(Blocks.LIGHTNING_ROD) : b(Blocks.GOLD_BLOCK));
    }

    // ------------------------------------------------------------------------------------------------ rooms

    /** A room carved out: walls of the builders' stone with posts of trim every six, its floor, lights in its roof (none when open). */
    private static void room(Frame f, int u0, int y, int v0, int u1, int v1, int height, Look look) {
        int h = height == 0 ? 12 : height;
        for (int u = u0 - 1; u <= u1 + 1; u++) {
            for (int v = v0 - 1; v <= v1 + 1; v++) {
                boolean edge = u < u0 || u > u1 || v < v0 || v > v1;
                boolean post = edge && (Math.floorMod(u - u0, 6) == 0 || Math.floorMod(v - v0, 6) == 0);
                f.set(u, y - 1, v, look.floor());
                for (int dy = 0; dy < h; dy++) f.set(u, y + dy, v, edge ? (post ? look.trim() : look.wall()) : Blocks.AIR.defaultBlockState());
                if (height == 0) continue;
                boolean lamp = !edge && Math.floorMod(u - u0, 5) == 2 && Math.floorMod(v - v0, 5) == 2;
                f.set(u, y + h, v, lamp ? b(Blocks.SHROOMLIGHT) : look.wall());
            }
        }
    }

    /** A round hall carved out, its wall of the builders' stone (no roof when open). */
    private static void round(Frame f, int cu, int cv, int y, int r, int height, Look look) {
        int h = height == 0 ? 12 : height;
        for (int du = -r - 1; du <= r + 1; du++) {
            for (int dv = -r - 1; dv <= r + 1; dv++) {
                double d = Math.sqrt(du * du + dv * dv);
                if (d > r + 1.2) continue;
                boolean edge = d > r;
                f.set(cu + du, y - 1, cv + dv, look.floor());
                for (int dy = 0; dy < h; dy++) {
                    boolean post = edge && Math.floorMod((int) Math.round(Math.atan2(dv, du) * 12), 3) == 0;
                    f.set(cu + du, y + dy, cv + dv, edge ? (post ? look.trim() : look.wall()) : Blocks.AIR.defaultBlockState());
                }
                if (height == 0) continue;
                boolean lamp = !edge && Math.floorMod(du, 6) == 3 && Math.floorMod(dv, 6) == 3;
                f.set(cu + du, y + h, cv + dv, lamp ? b(Blocks.SHROOMLIGHT) : look.wall());
            }
        }
    }

    /** Air only, on a floor: a way cut through what stands in a hall (its tiers), with no walls of its own. */
    private static void cut(Frame f, int u0, int y, int v0, int u1, int v1, Look look) {
        for (int u = u0; u <= u1; u++) for (int v = v0; v <= v1; v++) {
            f.set(u, y - 1, v, look.floor());
            for (int dy = 0; dy < 5; dy++) f.set(u, y + dy, v, Blocks.AIR.defaultBlockState());
        }
    }

    /**
     * The way from the gate to the hall: straight, through a guards' room, when there is room for one before the hall;
     * otherwise round its side (along its flank, through the guards' room, and in from the side).
     */
    private static void route(Frame f, int hu, int hv, int near, int front, int y, Look look, Random random) {
        List<int[]> rooms = new ArrayList<>();
        int span = front - 1 - (near + 1);
        if (span >= 18) {
            int g1 = front - 6, g0 = Math.max(near + 6, g1 - 10);
            rooms.add(new int[]{-2, g1 + 1, 2, front - 1, 6});
            rooms.add(new int[]{-7, g0, 7, g1, 8, 1});
            rooms.add(new int[]{8, (g0 + g1) / 2 - 2, 11, (g0 + g1) / 2 + 2, 5}); // a niche with treasure
            rooms.add(new int[]{-2, near + 2, 2, g0 - 1, 6});
            for (int[] r : rooms) room(f, r[0], y, r[1], r[2], r[3], r[4], look);
            cut(f, -2, y, near - 5, 2, near + 1, look);
        } else {
            int w = look.radius() + 8; // the flank's passage, beyond the hall's side wall
            rooms.add(new int[]{-2, front - 5, 2, front - 1, 6});
            rooms.add(new int[]{-w - 2, front - 5, -3, front - 1, 6});
            rooms.add(new int[]{-w - 7, hv + 4, -w + 3, Math.max(hv + 12, front - 6), 8, 1});
            rooms.add(new int[]{-w - 4, hv - 2, -w, hv + 3, 6});
            rooms.add(new int[]{-w - 11, hv + 6, -w - 8, hv + 10, 5}); // a niche with treasure
            int side = -look.radius() - 1; // the hall's side wall
            rooms.add(new int[]{-w + 1, hv - 2, side - 1, hv + 2, 6});
            for (int[] r : rooms) if (r[2] >= r[0] && r[3] >= r[1]) room(f, r[0], y, r[1], r[2], r[3], r[4], look);
            cut(f, side - 1, y, hv - 2, side + 6, hv + 2, look);
        }
        for (int[] r : rooms) open(f, r, y);
        for (int[] r : rooms) if (r.length > 5) guards(f, r, y, look, random);
        for (int[] r : rooms) if (r[2] - r[0] == 3 && r[3] - r[1] == 4) DungeonBuilder.chest(f.level, f.x((r[0] + r[2]) / 2, (r[1] + r[3]) / 2), y,
                f.z((r[0] + r[2]) / 2, (r[1] + r[3]) / 2), "sofe:chests/dungeon_medium", random);
    }

    /** Opens a room into the ones it touches: a wall block with air on both sides across it is a doorway. */
    private static void open(Frame f, int[] r, int y) {
        for (int u = r[0] - 1; u <= r[2] + 1; u++) {
            for (int v = r[1] - 1; v <= r[3] + 1; v++) {
                boolean edge = u < r[0] || u > r[2] || v < r[1] || v > r[3];
                if (!edge) continue;
                boolean throughU = f.air(u - 1, y, v) && f.air(u + 1, y, v), throughV = f.air(u, y, v - 1) && f.air(u, y, v + 1);
                if (!throughU && !throughV) continue;
                for (int dy = 0; dy < 5; dy++) f.set(u, y + dy, v, Blocks.AIR.defaultBlockState());
            }
        }
    }

    /** A guards' room: its two spawners (the creatures of the land), a chest, braziers in its corners, racks, a light from the roof. */
    private static void guards(Frame f, int[] r, int y, Look look, Random random) {
        int mu = (r[0] + r[2]) / 2, mv = (r[1] + r[3]) / 2;
        EntityType<?>[] kin = switch (look.front()) {
            case PERSIAN -> new EntityType<?>[]{EntityRegistry.MIRAGE_DANCER.get(), EntityType.CAVE_SPIDER};
            case EGYPTIAN -> new EntityType<?>[]{EntityRegistry.BOG_MUMMY.get(), EntityType.HUSK};
            case ROMAN -> new EntityType<?>[]{EntityRegistry.GILDED_LEGIONNAIRE.get(), EntityType.SKELETON};
            case TEMPLE -> new EntityType<?>[]{EntityRegistry.VOID_ZOMBIE.get(), EntityRegistry.VOID_SKELETON.get()};
            default -> new EntityType<?>[]{EntityRegistry.DRAUGR.get(), look.hall() == Hall.ARENA ? EntityType.STRAY : EntityType.SKELETON};
        };
        DungeonBuilder.spawner(f.level, f.x(r[0] + 2, mv), y, f.z(r[0] + 2, mv), kin[0]);
        DungeonBuilder.spawner(f.level, f.x(r[2] - 2, mv), y, f.z(r[2] - 2, mv), kin[1]);
        DungeonBuilder.chest(f.level, f.x(mu + 2, r[1]), y, f.z(mu + 2, r[1]), "sofe:chests/dungeon_small", random);
        BlockState corner = look.front() == Front.NORDIC ? s(SoFEBlocks.NORDRATH_IRON_BRAZIER) : look.glow();
        for (int[] c : new int[][]{{r[0], r[1]}, {r[2], r[1]}, {r[0], r[3]}, {r[2], r[3]}}) f.set(c[0], y, c[1], corner);
        for (int u = r[0] + 2; u <= r[2] - 2; u += 3) {
            if (Math.abs(u - mu) <= 2) continue;
            f.set(u, y, r[1], b(Blocks.SPRUCE_FENCE));
            f.set(u, y + 1, r[1], b(Blocks.LIGHTNING_ROD));
        }
        f.set(mu, y + r[4] - 1, mv, b(Blocks.SHROOMLIGHT));
    }

    // ------------------------------------------------------------------------------------------------ outside

    private static BlockState flame(Look look) {
        if (look.hall() == Hall.ARENA) return b(Blocks.RED_CANDLE).setValue(CandleBlock.CANDLES, 4).setValue(CandleBlock.LIT, true);
        if (look.light().getBlock() instanceof CampfireBlock) return look.light().setValue(CampfireBlock.SIGNAL_FIRE, false);
        return look.light();
    }

    /** The terrace before the front (where the Waystone and its runes stand), its parapet and lights, its guardians, and the stair down over the moat. */
    private static void terrace(Frame f, int front, int floor, int ground, Look look, Random random) {
        int deep = 17, half = 11;
        BlockState rail = switch (look.front()) {
            case PERSIAN -> s(SoFEBlocks.PARSIVAN_WHITE_PLASTER_WALL);
            case EGYPTIAN -> s(SoFEBlocks.KHEMET_SANDSTONE_WALL);
            case ROMAN -> look.ruined() ? b(Blocks.DEEPSLATE_TILE_WALL) : s(SoFEBlocks.AUREUM_MARBLE_BRICK_WALL);
            case TEMPLE -> s(SoFEBlocks.SULTHARI_SANDSTONE_BRICK_WALL);
            default -> s(SoFEBlocks.NORDRATH_RUNESTONE_BRICK_WALL);
        };
        for (int u = -half; u <= half; u++) {
            for (int v = front + 1; v <= front + deep; v++) {
                for (int y = Math.min(ground, floor) - 6; y < floor; y++) f.set(u, y, v, y == floor - 1 ? look.floor() : look.rock().get(0));
                for (int y = floor; y < floor + 8; y++) f.set(u, y, v, Blocks.AIR.defaultBlockState());
                boolean rim = Math.abs(u) == half || v == front + deep;
                boolean stair = v == front + deep && Math.abs(u) <= 2;
                if (rim && !stair) f.set(u, floor, v, Math.floorMod(u + v, 2) == 0 ? look.wall() : rail);
            }
        }
        for (int[] c : new int[][]{{-half, front + deep}, {half, front + deep}, {-half, front + 1}, {half, front + 1}, {-3, front + deep}, {3, front + deep}}) {
            f.set(c[0], floor, c[1], look.wall());
            f.set(c[0], floor + 1, c[1], look.trim());
            f.set(c[0], floor + 2, c[1], flame(look));
        }
        switch (look.front()) {
            case NORDIC -> {
                for (int du : new int[]{-8, 8}) {
                    warrior(f, du, floor, front + 5, look);
                    for (int i = 0; i < 2; i++) {
                        f.set(du, floor, front + 8 + i * 2, b(Blocks.SPRUCE_FENCE));
                        f.set(du, floor + 1, front + 8 + i * 2, b(Blocks.LIGHTNING_ROD));
                    }
                }
            }
            case EGYPTIAN -> {
                for (int du : new int[]{-7, 7}) {
                    sphinx(f, du, floor, front + 4, look);
                    for (int dy = 0; dy < 9; dy++) f.set(du, floor + dy, front + 12, dy == 8 ? b(Blocks.GOLD_BLOCK) : s(SoFEBlocks.KHEMET_OBELISK));
                }
            }
            case ROMAN -> {
                for (int du : new int[]{-8, 8}) {
                    statue(f, du, floor, front + 5, look);
                    f.set(du, floor, front + 11, look.trim());
                    f.set(du, floor + 1, front + 11, b(Blocks.CAMPFIRE));
                }
            }
            default -> { // Persian and the temple: lamps on posts and potted trees
                for (int du : new int[]{-8, 8}) {
                    for (int dy = 0; dy < 3; dy++) f.set(du, floor + dy, front + 5, dy == 2 ? look.trim() : look.wall());
                    f.set(du, floor + 3, front + 5, b(Blocks.LANTERN));
                    f.set(du, floor, front + 10, b(Blocks.FLOWERING_AZALEA));
                    f.set(du, floor, front + 12, b(Blocks.POTTED_FLOWERING_AZALEA));
                }
            }
        }
        // the stair down over the moat, five wide, with lights and piers
        int steps = Math.max(0, floor - ground);
        BlockState stairs = look.stairs().setValue(StairBlock.FACING, Direction.NORTH);
        for (int i = 0; i < steps; i++) {
            int v = front + deep + 1 + i, y = floor - 1 - i;
            for (int du = -3; du <= 3; du++) {
                int below = Math.abs(du) == 3 && i % 4 == 0 ? ground - 9 : y - 3;
                for (int yy = below; yy < y; yy++) f.set(du, yy, v, Math.abs(du) == 3 ? look.wall() : look.rock().get(0));
                if (Math.abs(du) == 3) {
                    f.set(du, y, v, look.wall());
                    f.set(du, y + 1, v, i % 4 == 0 ? flame(look) : Blocks.AIR.defaultBlockState());
                } else {
                    f.set(du, y, v, stairs);
                    for (int yy = y + 1; yy < y + 5; yy++) f.set(du, yy, v, Blocks.AIR.defaultBlockState());
                }
            }
        }
    }

    private static void warrior(Frame f, int u, int y, int v, Look look) {
        BlockState stone = b(Blocks.POLISHED_ANDESITE);
        f.set(u, y, v, look.trim());
        f.set(u, y + 1, v, stone);
        f.set(u, y + 2, v, stone);
        for (int du = -1; du <= 1; du++) f.set(u + du, y + 3, v, du == 0 ? b(Blocks.POLISHED_DEEPSLATE) : stone);
        f.set(u, y + 4, v, b(Blocks.CHISELED_STONE_BRICKS));
        f.set(u, y + 5, v, look.hall() == Hall.ARENA ? b(Blocks.RED_NETHER_BRICK_WALL) : b(Blocks.STONE_BRICK_WALL));
        f.set(u + 1, y + 4, v, b(Blocks.LIGHTNING_ROD));
    }

    /** A sphinx lying on its plinth, facing out: paws, body, haunches and a head in the nemes. */
    private static void sphinx(Frame f, int u, int y, int v, Look look) {
        for (int dv = -3; dv <= 1; dv++) f.set(u, y, v + dv, look.trim());
        for (int dv = -3; dv <= 0; dv++) f.set(u, y + 1, v + dv, b(Blocks.SMOOTH_SANDSTONE));
        f.set(u, y + 1, v + 1, b(Blocks.SMOOTH_SANDSTONE_SLAB));
        f.set(u, y + 2, v - 3, b(Blocks.SMOOTH_SANDSTONE_SLAB));
        f.set(u, y + 2, v, b(Blocks.CHISELED_SANDSTONE));
        f.set(u, y + 3, v, s(SoFEBlocks.KHEMET_GOLD_HIEROGLYPHS));
    }

    /** A statue of a senator or a legionary on a pedestal. */
    private static void statue(Frame f, int u, int y, int v, Look look) {
        BlockState body = look.ruined() ? b(Blocks.POLISHED_DEEPSLATE) : b(Blocks.QUARTZ_BLOCK);
        f.set(u, y, v, look.trim());
        f.set(u, y + 1, v, look.wall());
        f.set(u, y + 2, v, body);
        f.set(u, y + 3, v, body);
        f.set(u - 1, y + 3, v, look.ruined() ? b(Blocks.DEEPSLATE_TILE_WALL) : b(Blocks.QUARTZ_SLAB));
        f.set(u, y + 4, v, look.ruined() ? b(Blocks.CHISELED_DEEPSLATE) : b(Blocks.CHISELED_QUARTZ_BLOCK));
        f.set(u + 1, y + 3, v, b(Blocks.LIGHTNING_ROD));
    }

    /** The keep's front, carved into the crag in the style of its empire and standing out of it in layers. */
    private static void facade(Frame f, int front, int floor, Look look, Random random) {
        int v = front;
        BlockState up = look.stairs().setValue(StairBlock.FACING, Direction.NORTH);
        BlockState over = up.setValue(StairBlock.HALF, Half.TOP);
        // the face and the gate's opening (every style)
        for (int u = -10; u <= 10; u++) for (int dy = -1; dy <= 15; dy++) f.set(u, floor + dy, v, dy == 0 || dy == 8 || dy == 15 ? look.trim() : look.wall());
        for (int dy = 0; dy <= 3; dy++) for (int du = -1; du <= 1; du++) f.set(du, floor + dy, v, Blocks.AIR.defaultBlockState());
        switch (look.front()) {
            case NORDIC -> nordic(f, v, floor, look, up, over);
            case PERSIAN, TEMPLE -> iwan(f, v, floor, look, up, over);
            case EGYPTIAN -> pylon(f, v, floor, look, up, over);
            case ROMAN -> portico(f, v, floor, look, up, over, random);
        }
        emblem(f, floor, v, look);
        if (look.hall() == Hall.CITADEL) { // falls of lava down the face
            for (int du : new int[]{-12, 12}) {
                for (int dy = 0; dy <= 16; dy++) f.set(du, floor + dy, v, look.wall());
                f.set(du, floor + 17, v, b(Blocks.LAVA));
                f.set(du, floor + 16, v + 1, b(Blocks.LAVA));
            }
        }
    }

    private static void nordic(Frame f, int v, int floor, Look look, BlockState up, BlockState over) {
        for (int du = -1; du <= 1; du++) f.set(du, floor + 4, v, b(Blocks.IRON_BARS));
        for (int side : new int[]{-1, 1}) {
            for (int w = 9; w <= 10; w++) { // the buttresses
                int u = side * w;
                for (int dy = 0; dy <= 14; dy++) f.set(u, floor + dy, v + 1, dy == 0 || dy == 14 ? look.trim() : look.wall());
                for (int dy = 0; dy <= 9; dy++) f.set(u, floor + dy, v + 2, dy == 0 ? look.trim() : look.wall());
                f.set(u, floor + 10, v + 2, up);
                f.set(u, floor + 15, v + 1, up);
            }
            int u = side * 6; // the pillars of runes
            f.set(u, floor, v + 2, up);
            for (int dy = 0; dy <= 8; dy++) f.set(u, floor + dy, v + 1, dy == 0 ? look.trim() : dy % 2 == 1 && dy < 8 ? look.glow() : look.wall());
            f.set(u, floor + 9, v + 1, look.trim());
            f.set(u, floor + 9, v + 2, over);
            f.set(u, floor + 10, v + 1, s(SoFEBlocks.NORDRATH_IRON_BRAZIER));
            for (int dy = 0; dy <= 4; dy++) f.set(side * 2, floor + dy, v + 1, look.trim()); // the arch in three planes
            for (int dy = 0; dy <= 6; dy++) f.set(side * 3, floor + dy, v + 2, look.wall());
            f.set(side * 3, floor + 7, v + 2, over);
            f.set(side * 2, floor + 7, v + 2, look.wall());
        }
        for (int du = -2; du <= 2; du++) f.set(du, floor + 5, v + 1, look.trim());
        for (int du = -1; du <= 1; du++) f.set(du, floor + 7, v + 2, look.wall());
        for (int du = -2; du <= 2; du++) {
            f.set(du, floor + 8, v + 2, over);
            f.set(du, floor + 12, v + 2, over);
        }
        for (int du = -7; du <= 7; du++) {
            if (Math.abs(du) <= 2) continue;
            f.set(du, floor + 12, v + 1, look.trim());
            f.set(du, floor + 11, v + 1, over);
        }
        for (int du : new int[]{-7, 7}) f.set(du, floor + 13, v + 1, flame(look));
        for (int u = -11; u <= 11; u++) f.set(u, floor + 16, v + 1, over);
    }

    /** A Persian iwan: a tall pointed arch stepping back to the gate, muqarnas under its crown, tile bands, and two minarets. */
    private static void iwan(Frame f, int v, int floor, Look look, BlockState up, BlockState over) {
        // the frame of the iwan: two planes out from the face, and its pointed crown
        for (int side : new int[]{-1, 1}) {
            for (int dy = 0; dy <= 12; dy++) {
                f.set(side * 5, floor + dy, v + 1, dy % 3 == 0 ? look.trim() : look.wall());
                f.set(side * 6, floor + dy, v + 2, look.wall());
                f.set(side * 7, floor + dy, v + 2, look.trim());
            }
            for (int step = 0; step < 4; step++) { // the pointed arch: each course one in towards the middle
                int u = side * (4 - step), y = floor + 9 + step;
                f.set(u, y, v + 1, over.setValue(StairBlock.FACING, side < 0 ? Direction.WEST : Direction.EAST));
                f.set(u, y + 1, v + 1, look.wall());
            }
            for (int dy = 0; dy <= 22; dy++) { // the minaret, a slender tower with its balcony and dome
                int y = floor + dy;
                for (int du = 0; du <= 1; du++) for (int dv = 0; dv <= 1; dv++) {
                    BlockState block = dy % 5 == 4 ? look.trim() : look.wall();
                    f.set(side * (12 + du), y, v + 1 + dv, block);
                }
            }
            for (int du = -1; du <= 2; du++) for (int dv = 0; dv <= 3; dv++) { // the balcony round it
                if (du >= 0 && du <= 1 && dv >= 1 && dv <= 2) continue;
                f.set(side * (12 + du), floor + 17, v + dv, s(SoFEBlocks.PARSIVAN_WHITE_PLASTER_SLAB));
            }
            for (int du = 0; du <= 1; du++) for (int dv = 0; dv <= 1; dv++) f.set(side * (12 + du), floor + 23, v + 1 + dv, look.front() == Front.TEMPLE ? b(Blocks.GOLD_BLOCK) : look.trim());
            f.set(side * 12, floor + 24, v + 1, b(Blocks.GOLD_BLOCK));
            f.set(side * 12, floor + 25, v + 1, b(Blocks.LIGHTNING_ROD));
        }
        for (int du = -1; du <= 1; du++) f.set(du, floor + 13, v + 1, look.wall());
        for (int du = -4; du <= 4; du++) { // the muqarnas: rows of upturned steps under the crown, in the recess
            f.set(du, floor + 8, v, over);
            if (Math.abs(du) <= 3) f.set(du, floor + 9, v, look.trim());
        }
        for (int u = -11; u <= 11; u++) { // a band of tiles and the crenellations along the top
            f.set(u, floor + 14, v + 1, look.trim());
            if (u % 2 == 0) f.set(u, floor + 16, v, look.wall());
        }
        f.set(-3, floor + 6, v + 1, b(Blocks.LANTERN).setValue(LanternBlock.HANGING, true));
        f.set(3, floor + 6, v + 1, b(Blocks.LANTERN).setValue(LanternBlock.HANGING, true));
        for (int dy = 0; dy <= 6; dy++) for (int side : new int[]{-1, 1}) f.set(side * 2, floor + dy, v + 1, look.trim()); // the gate's jambs
        for (int du = -2; du <= 2; du++) f.set(du, floor + 7, v + 1, look.trim());
    }

    /** An Egyptian pylon: two battered towers of carved stone with bands of gold glyphs, the gate between them under a winged sun. */
    private static void pylon(Frame f, int v, int floor, Look look, BlockState up, BlockState over) {
        for (int side : new int[]{-1, 1}) {
            for (int dy = 0; dy <= 18; dy++) {
                int inset = dy / 5; // the towers lean in as they rise
                for (int w = 3; w <= 13 - inset; w++) {
                    for (int dv = 1; dv <= 3 - Math.min(2, inset); dv++) {
                        boolean glyph = (w == 6 || w == 10) && dy > 1 && dy < 16;
                        f.set(side * w, floor + dy, v + dv, dy == 18 || dy == 17 ? look.trim() : glyph ? s(SoFEBlocks.KHEMET_GOLD_HIEROGLYPHS) : look.wall());
                    }
                }
            }
            for (int dy = 0; dy < 20; dy++) f.set(side * 8, floor + dy, v + 4, dy == 19 ? b(Blocks.GOLD_BLOCK) : b(Blocks.SPRUCE_FENCE)); // the flagstaffs
            for (int dy = 14; dy <= 17; dy++) f.set(side * 8, floor + dy, v + 5, b(Blocks.BLUE_WOOL));
        }
        for (int du = -2; du <= 2; du++) for (int dy = 0; dy <= 10; dy++) {
            if (Math.abs(du) <= 1 && dy <= 3) continue;
            f.set(du, floor + dy, v + 1, dy >= 9 ? look.trim() : look.wall());
        }
        // the winged sun over the gate: a disc of gold, wings of gold and lapis
        f.set(0, floor + 12, v + 2, b(Blocks.GOLD_BLOCK));
        for (int du = 1; du <= 5; du++) for (int side : new int[]{-1, 1}) {
            f.set(side * du, floor + 12 + (du >= 4 ? 1 : 0), v + 2, du % 2 == 1 ? b(Blocks.GOLD_BLOCK) : b(Blocks.LAPIS_BLOCK));
        }
        for (int u = -2; u <= 2; u++) f.set(u, floor + 11, v + 2, over);
    }

    /** A Roman portico: steps, a row of columns, the architrave with its golden frieze and the pediment over it (broken, on a ruin). */
    private static void portico(Frame f, int v, int floor, Look look, BlockState up, BlockState over, Random random) {
        BlockState column = look.ruined() ? s(SoFEBlocks.CORRUPTED_AUREUM_MARBLE_PILLAR) : s(SoFEBlocks.AUREUM_MARBLE_PILLAR);
        if (look.ruined() && column.isAir()) column = b(Blocks.POLISHED_BASALT);
        for (int u = -10; u <= 10; u++) for (int dv = 1; dv <= 4; dv++) f.set(u, floor - 1, v + dv, look.floor()); // the portico's floor
        for (int u : new int[]{-9, -6, -3, 3, 6, 9}) {
            int height = look.ruined() && random.nextInt(3) == 0 ? 4 + random.nextInt(4) : 10;
            f.set(u, floor, v + 3, look.trim());
            for (int dy = 1; dy < height; dy++) f.set(u, floor + dy, v + 3, column);
            if (height == 10) f.set(u, floor + 10, v + 3, look.ruined() ? b(Blocks.CHISELED_DEEPSLATE) : b(Blocks.CHISELED_QUARTZ_BLOCK));
        }
        for (int u = -11; u <= 11; u++) {
            for (int dv = 0; dv <= 4; dv++) {
                if (look.ruined() && dv == 4 && random.nextInt(3) == 0) continue;
                f.set(u, floor + 11, v + dv, look.wall()); // the architrave and the portico's roof
            }
            f.set(u, floor + 12, v + 4, look.trim()); // the golden frieze
        }
        for (int row = 0; row < 7; row++) { // the pediment
            int w = 11 - row * 2 + (row == 0 ? 0 : 1);
            if (w < 0) break;
            for (int u = -w; u <= w; u++) {
                if (look.ruined() && row > 2 && u > 0) continue; // half of it fallen
                boolean edge = Math.abs(u) >= w - 1;
                BlockState block = edge ? up.setValue(StairBlock.FACING, u < 0 ? Direction.EAST : Direction.WEST) : row % 2 == 0 ? look.wall() : look.trim();
                f.set(u, floor + 13 + row, v + 4, block);
                for (int dv = 0; dv < 4; dv++) f.set(u, floor + 13 + row, v + dv, look.wall());
            }
        }
        for (int du = -1; du <= 1; du++) f.set(du, floor + 4, v, look.trim());
    }

    /** The keep's sign over its gate, after its boss. */
    private static void emblem(Frame f, int floor, int v, Look look) {
        int y = switch (look.front()) { case EGYPTIAN -> floor + 7; case ROMAN -> floor + 7; case PERSIAN, TEMPLE -> floor + 11; default -> floor + 10; };
        int dv = look.front() == Front.NORDIC ? 1 : look.front() == Front.ROMAN ? 0 : 1;
        for (int du = -1; du <= 1; du++) f.set(du, y, v + dv, look.trim());
        BlockState mark = switch (look.hall()) {
            case FORGE -> b(Blocks.ANVIL);
            case ARENA -> b(Blocks.SKELETON_SKULL);
            case CITADEL -> b(Blocks.WITHER_SKELETON_SKULL);
            case BATHS -> b(Blocks.AMETHYST_CLUSTER);
            case CARAVANSERAI, CATACOMBS -> b(Blocks.SOUL_LANTERN);
            case GARDENS -> b(Blocks.FLOWERING_AZALEA);
            case MARSH -> b(Blocks.VERDANT_FROGLIGHT);
            case TREASURY, VAULTS, MARKET -> b(Blocks.GOLD_BLOCK);
            case COLOSSEUM -> b(Blocks.LIGHTNING_ROD);
            case SENATE -> b(Blocks.EMERALD_BLOCK);
            case TEMPLE -> b(Blocks.GLOWSTONE);
        };
        f.set(0, y + 1, v + dv, mark);
        f.set(-1, y + 1, v + dv, look.glow());
        f.set(1, y + 1, v + dv, look.glow());
    }

    // ------------------------------------------------------------------------------------------------ the halls

    /** The hall's own things, after its boss. */
    private static void hall(Frame f, int cu, int cv, int y, Look look, Random random) {
        switch (look.hall()) {
            case FORGE -> forge(f, cu, cv, y, look, random);
            case ARENA -> tiers(f, cu, cv, y, look, random, 12, b(Blocks.RED_CONCRETE), b(Blocks.RED_TERRACOTTA), true);
            case CITADEL -> citadel(f, cu, cv, y, look, random);
            case BATHS -> baths(f, cu, cv, y, look, random);
            case CARAVANSERAI -> caravanserai(f, cu, cv, y, look, random);
            case GARDENS -> gardens(f, cu, cv, y, look, random);
            case CATACOMBS -> catacombs(f, cu, cv, y, look, random);
            case MARSH -> marsh(f, cu, cv, y, look, random);
            case TREASURY -> treasury(f, cu, cv, y, look, random);
            case MARKET -> market(f, cu, cv, y, look, random);
            case VAULTS -> vaults(f, cu, cv, y, look, random);
            case COLOSSEUM -> tiers(f, cu, cv, y, look, random, 20, b(Blocks.SAND), b(Blocks.SANDSTONE), false);
            case SENATE -> senate(f, cu, cv, y, look, random);
            case TEMPLE -> temple(f, cu, cv, y, look, random);
        }
    }

    /** Kaleth's forge: lava in channels with bridges, the giant anvil on its dais, forges, a gallery and a fall of lava. */
    private static void forge(Frame f, int cu, int cv, int y, Look look, Random random) {
        BlockState lava = b(Blocks.LAVA), rim = b(Blocks.POLISHED_BLACKSTONE), bridge = b(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB);
        int ru = look.radius() - 1, rv = look.depth() - 1;
        for (int u = cu - ru; u <= cu + ru; u++) for (int v = cv - rv; v <= cv + rv; v++) {
            boolean across = Math.abs(v - cv) <= 1 && Math.abs(u - cu) > 6, along = Math.abs(u - cu) == 11 || Math.abs(u - cu) == 12;
            if (!(across || along)) continue;
            boolean over = across && Math.abs(u - cu) % 6 == 0 || along && Math.floorMod(v - cv, 7) == 0;
            f.set(u, y - 2, v, rim);
            f.set(u, y - 1, v, over ? rim : lava);
            if (over) f.set(u, y, v, bridge);
        }
        for (int step = 0; step < 3; step++) {
            int r = 6 - step * 2;
            for (int du = -r; du <= r; du++) for (int dv = -r + 1; dv <= r - 1; dv++) f.set(cu + du, y + step, cv + dv, step == 2 ? look.floor() : rim);
        }
        int a = y + 3;
        for (int du = -1; du <= 1; du++) for (int dv = -1; dv <= 1; dv++) f.set(cu + du, a, cv + dv, b(Blocks.IRON_BLOCK));
        f.set(cu, a + 1, cv, b(Blocks.IRON_BLOCK));
        for (int du = -3; du <= 3; du++) for (int dv = -1; dv <= 1; dv++) f.set(cu + du, a + 2, cv + dv, Math.abs(du) == 3 ? b(Blocks.ANVIL) : b(Blocks.IRON_BLOCK));
        f.set(cu + 4, a + 2, cv, b(Blocks.ANVIL));
        for (int v = cv - rv + 2; v <= cv + rv - 2; v += 6) {
            for (int side : new int[]{-1, 1}) {
                int u = cu + side * (ru + 0);
                f.set(u, y, v, b(Blocks.BLAST_FURNACE));
                f.set(u, y + 1, v, b(Blocks.IRON_BARS));
                f.set(u + side, y + 1, v, lava);
                f.set(u, y + 2, v, look.trim());
                f.set(u - side, y, v, b(Blocks.ANVIL));
                f.set(u + side, y + 5, v, look.glow());
            }
        }
        for (int u = cu - ru; u <= cu + ru; u++) for (int v = cv - rv; v <= cv + rv; v++) {
            boolean ledge = v >= cv + rv - 2 || Math.abs(u - cu) >= ru - 2;
            if (!ledge) continue;
            f.set(u, y + 7, v, s(SoFEBlocks.NORDRATH_DARK_PLANKS));
            boolean edge = v == cv + rv - 2 && Math.abs(u - cu) < ru - 2 || Math.abs(u - cu) == ru - 2 && v < cv + rv - 2;
            if (edge) f.set(u, y + 8, v, b(Blocks.SPRUCE_FENCE));
        }
        for (int du = -2; du <= 2; du++) {
            f.set(cu + du, y + 13, cv - rv - 1, lava);
            f.set(cu + du, y - 1, cv - rv, lava);
            f.set(cu + du, y - 2, cv - rv, rim);
        }
        for (int i = 0; i < 10; i++) {
            int u = cu - ru + 2 + random.nextInt(2 * ru - 3), v = cv - rv + 1 + random.nextInt(2 * rv - 2);
            for (int dy = 14; dy > 10; dy--) f.set(u, y + dy, v, b(Blocks.CHAIN));
        }
    }

    /** A round pit with tiers of seats about it and watching skulls, its dais in the middle over a floor of the colours given. */
    private static void tiers(Frame f, int cu, int cv, int y, Look look, Random random, int pit, BlockState ground, BlockState patch, boolean blood) {
        int r = look.radius() - 1;
        for (int du = -r; du <= r; du++) for (int dv = -r; dv <= r; dv++) {
            double d = Math.sqrt(du * du + dv * dv);
            if (d > r + 0.5) continue;
            int u = cu + du, v = cv + dv;
            if (d <= 4) f.set(u, y, v, d > 3 ? look.trim() : Math.floorMod(du + dv, 2) == 0 ? b(Blocks.POLISHED_BLACKSTONE) : b(Blocks.GILDED_BLACKSTONE));
            else if (d <= pit) {
                BlockState floor = Precinct.patches(u * 2, v * 2) > 0.2 ? ground : Precinct.patches(u, v) > -0.3 ? patch : blood ? b(Blocks.NETHER_WART_BLOCK) : b(Blocks.GRAVEL);
                f.set(u, y - 1, v, d > pit - 0.7 ? look.trim() : floor);
            } else {
                int tier = Math.min(look.hall() == Hall.COLOSSEUM ? 9 : 6, (int) ((d - pit) * 0.8));
                for (int dy = 0; dy <= tier; dy++) f.set(u, y + dy, v, look.wall());
                if (tier >= 2 && random.nextInt(14) == 0) f.set(u, y + tier + 1, v, b(Blocks.SKELETON_SKULL));
            }
        }
        for (int i = 0; i < 10; i++) {
            double ang = i * Math.PI / 5;
            int u = cu + (int) Math.round(Math.cos(ang) * (pit + 0.5)), v = cv + (int) Math.round(Math.sin(ang) * (pit + 0.5));
            f.set(u, y, v, look.wall());
            f.set(u, y + 1, v, blood ? b(Blocks.RED_CANDLE).setValue(CandleBlock.CANDLES, 4).setValue(CandleBlock.LIT, true) : b(Blocks.CAMPFIRE));
        }
        if (look.hall() == Hall.COLOSSEUM) { // broken columns in the sand, mirrors about the dais, weapon racks
            for (int i = 0; i < 8; i++) {
                double ang = i * Math.PI / 4 + 0.4;
                int u = cu + (int) Math.round(Math.cos(ang) * 10), v = cv + (int) Math.round(Math.sin(ang) * 10), h = 2 + random.nextInt(5);
                for (int dy = 0; dy < h; dy++) f.set(u, y + dy, v, s(SoFEBlocks.AUREUM_MARBLE_PILLAR));
                f.set(cu + (int) Math.round(Math.cos(ang) * 5), y + 1, cv + (int) Math.round(Math.sin(ang) * 5), b(Blocks.GLASS_PANE));
                f.set(cu + (int) Math.round(Math.cos(ang) * 5), y + 2, cv + (int) Math.round(Math.sin(ang) * 5), b(Blocks.GLASS_PANE));
            }
        } else {
            for (int i = 0; i < 6; i++) {
                double ang = i * Math.PI / 3 + 0.3;
                int u = cu + (int) Math.round(Math.cos(ang) * r), v = cv + (int) Math.round(Math.sin(ang) * r);
                for (int dy = 9; dy <= 12; dy++) f.set(u, y + dy, v, b(Blocks.RED_WOOL));
                f.set(u, y + 13, v, s(SoFEBlocks.NORDRATH_IRON_BRAZIER));
            }
        }
    }

    /** Vorath's hall: rings of lava about the dais, four bridges, tiers, red banners, iron trusses hung with chains. */
    private static void citadel(Frame f, int cu, int cv, int y, Look look, Random random) {
        int r = look.radius() - 1;
        for (int du = -r; du <= r; du++) for (int dv = -r; dv <= r; dv++) {
            double d = Math.sqrt(du * du + dv * dv);
            if (d > r + 0.5) continue;
            int u = cu + du, v = cv + dv;
            boolean bridge = Math.abs(du) <= 1 || Math.abs(dv) <= 1;
            if (d <= 6) f.set(u, y - 1, v, d > 5 ? b(Blocks.MAGMA_BLOCK) : Math.floorMod((int) d, 2) == 0 ? b(Blocks.BLACKSTONE) : b(Blocks.POLISHED_BLACKSTONE));
            else if (d <= 9.5) {
                f.set(u, y - 2, v, b(Blocks.BLACKSTONE));
                f.set(u, y - 1, v, bridge ? b(Blocks.POLISHED_BLACKSTONE_BRICKS) : b(Blocks.LAVA));
            } else if (d <= 13) f.set(u, y - 1, v, d > 12.3 ? b(Blocks.MAGMA_BLOCK) : look.floor());
            else {
                int tier = Math.min(7, (int) ((d - 13) * 0.9));
                for (int dy = 0; dy <= tier; dy++) f.set(u, y + dy, v, dy == tier ? b(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB) : look.wall());
            }
        }
        for (int i = -18; i <= 18; i++) {
            f.set(cu + i, y + 18, cv, b(Blocks.IRON_BARS));
            f.set(cu, y + 18, cv + i, b(Blocks.IRON_BARS));
            if (i % 6 == 0) for (int dy = 17; dy > 12; dy--) {
                f.set(cu + i, y + dy, cv, b(Blocks.CHAIN));
                f.set(cu, y + dy, cv + i, b(Blocks.CHAIN));
            }
        }
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4 + 0.2;
            int u = cu + (int) Math.round(Math.cos(ang) * r), v = cv + (int) Math.round(Math.sin(ang) * r);
            for (int dy = 10; dy <= 14; dy++) f.set(u, y + dy, v, b(Blocks.RED_WOOL));
            f.set(u, y + 15, v, s(SoFEBlocks.NORDRATH_IRON_BRAZIER));
        }
        for (int i = 0; i < 4; i++) {
            double ang = i * Math.PI / 2 + Math.PI / 4;
            int u = cu + (int) Math.round(Math.cos(ang) * 4), v = cv + (int) Math.round(Math.sin(ang) * 4);
            f.set(u, y, v, b(Blocks.NETHERRACK));
            f.set(u, y + 1, v, b(Blocks.FIRE));
        }
    }

    /** Mirael's baths: a ring of turquoise water about her dais, a ring of columns, violet lights, smaller pools in the walls' niches. */
    private static void baths(Frame f, int cu, int cv, int y, Look look, Random random) {
        int r = look.radius() - 1;
        for (int du = -r; du <= r; du++) for (int dv = -r; dv <= r; dv++) {
            double d = Math.sqrt(du * du + dv * dv);
            if (d > r + 0.5) continue;
            int u = cu + du, v = cv + dv;
            if (d <= 3) f.set(u, y, v, d > 2 ? look.trim() : s(SoFEBlocks.PARSIVAN_VIOLET_TILES));
            else if (d <= 8) {
                f.set(u, y - 3, v, look.trim());
                f.set(u, y - 2, v, b(Blocks.WATER));
                f.set(u, y - 1, v, b(Blocks.WATER));
                if (random.nextInt(9) == 0) f.set(u, y - 3, v, b(Blocks.SEA_LANTERN));
            } else if (d <= 9) f.set(u, y, v, look.trim());
        }
        for (int i = 0; i < 12; i++) {
            double ang = i * Math.PI / 6;
            int u = cu + (int) Math.round(Math.cos(ang) * (r - 1)), v = cv + (int) Math.round(Math.sin(ang) * (r - 1));
            for (int dy = 0; dy < 13; dy++) f.set(u, y + dy, v, dy == 0 || dy == 12 ? look.trim() : b(Blocks.QUARTZ_PILLAR));
            f.set(u, y + 6, v, b(Blocks.AMETHYST_BLOCK));
        }
        for (int i = 0; i < 6; i++) {
            double ang = i * Math.PI / 3 + 0.5;
            f.set(cu + (int) Math.round(Math.cos(ang) * 6), y + 11, cv + (int) Math.round(Math.sin(ang) * 6), b(Blocks.AMETHYST_BLOCK));
            f.set(cu + (int) Math.round(Math.cos(ang) * 6), y + 10, cv + (int) Math.round(Math.sin(ang) * 6), b(Blocks.LANTERN).setValue(LanternBlock.HANGING, true));
        }
    }

    /** Thessyn's caravanserai: a court under the sky with arcades about it, webs and silk strung across, crates, a dais wrapped in silk, poison-green lights. */
    private static void caravanserai(Frame f, int cu, int cv, int y, Look look, Random random) {
        int ru = look.radius() - 1, rv = look.depth() - 1;
        for (int u = cu - ru; u <= cu + ru; u++) for (int v = cv - rv; v <= cv + rv; v++) {
            boolean arcade = Math.abs(u - cu) >= ru - 3 || Math.abs(v - cv) >= rv - 3;
            f.set(u, y - 1, v, Math.floorMod(u + v, 4) == 0 ? look.trim() : look.floor());
            if (arcade) f.set(u, y + 6, v, look.wall()); // the arcades' roof
            boolean pillar = arcade && (Math.abs(u - cu) == ru - 3 || Math.abs(v - cv) == rv - 3) && Math.floorMod(u + v, 4) == 0;
            if (pillar) for (int dy = 0; dy < 6; dy++) f.set(u, y + dy, v, dy == 5 ? look.trim() : look.wall());
            if (arcade && !pillar && random.nextInt(14) == 0) f.set(u, y, v, random.nextBoolean() ? b(Blocks.BARREL) : b(Blocks.CHEST));
            if (random.nextInt(7) == 0) f.set(u, y + 3 + random.nextInt(6), v, b(Blocks.COBWEB));
        }
        for (int i = 0; i < 6; i++) { // silk strung across the court
            int v = cv - rv + 3 + i * (2 * rv - 6) / 5;
            for (int u = cu - ru + 3; u <= cu + ru - 3; u++) if (random.nextInt(3) > 0) f.set(u, y + 8, v, b(Blocks.WHITE_WOOL));
        }
        for (int du = -3; du <= 3; du++) for (int dv = -3; dv <= 3; dv++) {
            if (Math.abs(du) + Math.abs(dv) > 4) continue;
            f.set(cu + du, y, cv + dv, b(Blocks.WHITE_WOOL));
            if (random.nextInt(2) == 0) f.set(cu + du, y + 1, cv + dv, b(Blocks.COBWEB));
        }
        for (int[] c : new int[][]{{-ru + 3, -rv + 3}, {ru - 3, -rv + 3}, {-ru + 3, rv - 3}, {ru - 3, rv - 3}}) f.set(cu + c[0], y + 7, cv + c[1], look.glow());
    }

    /** Luxara's gardens under the moon: hedges and flowers, cherry trees, paths to a fountain on her dais, shards of mirror, pearl lights. */
    private static void gardens(Frame f, int cu, int cv, int y, Look look, Random random) {
        int r = look.radius() - 1;
        Block[] flowers = {Blocks.ALLIUM, Blocks.PINK_TULIP, Blocks.AZURE_BLUET, Blocks.LILY_OF_THE_VALLEY, Blocks.PINK_PETALS, Blocks.BLUE_ORCHID};
        for (int du = -r; du <= r; du++) for (int dv = -r; dv <= r; dv++) {
            double d = Math.sqrt(du * du + dv * dv);
            if (d > r + 0.5) continue;
            int u = cu + du, v = cv + dv;
            boolean path = Math.abs(du) <= 1 || Math.abs(dv) <= 1 || Math.abs(d - 10) < 0.8;
            f.set(u, y - 1, v, path ? b(Blocks.SMOOTH_QUARTZ) : b(Blocks.GRASS_BLOCK));
            if (d <= 4) f.set(u, y, v, d > 3 ? look.trim() : b(Blocks.WATER));
            else if (!path && Math.abs(d - 7) < 0.6) f.set(u, y, v, b(Blocks.FLOWERING_AZALEA_LEAVES).setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
            else if (!path && random.nextInt(3) == 0) f.set(u, y, v, b(flowers[random.nextInt(flowers.length)]));
        }
        for (int dy = 1; dy <= 3; dy++) f.set(cu, y + dy, cv, b(Blocks.QUARTZ_PILLAR));
        f.set(cu, y + 4, cv, b(Blocks.WATER));
        for (int i = 0; i < 4; i++) { // cherry trees
            double ang = i * Math.PI / 2 + Math.PI / 4;
            int u = cu + (int) Math.round(Math.cos(ang) * 14), v = cv + (int) Math.round(Math.sin(ang) * 14);
            for (int dy = 0; dy < 5; dy++) f.set(u, y + dy, v, b(Blocks.CHERRY_LOG));
            for (int du = -2; du <= 2; du++) for (int dv = -2; dv <= 2; dv++) for (int dy = 4; dy <= 6; dy++) {
                if (Math.abs(du) + Math.abs(dv) + Math.abs(dy - 5) <= 3 && !(du == 0 && dv == 0 && dy < 5)) f.set(u + du, y + dy, v + dv, b(Blocks.CHERRY_LEAVES).setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true));
            }
        }
        for (int i = 0; i < 10; i++) { // shards of mirror about the dais, and the pearl lights
            double ang = i * Math.PI / 5;
            f.set(cu + (int) Math.round(Math.cos(ang) * 6), y + 2 + random.nextInt(3), cv + (int) Math.round(Math.sin(ang) * 6), b(Blocks.TINTED_GLASS));
            int u = cu + (int) Math.round(Math.cos(ang + 0.3) * 17), v = cv + (int) Math.round(Math.sin(ang + 0.3) * 17);
            f.set(u, y, v, look.trim());
            f.set(u, y + 1, v, look.trim());
            f.set(u, y + 2, v, look.glow());
        }
    }

    /** Dormiel's catacombs: niches of the dead in the long walls, rows of sarcophagi, his great lantern over the dais, soul fires. */
    private static void catacombs(Frame f, int cu, int cv, int y, Look look, Random random) {
        int ru = look.radius() - 1, rv = look.depth() - 1;
        for (int v = cv - rv; v <= cv + rv; v += 2) for (int side : new int[]{-1, 1}) {
            for (int dy = 1; dy <= 9; dy += 3) { // the niches, three high
                f.set(cu + side * (ru + 1), y + dy, v, Blocks.AIR.defaultBlockState());
                f.set(cu + side * (ru + 1), y + dy, v, random.nextBoolean() ? b(Blocks.SKELETON_SKULL) : b(Blocks.BONE_BLOCK));
            }
        }
        for (int u = cu - ru + 3; u <= cu + ru - 3; u += 5) for (int v : new int[]{cv - rv + 2, cv + rv - 2}) {
            if (Math.abs(u - cu) <= 3) continue;
            f.set(u, y, v, look.trim());
            f.set(u + 1, y, v, look.trim());
            f.set(u, y + 1, v, b(Blocks.SMOOTH_SANDSTONE_SLAB));
            f.set(u + 1, y + 1, v, b(Blocks.SMOOTH_SANDSTONE_SLAB));
        }
        for (int u = cu - ru + 4; u <= cu + ru - 4; u += 8) for (int v : new int[]{cv - 5, cv + 5}) {
            for (int dy = 0; dy < 14; dy++) f.set(u, y + dy, v, dy % 4 == 0 ? look.trim() : look.wall());
        }
        for (int du = -3; du <= 3; du++) for (int dv = -3; dv <= 3; dv++) f.set(cu + du, y, cv + dv, Math.max(Math.abs(du), Math.abs(dv)) == 3 ? look.trim() : look.floor());
        for (int dy = 13; dy >= 5; dy--) f.set(cu, y + dy, cv, b(Blocks.CHAIN));
        f.set(cu, y + 4, cv, b(Blocks.SOUL_LANTERN).setValue(LanternBlock.HANGING, true));
        for (int[] c : new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}}) f.set(cu + c[0], y + 1, cv + c[1], b(Blocks.SOUL_CAMPFIRE));
        for (int i = 0; i < 18; i++) f.set(cu - ru + random.nextInt(2 * ru), y + 8 + random.nextInt(5), cv - rv + random.nextInt(2 * rv), b(Blocks.COBWEB));
    }

    /** Morthis's marsh under the sky: mud and black water, reeds and roots, his stepped pyramid in the middle, green lights. */
    private static void marsh(Frame f, int cu, int cv, int y, Look look, Random random) {
        int r = look.radius() - 1;
        for (int du = -r; du <= r; du++) for (int dv = -r; dv <= r; dv++) {
            double d = Math.sqrt(du * du + dv * dv);
            if (d > r + 0.5) continue;
            int u = cu + du, v = cv + dv;
            double n = Precinct.patches(u * 3, v * 3);
            if (d > 8 && n > 0.15) {
                f.set(u, y - 2, v, b(Blocks.MUD));
                f.set(u, y - 1, v, b(Blocks.WATER));
                if (random.nextInt(6) == 0) f.set(u, y, v, b(Blocks.LILY_PAD));
            } else {
                f.set(u, y - 1, v, n < -0.4 ? b(Blocks.MUDDY_MANGROVE_ROOTS) : b(Blocks.MUD));
                if (d > 8 && random.nextInt(9) == 0) f.set(u, y, v, b(Blocks.MANGROVE_ROOTS));
                if (d > 8 && random.nextInt(11) == 0) f.set(u, y, v, b(Blocks.TALL_GRASS));
            }
        }
        for (int tier = 0; tier < 4; tier++) { // the pyramid
            int w = 6 - tier;
            for (int du = -w; du <= w; du++) for (int dv = -w; dv <= w; dv++) f.set(cu + du, y + tier, cv + dv, tier == 3 ? b(Blocks.GOLD_BLOCK) : look.wall());
        }
        for (int i = 0; i < 6; i++) { // dead mangroves and green lights on poles
            double ang = i * Math.PI / 3 + 0.4;
            int u = cu + (int) Math.round(Math.cos(ang) * 14), v = cv + (int) Math.round(Math.sin(ang) * 14);
            for (int dy = 0; dy < 5; dy++) f.set(u, y + dy, v, b(Blocks.MANGROVE_LOG));
            f.set(u + 1, y + 4, v, b(Blocks.MANGROVE_LOG).setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
            int u2 = cu + (int) Math.round(Math.cos(ang + 0.5) * 10), v2 = cv + (int) Math.round(Math.sin(ang + 0.5) * 10);
            f.set(u2, y, v2, look.trim());
            f.set(u2, y + 1, v2, look.trim());
            f.set(u2, y + 2, v2, look.glow());
        }
    }

    /** Goldarc's treasury: colonnades, heaps of gold along the walls, his golden shield on the far wall over the dais. */
    private static void treasury(Frame f, int cu, int cv, int y, Look look, Random random) {
        int ru = look.radius() - 1, rv = look.depth() - 1;
        for (int u = cu - ru; u <= cu + ru; u++) for (int v = cv - rv; v <= cv + rv; v++) f.set(u, y - 1, v, Math.floorMod(u + v, 2) == 0 ? look.floor() : b(Blocks.QUARTZ_BLOCK));
        for (int v = cv - rv + 2; v <= cv + rv - 2; v += 4) for (int side : new int[]{-1, 1}) {
            int u = cu + side * 6;
            for (int dy = 0; dy < 14; dy++) f.set(u, y + dy, v, dy == 0 || dy == 13 ? look.trim() : s(SoFEBlocks.AUREUM_MARBLE_PILLAR));
            gold(f, cu + side * (ru - 2), v, y, random);
        }
        for (int du = -3; du <= 3; du++) for (int dv = -2; dv <= 2; dv++) f.set(cu + du, y, cv + dv, look.trim());
        int wall = cv - rv; // the shield: a disc of gold with a boss of glowstone, on the far wall
        for (int du = -4; du <= 4; du++) for (int dy = 0; dy <= 8; dy++) {
            double d = Math.sqrt(du * du + (dy - 4) * (dy - 4));
            if (d <= 4.3) f.set(cu + du, y + 3 + dy, wall, d < 1.2 ? b(Blocks.GLOWSTONE) : d > 3.4 ? b(Blocks.RAW_GOLD_BLOCK) : b(Blocks.GOLD_BLOCK));
        }
    }

    /** A heap of gold. */
    private static void gold(Frame f, int u, int v, int y, Random random) {
        for (int du = -1; du <= 1; du++) for (int dv = -1; dv <= 1; dv++) {
            int h = 2 - Math.max(Math.abs(du), Math.abs(dv)) + random.nextInt(2);
            for (int dy = 0; dy < h; dy++) f.set(u + du, y + dy, v + dv, random.nextInt(3) == 0 ? b(Blocks.RAW_GOLD_BLOCK) : b(Blocks.GOLD_BLOCK));
        }
    }

    /** Nixara's market under the sky: stalls with awnings over trapdoors, banners, and the scales on her dais. */
    private static void market(Frame f, int cu, int cv, int y, Look look, Random random) {
        int ru = look.radius() - 1, rv = look.depth() - 1;
        Block[] awnings = {Blocks.RED_WOOL, Blocks.BLUE_WOOL, Blocks.YELLOW_WOOL, Blocks.WHITE_WOOL};
        for (int u = cu - ru + 3; u <= cu + ru - 3; u += 7) for (int v = cv - rv + 3; v <= cv + rv - 3; v += 7) {
            if (Math.abs(u - cu) <= 5 && Math.abs(v - cv) <= 5) continue;
            Block awning = awnings[random.nextInt(awnings.length)];
            for (int du = -1; du <= 1; du++) for (int dv = -1; dv <= 1; dv++) {
                f.set(u + du, y + 3, v + dv, b(awning));
                f.set(u + du, y - 1, v + dv, b(Blocks.SPRUCE_TRAPDOOR)); // the false floor of the hollow merchant
            }
            for (int[] c : new int[][]{{-1, -1}, {1, -1}, {-1, 1}, {1, 1}}) for (int dy = 0; dy < 3; dy++) f.set(u + c[0], y + dy, v + c[1], b(Blocks.SPRUCE_FENCE));
            f.set(u, y, v, random.nextBoolean() ? b(Blocks.BARREL) : b(Blocks.COMPOSTER));
        }
        for (int du = -3; du <= 3; du++) for (int dv = -3; dv <= 3; dv++) f.set(cu + du, y, cv + dv, look.trim());
        for (int dy = 1; dy <= 6; dy++) f.set(cu, y + dy, cv, b(Blocks.GOLD_BLOCK)); // the scales
        for (int du = -3; du <= 3; du++) f.set(cu + du, y + 7, cv, b(Blocks.CHAIN).setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        for (int side : new int[]{-3, 3}) {
            f.set(cu + side, y + 6, cv, b(Blocks.CHAIN));
            f.set(cu + side, y + 5, cv, b(Blocks.GOLD_BLOCK));
        }
    }

    /** Avarok's vault: mountains of gold, iron vault doors round the wall, chains from the roof, his raised seat of gold. */
    private static void vaults(Frame f, int cu, int cv, int y, Look look, Random random) {
        int r = look.radius() - 1;
        for (int i = 0; i < 14; i++) {
            double ang = random.nextDouble() * Math.PI * 2, d = 8 + random.nextDouble() * (r - 10);
            int u = cu + (int) Math.round(Math.cos(ang) * d), v = cv + (int) Math.round(Math.sin(ang) * d);
            for (int du = -3; du <= 3; du++) for (int dv = -3; dv <= 3; dv++) {
                int h = 4 - Math.max(Math.abs(du), Math.abs(dv)) - random.nextInt(2);
                for (int dy = 0; dy < h; dy++) f.set(u + du, y + dy, v + dv, random.nextInt(4) == 0 ? b(Blocks.RAW_GOLD_BLOCK) : b(Blocks.GOLD_BLOCK));
            }
        }
        for (int i = 0; i < 8; i++) { // the vault doors in the wall
            double ang = i * Math.PI / 4;
            int u = cu + (int) Math.round(Math.cos(ang) * (r + 1)), v = cv + (int) Math.round(Math.sin(ang) * (r + 1));
            for (int dy = 0; dy < 5; dy++) f.set(u, y + dy, v, dy == 2 ? b(Blocks.GOLD_BLOCK) : b(Blocks.IRON_BLOCK));
        }
        for (int tier = 0; tier < 3; tier++) {
            int w = 4 - tier;
            for (int du = -w; du <= w; du++) for (int dv = -w; dv <= w; dv++) f.set(cu + du, y + tier, cv + dv, tier == 2 ? b(Blocks.GOLD_BLOCK) : look.trim());
        }
        for (int i = 0; i < 16; i++) {
            int u = cu - r + 3 + random.nextInt(2 * r - 6), v = cv - r + 3 + random.nextInt(2 * r - 6);
            for (int dy = 17; dy > 11 + random.nextInt(3); dy--) f.set(u, y + dy, v, b(Blocks.CHAIN));
        }
    }

    /** Envyris's senate: tiers in a ring about her throne of obsidian on its dais, emerald lights, dark statues. */
    private static void senate(Frame f, int cu, int cv, int y, Look look, Random random) {
        tiers(f, cu, cv, y, look, random, 9, b(Blocks.POLISHED_DEEPSLATE), b(Blocks.SCULK), false);
        for (int du = -1; du <= 1; du++) for (int dv = -1; dv <= 1; dv++) f.set(cu + du, y, cv + dv, b(Blocks.CRYING_OBSIDIAN));
        f.set(cu, y + 1, cv, b(Blocks.DEEPSLATE_TILE_STAIRS).setValue(StairBlock.FACING, Direction.NORTH));
        for (int dy = 1; dy <= 5; dy++) f.set(cu, y + dy, cv - 1, dy == 5 ? b(Blocks.EMERALD_BLOCK) : b(Blocks.OBSIDIAN)); // the throne's back
        f.set(cu - 1, y + 1, cv, b(Blocks.OBSIDIAN));
        f.set(cu + 1, y + 1, cv, b(Blocks.OBSIDIAN));
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4 + 0.2;
            int u = cu + (int) Math.round(Math.cos(ang) * 6), v = cv + (int) Math.round(Math.sin(ang) * 6);
            f.set(u, y, v, look.trim());
            f.set(u, y + 1, v, look.glow());
        }
    }

    /** Solrath's temple: a ring of columns with golden capitals, an altar under a pillar of light from an opening in the roof, the sun on the far wall. */
    private static void temple(Frame f, int cu, int cv, int y, Look look, Random random) {
        int r = look.radius() - 1;
        for (int i = 0; i < 12; i++) {
            double ang = i * Math.PI / 6;
            int u = cu + (int) Math.round(Math.cos(ang) * (r - 3)), v = cv + (int) Math.round(Math.sin(ang) * (r - 3));
            for (int dy = 0; dy < 17; dy++) f.set(u, y + dy, v, dy == 16 ? b(Blocks.GOLD_BLOCK) : dy == 0 ? look.trim() : b(Blocks.CUT_SANDSTONE));
        }
        for (int du = -2; du <= 2; du++) for (int dv = -2; dv <= 2; dv++) f.set(cu + du, y, cv + dv, Math.max(Math.abs(du), Math.abs(dv)) == 2 ? b(Blocks.GOLD_BLOCK) : look.trim());
        for (int dy = 1; dy <= 18; dy++) f.set(cu, y + dy, cv, dy <= 2 ? b(Blocks.GLOWSTONE) : b(Blocks.END_ROD)); // the pillar of false light
        for (int du = -1; du <= 1; du++) for (int dv = -1; dv <= 1; dv++) f.set(cu + du, y + 18, cv + dv, Blocks.AIR.defaultBlockState()); // the opening in the roof
        for (int dy = 19; dy < 40; dy++) for (int du = -1; du <= 1; du++) for (int dv = -1; dv <= 1; dv++) {
            if (!f.air(cu + du, y + dy, cv + dv) && !f.get(cu + du, y + dy, cv + dv).is(Blocks.GOLD_BLOCK)) f.set(cu + du, y + dy, cv + dv, Blocks.AIR.defaultBlockState());
        }
        int wall = cv - r - 1; // the sun on the far wall
        for (int du = -4; du <= 4; du++) for (int dy = 0; dy <= 8; dy++) {
            double d = Math.sqrt(du * du + (dy - 4) * (dy - 4));
            if (d <= 4.3) f.set(cu + du, y + 5 + dy, wall, d < 2 ? b(Blocks.GLOWSTONE) : b(Blocks.GOLD_BLOCK));
        }
    }
}
