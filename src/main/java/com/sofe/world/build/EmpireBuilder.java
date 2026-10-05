package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

import java.util.Random;

/**
 * The blockouts of Acts III and IV (docs/Mundo.md, W6): the dungeons and arenas of Parsivan, Khemet and Aureum,
 * and the underground of Nordrath (the Caverns and the Feast Halls). Each is built from its empire's own set, the
 * ruins mixing intact and corrupted blocks, with its door on the side that faces Sulthari (west for Parsivan and
 * Khemet, east for Aureum), where its Sealed Gate stands. A boss waits in each (BossLairs); the rooms they fight
 * in are protected arenas of their own in structure_positions.json, so their builders here do nothing.
 */
final class EmpireBuilder {

    private EmpireBuilder() {
    }

    static boolean blockout(ServerLevel level, StructurePositions.Structure s, String piece) {
        int y = SultharisBuilder.surfaceY(level, s.x(), s.z());
        if (!piece.startsWith("nordrath/") && isPlace(piece)) y = ground(level, s, piece);
        switch (piece) {
            case "parsivan/baths" -> baths(level, s, y);
            case "parsivan/silk_road" -> caravanserai(level, s, y);
            case "parsivan/enchanted_gardens" -> gardens(level, s, y);
            case "khemet/catacombs" -> catacombs(level, s, y);
            case "khemet/stagnant_marsh" -> marsh(level, s, y);
            case "aureum/treasury" -> treasury(level, s, y);
            case "aureum/market" -> market(level, s, y);
            case "aureum/golden_vaults" -> vaults(level, s, y);
            case "aureum/colosseum" -> colosseum(level, s, y);
            case "aureum/shadow_throne" -> shadowThrone(level, s, y);
            case "sulthari/temple" -> temple(level, s, y);
            case "sulthari/celestial_spire" -> {
                spire(level, s);
                return true;
            }
            case "sulthari/inverted_throne" -> {
                invertedThrone(level, s);
                return true;
            }
            case "nordrath/camp", "parsivan/camp", "khemet/camp", "aureum/camp" -> camp(level, s, y, piece.substring(0, piece.indexOf('/')));
            case "nordrath/caverns" -> {
                caverns(level, s);
                return true;
            }
            case "nordrath/feast_halls" -> {
                feastHalls(level, s);
                return true;
            }
            default -> {
                // the arenas inside the places above are zones only
                return piece.endsWith("_pool") || piece.endsWith("_court") || piece.endsWith("_heart") || piece.endsWith("_crypt")
                        || piece.endsWith("_pyramid") || piece.endsWith("_hall") || piece.endsWith("_forum") || piece.endsWith("_floor")
                        || piece.endsWith("_room");
            }
        }
        StructureBuilder.placeGates(level, s, y);
        return true;
    }

    private static boolean isPlace(String piece) {
        return switch (piece) {
            case "sulthari/temple",
                 "parsivan/baths", "parsivan/silk_road", "parsivan/enchanted_gardens", "khemet/catacombs", "khemet/stagnant_marsh",
                 "aureum/treasury", "aureum/market", "aureum/golden_vaults", "aureum/colosseum", "aureum/shadow_throne",
                 "parsivan/camp", "khemet/camp", "aureum/camp" -> true;
            default -> false;
        };
    }

    /**
     * A liberated camp (docs/Anexos.md, A4): a palisade with four openings, three tents in the empire's colours round
     * a fire, the stalls of the camp's alchemist and smith (who stand there), lanterns, and a Personal Vault by the
     * Waystone. Its merchants trade once the region's Archsin has fallen.
     */
    private static void camp(ServerLevel level, StructurePositions.Structure s, int y, String region) {
        int cx = s.x(), cz = s.z(), r = 13;
        BlockState[] cloth = switch (region) {
            case "parsivan" -> new BlockState[]{Blocks.CYAN_WOOL.defaultBlockState(), Blocks.WHITE_WOOL.defaultBlockState()};
            case "khemet" -> new BlockState[]{Blocks.YELLOW_WOOL.defaultBlockState(), Blocks.BLUE_WOOL.defaultBlockState()};
            case "aureum" -> new BlockState[]{Blocks.RED_WOOL.defaultBlockState(), Blocks.WHITE_WOOL.defaultBlockState()};
            default -> new BlockState[]{Blocks.BROWN_WOOL.defaultBlockState(), Blocks.RED_WOOL.defaultBlockState()};
        };
        BlockState post = Blocks.SPRUCE_LOG.defaultBlockState(), path = Blocks.DIRT_PATH.defaultBlockState();
        for (int dx = -r - 1; dx <= r + 1; dx++) {
            for (int dz = -r - 1; dz <= r + 1; dz++) {
                for (int dy = 0; dy <= 6; dy++) set(level, cx + dx, y + dy, cz + dz, Blocks.AIR.defaultBlockState());
                boolean edge = Math.abs(dx) == r || Math.abs(dz) == r;
                boolean opening = Math.abs(dx) <= 1 || Math.abs(dz) <= 1;
                if (edge && !opening) {
                    for (int dy = 0; dy < 3; dy++) set(level, cx + dx, y + dy, cz + dz, post);
                    if ((dx + dz) % 4 == 0) set(level, cx + dx, y + 3, cz + dz, Blocks.LANTERN.defaultBlockState());
                }
                if (Math.abs(dx) <= 1 || Math.abs(dz) <= 1) set(level, cx + dx, y - 1, cz + dz, path);
            }
        }
        // the fire, ringed with stones
        set(level, cx, y, cz, Blocks.CAMPFIRE.defaultBlockState());
        for (int[] o : new int[][]{{-2, 0}, {2, 0}, {0, -2}, {0, 2}, {-1, -1}, {1, 1}, {-1, 1}, {1, -1}}) {
            set(level, cx + o[0], y - 1, cz + o[1], Blocks.COBBLESTONE.defaultBlockState());
        }
        // three tents: ridged prisms of wool, open toward the fire
        for (int[] t : new int[][]{{-8, 6}, {8, 6}, {0, 9}}) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int i = 0; i <= 2; i++) {
                    for (int dx = -2 + i; dx <= 2 - i; dx++) {
                        boolean door = dz == -2 && Math.abs(dx) <= 0 && i < 2;
                        boolean inside = Math.abs(dx) < 2 - i && dz > -2 && dz < 2 && i < 2;
                        if (!door && !inside) set(level, cx + t[0] + dx, y + i, cz + t[1] + dz, cloth[(dx + i) & 1]);
                    }
                }
            }
            set(level, cx + t[0], y, cz + t[1] + 1, Blocks.WHITE_BED.defaultBlockState());
        }
        // the stalls of the camp's merchants (they stand at x -5 and +5, z -4)
        set(level, cx - 6, y, cz - 6, Blocks.BREWING_STAND.defaultBlockState());
        set(level, cx - 4, y, cz - 6, Blocks.BARREL.defaultBlockState());
        set(level, cx - 5, y, cz - 6, Blocks.BARREL.defaultBlockState());
        set(level, cx + 4, y, cz - 6, Blocks.ANVIL.defaultBlockState());
        set(level, cx + 6, y, cz - 6, Blocks.SMITHING_TABLE.defaultBlockState());
        set(level, cx + 5, y, cz - 6, Blocks.BLAST_FURNACE.defaultBlockState());
        for (int dx : new int[]{-7, -3, 3, 7}) {
            set(level, cx + dx, y, cz - 7, Blocks.SPRUCE_FENCE.defaultBlockState());
            set(level, cx + dx, y + 1, cz - 7, Blocks.SPRUCE_FENCE.defaultBlockState());
            set(level, cx + dx, y + 2, cz - 7, cloth[0]);
        }
        for (int dx = -7; dx <= 7; dx++) if (Math.abs(dx) >= 3) set(level, cx + dx, y + 2, cz - 7, cloth[(dx & 1)]);
        set(level, cx + 3, y, cz + 6, SoFEBlocks.PERSONAL_VAULT.get().defaultBlockState());                    // beside the Waystone (z + 6)
        // the refugees who wait here (scripts/story_catalog_act34.py): they give the region's quests
        int[][] spots = {{-6, 2}, {6, 2}, {0, -9}};
        java.util.List<String> folk = CAMP_FOLK.getOrDefault(region, java.util.List.of());
        for (int i = 0; i < folk.size(); i++) {
            com.sofe.world.StoryPlacements.spawnNpc(level, new StructurePositions.Npc(folk.get(i), "citizen", cx + spots[i][0], cz + spots[i][1],
                    i * 120, null));
        }
    }

    /** The people of each empire who fled to its camp. */
    static final java.util.Map<String, java.util.List<String>> CAMP_FOLK = java.util.Map.of(
            "parsivan", java.util.List.of("parsivan_poet", "parsivan_gardener", "parsivan_dancer"),
            "khemet", java.util.List.of("khemet_embalmer", "khemet_ferryman", "khemet_scribe"),
            "aureum", java.util.List.of("aureum_senator", "aureum_gladiator", "aureum_widow"));

    /** How far round a place the land is shaped, sloping down from its floor to the land (or sea bed) around. */
    static final int SHORE = 14;

    /**
     * The land under a place. The terrain is procedural, so a fixed place may fall on a lake, the sea or a slope:
     * the footprint is made solid up to one floor height (the middle of the land found under it, never below the
     * sea) and a shore of its empire's ground slopes from it down to the land around, so it stands on an island or
     * a terrace instead of on water. Returns the floor height.
     */
    static int ground(ServerLevel level, StructurePositions.Structure s, String piece) {
        int x0 = minX(s), x1 = maxX(s), z0 = minZ(s), z1 = maxZ(s);
        level.getChunk(s.x() >> 4, s.z() >> 4);
        int[] heights = new int[9];
        int k = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) heights[k++] = land(level, x0 + (x1 - x0) * i / 2, z0 + (z1 - z0) * j / 2);
        }
        java.util.Arrays.sort(heights);
        int y = Math.max(heights[4], level.getSeaLevel() + 2);
        boolean desert = piece.startsWith("khemet/") || piece.startsWith("sulthari/");
        BlockState top = desert ? Blocks.SAND.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState fill = desert ? Blocks.SANDSTONE.defaultBlockState() : Blocks.DIRT.defaultBlockState();
        for (int x = x0 - SHORE; x <= x1 + SHORE; x++) {
            for (int z = z0 - SHORE; z <= z1 + SHORE; z++) {
                int out = Math.max(Math.max(x0 - x, x - x1), Math.max(z0 - z, z - z1)); // blocks outside the footprint
                int here = land(level, x, z);
                int want = out <= 0 ? y : y - (int) Math.round((double) out * (y - Math.min(here, y)) / SHORE);
                if (out > 0 && want <= here) continue; // the land round about is already as high
                for (int yy = here; yy < want; yy++) set(level, x, yy, z, yy == want - 1 ? top : fill);
                if (out <= 0) for (int yy = y; yy < y + 3; yy++) {
                    if (!level.getFluidState(new BlockPos(x, yy, z)).isEmpty()) set(level, x, yy, z, Blocks.AIR.defaultBlockState());
                }
            }
        }
        causeway(level, s, piece, y);
        return y;
    }

    /** How far a causeway runs out from a door over water, looking for the shore. */
    static final int CAUSEWAY = 64;

    /**
     * A paved causeway 5 wide from the door out over any water, on arches of the empire's stone, until it meets
     * dry land: so a place on a lake or the sea can still be walked into.
     */
    private static void causeway(ServerLevel level, StructurePositions.Structure s, String piece, int y) {
        int dir = piece.startsWith("aureum/") ? 1 : -1; // Aureum's doors face east, the others west
        int start = dir > 0 ? maxX(s) + 1 : minX(s) - 1;
        BlockState paving = piece.startsWith("aureum/") ? b(SoFEBlocks.AUREUM_POLISHED_MARBLE)
                : piece.startsWith("khemet/") ? b(SoFEBlocks.KHEMET_CARVED_SANDSTONE) : b(SoFEBlocks.PARSIVAN_WHITE_PLASTER);
        BlockState rail = piece.startsWith("aureum/") ? b(SoFEBlocks.AUREUM_MARBLE_BRICK_WALL)
                : piece.startsWith("khemet/") ? b(SoFEBlocks.KHEMET_SANDSTONE_WALL) : b(SoFEBlocks.PARSIVAN_WHITE_PLASTER_WALL);
        int dry = 0;
        for (int i = 0; i < CAUSEWAY && dry < 3; i++) {
            int x = start + dir * i;
            boolean wet = false;
            for (int dz = -2; dz <= 2; dz++) {
                int z = s.z() + dz;
                level.getChunk(x >> 4, z >> 4);
                int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, x, z);
                boolean water = !level.getFluidState(new BlockPos(x, top - 1, z)).isEmpty();
                wet |= water;
                if (!water && land(level, x, z) >= y) continue;
                set(level, x, y - 1, z, paving);
                if (Math.abs(dz) == 2) set(level, x, y, z, rail);
                else for (int h = 0; h < 3; h++) set(level, x, y + h, z, Blocks.AIR.defaultBlockState());
                if (Math.floorMod(i, 6) == 0 && Math.abs(dz) == 2) { // a pier down to the bed
                    for (int yy = land(level, x, z); yy < y - 1; yy++) set(level, x, yy, z, paving);
                }
            }
            dry = wet ? 0 : dry + 1;
        }
    }

    /** The first block above the solid ground of a column (under water, the sea bed). */
    private static int land(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        return level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR, x, z);
    }

    // ------------------------------------------------------------------------------------------------ palettes

    private static BlockState b(net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.Block> block) {
        return block.get().defaultBlockState();
    }

    /** Intact or corrupted, by a fixed hash of the position: the same ruin is built the same way every time. */
    private static BlockState ruin(int x, int y, int z, BlockState intact, BlockState corrupted, double corruption) {
        long h = (x * 73856093L) ^ (y * 19349663L) ^ (z * 83492791L);
        return Math.floorMod(h, 1000) < corruption * 1000 ? corrupted : intact;
    }

    private static final double PARSIVAN_RUIN = 0.35, KHEMET_RUIN = 0.4, AUREUM_RUIN = 0.3;

    private static BlockState plaster(int x, int y, int z) {
        return ruin(x, y, z, b(SoFEBlocks.PARSIVAN_WHITE_PLASTER), b(SoFEBlocks.CORRUPTED_PARSIVAN_WHITE_PLASTER), PARSIVAN_RUIN);
    }

    private static BlockState tiles(int x, int y, int z) {
        return ruin(x, y, z, b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES), b(SoFEBlocks.CORRUPTED_PARSIVAN_TURQUOISE_TILES), PARSIVAN_RUIN);
    }

    private static BlockState sandstone(int x, int y, int z) {
        return ruin(x, y, z, b(SoFEBlocks.KHEMET_CARVED_SANDSTONE), b(SoFEBlocks.CORRUPTED_KHEMET_CARVED_SANDSTONE), KHEMET_RUIN);
    }

    private static BlockState limestone(int x, int y, int z) {
        return ruin(x, y, z, b(SoFEBlocks.KHEMET_PAINTED_LIMESTONE), b(SoFEBlocks.CORRUPTED_KHEMET_PAINTED_LIMESTONE), KHEMET_RUIN);
    }

    private static BlockState marble(int x, int y, int z, double corruption) {
        return ruin(x, y, z, b(SoFEBlocks.AUREUM_MARBLE_BRICKS), b(SoFEBlocks.CORRUPTED_AUREUM_MARBLE_BRICKS), corruption);
    }

    private static BlockState pillar(int x, int y, int z, double corruption) {
        return ruin(x, y, z, b(SoFEBlocks.AUREUM_MARBLE_PILLAR), b(SoFEBlocks.CORRUPTED_AUREUM_MARBLE_PILLAR), corruption);
    }

    private interface Wall {
        BlockState at(int x, int y, int z);
    }

    // ------------------------------------------------------------------------------------------------ shared pieces

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        SultharisBuilder.set(level, x, y, z, state);
    }

    private static int minX(StructurePositions.Structure s) {
        return s.x() - SultharisBuilder.half(s.sizeX());
    }

    private static int maxX(StructurePositions.Structure s) {
        return s.x() + SultharisBuilder.half(s.sizeX()) - 1;
    }

    private static int minZ(StructurePositions.Structure s) {
        return s.z() - SultharisBuilder.half(s.sizeZ());
    }

    private static int maxZ(StructurePositions.Structure s) {
        return s.z() + SultharisBuilder.half(s.sizeZ()) - 1;
    }

    /**
     * Four walls round a footprint with a door 3 wide and 4 high in the middle of the side that faces
     * Sulthari (west or east); pilasters every 6 blocks, a band of trim at two heights.
     */
    private static void walls(ServerLevel level, int minX, int minZ, int maxX, int maxZ, int y, int height, Wall wall, Wall pilaster,
                              BlockState band, boolean doorWest) {
        int cz = (minZ + maxZ) / 2;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean edge = x == minX || x == maxX || z == minZ || z == maxZ;
                if (!edge) continue;
                boolean door = (doorWest ? x == minX : x == maxX) && Math.abs(z - cz) <= 1;
                boolean pil = Math.floorMod(x - minX, 6) == 0 && (z == minZ || z == maxZ) || Math.floorMod(z - minZ, 6) == 0 && (x == minX || x == maxX);
                for (int dy = 0; dy < height; dy++) {
                    if (door && dy < 4) continue;
                    BlockState state = pil ? pilaster.at(x, y + dy, z) : (dy == 1 || dy == height - 2) && band != null ? band : wall.at(x, y + dy, z);
                    set(level, x, y + dy, z, state);
                }
            }
        }
    }

    private static void roof(ServerLevel level, int minX, int minZ, int maxX, int maxZ, int y, BlockState slab, BlockState light) {
        for (int x = minX + 1; x < maxX; x++) {
            for (int z = minZ + 1; z < maxZ; z++) {
                set(level, x, y, z, slab);
                if (light != null && Math.floorMod(x - minX, 6) == 3 && Math.floorMod(z - minZ, 6) == 3) set(level, x, y - 1, z, light);
            }
        }
    }

    private static void column(ServerLevel level, int x, int y, int z, int height, BlockState shaft, BlockState capital) {
        for (int dy = 0; dy < height; dy++) set(level, x, y + dy, z, dy == height - 1 && capital != null ? capital : shaft);
    }

    /** A broken column: its shaft stops short and a drum of it lies beside. */
    private static void brokenColumn(ServerLevel level, int x, int y, int z, int height, BlockState shaft, Random random) {
        int stands = 2 + random.nextInt(Math.max(1, height - 2));
        for (int dy = 0; dy < stands; dy++) set(level, x, y + dy, z, shaft);
        if (random.nextBoolean()) {
            BlockState fallen = shaft.hasProperty(RotatedPillarBlock.AXIS) ? shaft.setValue(RotatedPillarBlock.AXIS, Direction.Axis.X) : shaft;
            set(level, x + 1, y, z + 1, fallen);
            set(level, x + 2, y, z + 1, fallen);
        }
    }

    private static void spawner(ServerLevel level, int x, int y, int z, EntityType<?> type) {
        BlockPos pos = new BlockPos(x, y, z);
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) spawner.setEntityId(type, level.getRandom());
    }

    private static void pool(ServerLevel level, int cx, int cz, int halfX, int halfZ, int y, BlockState rim, BlockState bottom, BlockState water) {
        for (int x = cx - halfX - 1; x <= cx + halfX + 1; x++) {
            for (int z = cz - halfZ - 1; z <= cz + halfZ + 1; z++) {
                boolean edge = x == cx - halfX - 1 || x == cx + halfX + 1 || z == cz - halfZ - 1 || z == cz + halfZ + 1;
                if (edge) {
                    set(level, x, y - 1, z, rim);
                } else {
                    set(level, x, y - 3, z, bottom);
                    set(level, x, y - 2, z, water);
                    set(level, x, y - 1, z, water);
                }
            }
        }
    }

    private static void fountain(ServerLevel level, int cx, int y, int cz, BlockState stone, BlockState top) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                boolean rim = Math.abs(dx) == 2 || Math.abs(dz) == 2;
                set(level, cx + dx, y, cz + dz, rim ? stone : Blocks.WATER.defaultBlockState());
            }
        }
        column(level, cx, y, cz, 3, stone, top);
        set(level, cx, y + 3, cz, Blocks.WATER.defaultBlockState());
    }

    private static void floorPattern(ServerLevel level, int minX, int minZ, int maxX, int maxZ, int y, BlockState a, BlockState b, int every) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                set(level, x, y - 1, z, Math.floorMod(x + z, every) == 0 ? b : a);
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ Parsivan

    /** The Parsivan Baths (Mirael): a domed bathhouse of plaster and tile round a great pool, steam vents in the corners. */
    private static void baths(ServerLevel level, StructurePositions.Structure s, int y) {
        int x0 = minX(s), x1 = maxX(s), z0 = minZ(s), z1 = maxZ(s);
        SultharisBuilder.pad(level, x0, z0, x1, z1, y, b(SoFEBlocks.PARSIVAN_LAPIS_MOSAIC));
        floorPattern(level, x0 + 1, z0 + 1, x1 - 1, z1 - 1, y, b(SoFEBlocks.PARSIVAN_WHITE_PLASTER), b(SoFEBlocks.PARSIVAN_LAPIS_MOSAIC), 4);
        walls(level, x0, z0, x1, z1, y, 11, EmpireBuilder::plaster, EmpireBuilder::tiles, b(SoFEBlocks.PARSIVAN_LAPIS_MOSAIC), true);
        roof(level, x0, z0, x1, z1, y + 11, b(SoFEBlocks.PARSIVAN_WHITE_PLASTER_SLAB), Blocks.LANTERN.defaultBlockState());
        Architecture.openRoof(level, s.x(), y + 11, s.z(), 9);
        Architecture.dome(level, s.x(), y + 11, s.z(), 9, b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES), b(SoFEBlocks.PARSIVAN_WHITE_PLASTER),
                Blocks.LIGHTNING_ROD.defaultBlockState());
        pool(level, s.x(), s.z(), 7, 7, y, b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES), b(SoFEBlocks.PARSIVAN_LAPIS_MOSAIC), Blocks.WATER.defaultBlockState());
        for (int i = -1; i <= 1; i += 2) {
            for (int j = -1; j <= 1; j += 2) {
                column(level, s.x() + i * 10, y, s.z() + j * 10, 10, b(SoFEBlocks.PARSIVAN_WHITE_PLASTER), b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES));
                set(level, x0 + 3 + (i + 1) / 2 * (x1 - x0 - 6), y, z0 + 3 + (j + 1) / 2 * (z1 - z0 - 6), Blocks.CAMPFIRE.defaultBlockState()); // steam
            }
        }
        spawner(level, x0 + 6, y, s.z() - 8, EntityType.DROWNED);
        spawner(level, x0 + 6, y, s.z() + 8, EntityType.WITCH);
    }

    /** The Silk Road (Thessyn): a caravanserai, a square court ringed by arcades and stalls, webs in the corners. */
    private static void caravanserai(ServerLevel level, StructurePositions.Structure s, int y) {
        int x0 = minX(s), x1 = maxX(s), z0 = minZ(s), z1 = maxZ(s);
        Random random = new Random(s.x() * 31L + s.z());
        SultharisBuilder.pad(level, x0, z0, x1, z1, y, Blocks.PACKED_MUD.defaultBlockState());
        walls(level, x0, z0, x1, z1, y, 9, EmpireBuilder::plaster, EmpireBuilder::tiles, b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES), true);
        int in = 7; // depth of the arcades round the court
        for (int x = x0 + 1; x < x1; x++) {
            for (int z = z0 + 1; z < z1; z++) {
                boolean court = x > x0 + in && x < x1 - in && z > z0 + in && z < z1 - in;
                if (!court) set(level, x, y + 6, z, b(SoFEBlocks.PARSIVAN_WHITE_PLASTER_SLAB));
                boolean arcade = (x == x0 + in || x == x1 - in) && z >= z0 + in && z <= z1 - in
                        || (z == z0 + in || z == z1 - in) && x >= x0 + in && x <= x1 - in;
                if (arcade && Math.floorMod(x + z, 4) == 0) column(level, x, y, z, 6, b(SoFEBlocks.PARSIVAN_WHITE_PLASTER), b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES));
            }
        }
        BlockState silkA = Blocks.PURPLE_WOOL.defaultBlockState(), silkB = Blocks.CYAN_WOOL.defaultBlockState();
        for (int z = z0 + 3; z < z1 - 4; z += 7) {
            Architecture.stall(level, x1 - 6, y, z, silkA, silkB, random);
        }
        for (int[] c : new int[][]{{x0 + 1, z0 + 1}, {x1 - 1, z0 + 1}, {x0 + 1, z1 - 1}, {x1 - 1, z1 - 1}}) {
            for (int dy = 0; dy < 5; dy++) {
                for (int d = 0; d < 3 - dy / 2; d++) {
                    set(level, c[0] + (c[0] < s.x() ? d : -d), y + dy, c[1], Blocks.COBWEB.defaultBlockState());
                    set(level, c[0], y + dy, c[1] + (c[1] < s.z() ? d : -d), Blocks.COBWEB.defaultBlockState());
                }
            }
        }
        for (int i = 0; i < 6; i++) { // bales and chests of the caravans; some are not what they seem
            int x = x0 + 2 + random.nextInt(in - 2), z = z0 + 2 + random.nextInt(z1 - z0 - 4);
            set(level, x, y, z, i % 2 == 0 ? Blocks.CHEST.defaultBlockState() : Blocks.HAY_BLOCK.defaultBlockState());
        }
        fountain(level, s.x(), y, s.z(), b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES), b(SoFEBlocks.PARSIVAN_LAPIS_MOSAIC));
        spawner(level, x0 + 3, y, z0 + 4, EntityType.CAVE_SPIDER);
        spawner(level, x0 + 3, y, z1 - 4, EntityType.SPIDER);
    }

    /**
     * The Enchanted Gardens (Luxara): a walled paradise garden in four quarters, water channels meeting at a
     * fountain in a ring of columns, hedges, flowers and blossoming trees, overgrown where the illusion frays.
     */
    private static void gardens(ServerLevel level, StructurePositions.Structure s, int y) {
        int x0 = minX(s), x1 = maxX(s), z0 = minZ(s), z1 = maxZ(s);
        Random random = new Random(s.x() * 17L + s.z());
        SultharisBuilder.pad(level, x0, z0, x1, z1, y, Blocks.GRASS_BLOCK.defaultBlockState());
        walls(level, x0, z0, x1, z1, y, 7, EmpireBuilder::plaster, EmpireBuilder::tiles, b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES), true);
        BlockState path = b(SoFEBlocks.PARSIVAN_WHITE_PLASTER), water = Blocks.WATER.defaultBlockState();
        for (int i = x0 + 1; i < x1; i++) { // the cross of channels with walks beside them
            for (int w = -3; w <= 3; w++) {
                set(level, i, y - 1, s.z() + w, Math.abs(w) <= 1 ? water : path);
            }
        }
        for (int i = z0 + 1; i < z1; i++) {
            for (int w = -3; w <= 3; w++) {
                set(level, s.x() + w, y - 1, i, Math.abs(w) <= 1 ? water : path);
            }
        }
        BlockState hedge = Blocks.AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        BlockState blossom = Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        BlockState[] flowers = {Blocks.ALLIUM.defaultBlockState(), Blocks.PINK_TULIP.defaultBlockState(), Blocks.LILY_OF_THE_VALLEY.defaultBlockState(),
                Blocks.CORNFLOWER.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.AZURE_BLUET.defaultBlockState()};
        for (int x = x0 + 2; x < x1 - 1; x++) {
            for (int z = z0 + 2; z < z1 - 1; z++) {
                if (Math.abs(x - s.x()) <= 4 || Math.abs(z - s.z()) <= 4) continue;
                boolean border = Math.floorMod(x - x0, 12) == 0 || Math.floorMod(z - z0, 12) == 0;
                if (border) {
                    set(level, x, y, z, random.nextInt(9) == 0 ? blossom : hedge);
                } else if (random.nextInt(5) == 0) {
                    set(level, x, y, z, flowers[random.nextInt(flowers.length)]);
                } else if (random.nextInt(140) == 0) {
                    blossomTree(level, x, y, z, random);
                }
                if (random.nextInt(14) == 0) set(level, x, y - 1, z, Blocks.MOSS_BLOCK.defaultBlockState()); // the garden runs wild
            }
        }
        // the heart: a ring of columns round the fountain, where Luxara waits
        int r = 12;
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            column(level, s.x() + (int) Math.round(Math.cos(a) * r), y, s.z() + (int) Math.round(Math.sin(a) * r), 7,
                    b(SoFEBlocks.PARSIVAN_WHITE_PLASTER), b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES));
        }
        for (int dx = -r + 2; dx <= r - 2; dx++) {
            for (int dz = -r + 2; dz <= r - 2; dz++) {
                if (dx * dx + dz * dz <= (r - 2) * (r - 2)) set(level, s.x() + dx, y - 1, s.z() + dz,
                        Math.floorMod(dx + dz, 3) == 0 ? b(SoFEBlocks.PARSIVAN_LAPIS_MOSAIC) : b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES));
            }
        }
        fountain(level, s.x(), y, s.z(), b(SoFEBlocks.PARSIVAN_WHITE_PLASTER), b(SoFEBlocks.PARSIVAN_LAPIS_MOSAIC));
        spawner(level, x0 + 8, y, z0 + 8, EntityType.WITCH);
        spawner(level, x0 + 8, y, z1 - 8, EntityType.ZOMBIE);
    }

    private static void blossomTree(ServerLevel level, int x, int y, int z, Random random) {
        int trunk = 4 + random.nextInt(2);
        for (int dy = 0; dy < trunk; dy++) set(level, x, y + dy, z, Blocks.CHERRY_LOG.defaultBlockState());
        BlockState leaves = Blocks.CHERRY_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = 0; dy <= 1; dy++) {
                    if (Math.abs(dx) + Math.abs(dz) + dy <= 3) set(level, x + dx, y + trunk - 1 + dy, z + dz, leaves);
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------ Khemet

    /**
     * The Khemet Catacombs (Dormiel): a temple between two obelisks at the surface, its stair going down into a
     * crypt of painted limestone with sarcophagi and canopic urns along the walls.
     */
    private static void catacombs(ServerLevel level, StructurePositions.Structure s, int y) {
        int x0 = minX(s), x1 = maxX(s), z0 = minZ(s), z1 = maxZ(s);
        int tx0 = x0, tx1 = x0 + 15, tz0 = s.z() - 7, tz1 = s.z() + 7; // the temple, on the west side of the footprint
        SultharisBuilder.pad(level, tx0 - 4, tz0 - 4, tx1, tz1 + 4, y, b(SoFEBlocks.KHEMET_CARVED_SANDSTONE));
        walls(level, tx0, tz0, tx1, tz1, y, 8, EmpireBuilder::sandstone, (x, yy, z) -> b(SoFEBlocks.KHEMET_GOLD_HIEROGLYPHS),
                b(SoFEBlocks.KHEMET_PAINTED_LIMESTONE), true);
        roof(level, tx0, tz0, tx1, tz1, y + 8, b(SoFEBlocks.KHEMET_SANDSTONE_SLAB), null);
        for (int dz : new int[]{-4, 4}) { // the obelisks before the door
            column(level, tx0 - 3, y, s.z() + dz, 9, b(SoFEBlocks.KHEMET_OBELISK), Blocks.GOLD_BLOCK.defaultBlockState());
        }
        // the stair down, eastwards, 16 steps, into the crypt
        int depth = 16, cy = y - depth;
        for (int i = 0; i < depth; i++) {
            int x = tx0 + 2 + i;
            for (int dz = -1; dz <= 1; dz++) {
                set(level, x, y - 1 - i, s.z() + dz, b(SoFEBlocks.KHEMET_SANDSTONE_STAIRS).setValue(StairBlock.FACING, Direction.EAST));
                for (int h = 0; h < 4; h++) set(level, x, y - i + h, s.z() + dz, Blocks.AIR.defaultBlockState());
            }
            for (int h = -1; h < 5; h++) {
                set(level, x, y - i + h - 1, s.z() - 2, limestone(x, y + h, s.z() - 2));
                set(level, x, y - i + h - 1, s.z() + 2, limestone(x, y + h, s.z() + 2));
            }
        }
        // the crypt: a hall under the eastern part of the footprint
        int cx0 = tx0 + 2 + depth, cx1 = x1, cz0 = z0 + 4, cz1 = z1 - 4;
        for (int x = cx0; x <= cx1; x++) {
            for (int z = cz0; z <= cz1; z++) {
                boolean edge = x == cx0 || x == cx1 || z == cz0 || z == cz1;
                set(level, x, cy - 1, z, Math.floorMod(x + z, 5) == 0 ? b(SoFEBlocks.KHEMET_GOLD_HIEROGLYPHS) : sandstone(x, cy - 1, z));
                for (int h = 0; h < 8; h++) {
                    boolean door = x == cx0 && Math.abs(z - s.z()) <= 1 && h < 4;
                    set(level, x, cy + h, z, edge && !door ? limestone(x, cy + h, z) : Blocks.AIR.defaultBlockState());
                }
                set(level, x, cy + 8, z, sandstone(x, cy + 8, z));
            }
        }
        for (int x = cx0 + 3; x < cx1 - 2; x += 4) { // sarcophagi and urns along both walls, soul fire between
            for (int z : new int[]{cz0 + 2, cz1 - 2}) {
                set(level, x, cy, z, Blocks.SMOOTH_SANDSTONE.defaultBlockState());
                set(level, x + 1, cy, z, Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState());
                set(level, x + 2, cy, z + (z == cz0 + 2 ? -1 : 1), Blocks.DECORATED_POT.defaultBlockState());
                set(level, x, cy + 4, z + (z == cz0 + 2 ? -1 : 1), Blocks.SOUL_LANTERN.defaultBlockState());
            }
        }
        for (int x = cx0 + 6; x < cx1 - 3; x += 8) {
            column(level, x, cy, s.z() - 6, 8, b(SoFEBlocks.KHEMET_OBELISK), null);
            column(level, x, cy, s.z() + 6, 8, b(SoFEBlocks.KHEMET_OBELISK), null);
        }
        spawner(level, cx0 + 4, cy, s.z(), EntityType.HUSK);
        spawner(level, (cx0 + cx1) / 2, cy, cz0 + 1, EntityType.SKELETON);
        StructureBuilder.placeGates(level, s, y);
    }

    /**
     * The Stagnant Marsh (Morthis): reeds, mud and still water round a sunken stepped pyramid; its broad lowest
     * terrace, facing west, is where the slothful king waits.
     */
    private static void marsh(ServerLevel level, StructurePositions.Structure s, int y) {
        int x0 = minX(s), x1 = maxX(s), z0 = minZ(s), z1 = maxZ(s);
        Random random = new Random(s.x() * 13L + s.z());
        SultharisBuilder.pad(level, x0, z0, x1, z1, y, Blocks.MUD.defaultBlockState(), 20);
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                double n = Math.sin(x * 0.21) + Math.cos(z * 0.17) + Math.sin((x + z) * 0.09);
                if (n > 1.3) set(level, x, y - 1, z, Blocks.WATER.defaultBlockState());
                else if (random.nextInt(9) == 0) set(level, x, y, z, random.nextBoolean() ? Blocks.GRASS.defaultBlockState() : Blocks.FERN.defaultBlockState());
                if (n > 1.3 && random.nextInt(6) == 0) set(level, x, y, z, Blocks.LILY_PAD.defaultBlockState());
            }
        }
        // a low wall of sunken sandstone, broken here and there, door to the west
        walls(level, x0, z0, x1, z1, y, 4, EmpireBuilder::sandstone, EmpireBuilder::sandstone, null, true);
        // the pyramid, to the east of the middle
        int px = s.x() + 16, steps = 6, base = 13;
        for (int i = 0; i < steps; i++) {
            int r = base - i * 2;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    for (int h = 0; h < 2; h++) set(level, px + dx, y + i * 2 + h, s.z() + dz, sandstone(px + dx, y + i * 2 + h, s.z() + dz));
                }
            }
        }
        set(level, px, y + steps * 2, s.z(), Blocks.GOLD_BLOCK.defaultBlockState());
        // the terrace before it: flat paving where the fight is
        for (int dx = -14; dx <= 6; dx++) {
            for (int dz = -14; dz <= 14; dz++) {
                if (dx * dx / 2 + dz * dz <= 200) set(level, s.x() + dx - 6, y - 1, s.z() + dz, sandstone(s.x() + dx, y - 1, s.z() + dz));
            }
        }
        for (int i = 0; i < 6; i++) {
            column(level, s.x() - 14 + i * 4, y, s.z() - 12, 5 - (i % 3), b(SoFEBlocks.KHEMET_OBELISK), null);
            column(level, s.x() - 14 + i * 4, y, s.z() + 12, 3 + (i % 3), b(SoFEBlocks.KHEMET_OBELISK), null);
        }
        spawner(level, x0 + 8, y, z0 + 10, EntityType.DROWNED);
        spawner(level, x0 + 8, y, z1 - 10, EntityType.SLIME);
    }

    // ------------------------------------------------------------------------------------------------ Aureum

    /** The Treasury (Goldarc): a marble hall of columns, a gold mosaic floor and heaps of coin in the aisles. */
    private static void treasury(ServerLevel level, StructurePositions.Structure s, int y) {
        int x0 = minX(s), x1 = maxX(s), z0 = minZ(s), z1 = maxZ(s);
        SultharisBuilder.pad(level, x0, z0, x1, z1, y, b(SoFEBlocks.AUREUM_POLISHED_MARBLE));
        floorPattern(level, x0 + 4, z0 + 4, x1 - 4, z1 - 4, y, b(SoFEBlocks.AUREUM_POLISHED_MARBLE), b(SoFEBlocks.AUREUM_GOLD_MOSAIC), 3);
        walls(level, x0, z0, x1, z1, y, 12, (x, yy, z) -> marble(x, yy, z, AUREUM_RUIN), (x, yy, z) -> pillar(x, yy, z, AUREUM_RUIN),
                b(SoFEBlocks.AUREUM_GOLD_MOSAIC), false);
        roof(level, x0, z0, x1, z1, y + 12, b(SoFEBlocks.AUREUM_MARBLE_BRICK_SLAB), Blocks.LANTERN.defaultBlockState());
        for (int x = x0 + 5; x < x1 - 3; x += 6) {
            for (int z : new int[]{z0 + 6, z1 - 6}) {
                column(level, x, y, z, 12, pillar(x, y, z, 0.2), b(SoFEBlocks.AUREUM_GOLD_MOSAIC));
                set(level, x, y, z + (z < s.z() ? -2 : 2), Blocks.GOLD_BLOCK.defaultBlockState()); // coin heaps
                set(level, x + 1, y, z + (z < s.z() ? -2 : 2), Blocks.RAW_GOLD_BLOCK.defaultBlockState());
                set(level, x, y + 1, z + (z < s.z() ? -2 : 2), Blocks.GOLD_BLOCK.defaultBlockState());
            }
        }
        for (int dz = -2; dz <= 2; dz++) set(level, x0 + 3, y, s.z() + dz, Blocks.CHEST.defaultBlockState());
        spawner(level, x1 - 5, y, z0 + 3, EntityType.PIGLIN_BRUTE);
        spawner(level, x1 - 5, y, z1 - 3, EntityType.VINDICATOR);
    }

    /**
     * The Aureum Market (Nixara): a forum under colonnades, stalls in rows, and a floor of trapdoors over pits
     * where the Hollow Merchant's customers vanish.
     */
    private static void market(ServerLevel level, StructurePositions.Structure s, int y) {
        int x0 = minX(s), x1 = maxX(s), z0 = minZ(s), z1 = maxZ(s);
        Random random = new Random(s.x() * 7L + s.z());
        SultharisBuilder.pad(level, x0, z0, x1, z1, y, b(SoFEBlocks.AUREUM_POLISHED_MARBLE));
        walls(level, x0, z0, x1, z1, y, 6, (x, yy, z) -> marble(x, yy, z, AUREUM_RUIN), (x, yy, z) -> pillar(x, yy, z, AUREUM_RUIN), null, false);
        for (int x = x0 + 4; x <= x1 - 4; x += 4) { // the colonnades along north and south
            for (int z : new int[]{z0 + 4, z1 - 4}) column(level, x, y, z, 7, pillar(x, y, z, 0.25), b(SoFEBlocks.AUREUM_MARBLE_BRICKS));
            for (int z = z0 + 1; z <= z0 + 4; z++) set(level, x, y + 7, z, b(SoFEBlocks.AUREUM_MARBLE_BRICK_SLAB));
            for (int z = z1 - 4; z <= z1 - 1; z++) set(level, x, y + 7, z, b(SoFEBlocks.AUREUM_MARBLE_BRICK_SLAB));
        }
        BlockState blue = Blocks.BLUE_WOOL.defaultBlockState(), white = Blocks.WHITE_WOOL.defaultBlockState();
        for (int x = x0 + 6; x < x1 - 8; x += 7) {
            Architecture.stall(level, x, y, z0 + 7, blue, white, random);
            Architecture.stall(level, x, y, z1 - 10, blue, white, random);
        }
        for (int dx = -6; dx <= 6; dx += 3) { // trapdoors over two-deep pits round the forum's middle
            for (int dz = -6; dz <= 6; dz += 6) {
                if (dx == 0 && dz == 0) continue;
                int x = s.x() + dx, z = s.z() + dz;
                set(level, x, y - 3, z, Blocks.MAGMA_BLOCK.defaultBlockState());
                set(level, x, y - 2, z, Blocks.AIR.defaultBlockState());
                set(level, x, y - 1, z, Blocks.SPRUCE_TRAPDOOR.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.TrapDoorBlock.HALF, net.minecraft.world.level.block.state.properties.Half.TOP));
            }
        }
        set(level, s.x(), y, s.z(), b(SoFEBlocks.AUREUM_GOLD_MOSAIC));
        column(level, s.x(), y + 1, s.z(), 3, pillar(s.x(), y, s.z(), 0), Blocks.GOLD_BLOCK.defaultBlockState()); // a statue base
        spawner(level, x1 - 4, y, s.z() - 3, EntityType.PILLAGER);
        spawner(level, x1 - 4, y, s.z() + 3, EntityType.VINDICATOR);
    }

    /** The Golden Vaults (Avarok): a marble fortress with four towers; inside, a vault floor knee-deep in gold. */
    private static void vaults(ServerLevel level, StructurePositions.Structure s, int y) {
        int x0 = minX(s), x1 = maxX(s), z0 = minZ(s), z1 = maxZ(s);
        Random random = new Random(s.x() * 3L + s.z());
        SultharisBuilder.pad(level, x0, z0, x1, z1, y, b(SoFEBlocks.AUREUM_GOLD_MOSAIC));
        walls(level, x0, z0, x1, z1, y, 14, (x, yy, z) -> marble(x, yy, z, 0.45), (x, yy, z) -> pillar(x, yy, z, 0.45),
                Blocks.GOLD_BLOCK.defaultBlockState(), false);
        for (int[] c : new int[][]{{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}}) {
            Architecture.tower(level, c[0], y, c[1], 3, 20, b(SoFEBlocks.CORRUPTED_AUREUM_MARBLE_BRICKS), Blocks.GOLD_BLOCK.defaultBlockState());
        }
        for (int x = x0 + 2; x < x1 - 1; x++) {
            for (int z = z0 + 2; z < z1 - 1; z++) {
                double d = Math.hypot(x - s.x(), z - s.z());
                if (d > 16 && random.nextInt(7) == 0) {
                    set(level, x, y, z, random.nextBoolean() ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.RAW_GOLD_BLOCK.defaultBlockState());
                    if (random.nextInt(3) == 0) set(level, x, y + 1, z, Blocks.GOLD_BLOCK.defaultBlockState());
                }
            }
        }
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            int x = s.x() + (int) Math.round(Math.cos(a) * 16), z = s.z() + (int) Math.round(Math.sin(a) * 16);
            column(level, x, y, z, 10, pillar(x, y, z, 0.4), Blocks.GOLD_BLOCK.defaultBlockState());
        }
        spawner(level, x0 + 4, y, z0 + 4, EntityType.PIGLIN_BRUTE);
        spawner(level, x0 + 4, y, z1 - 4, EntityType.EVOKER);
    }

    /**
     * The Aureum Colosseum (Shadeyn): an ellipse of arches in two storeys, tiers of seats rising from a sand floor,
     * the gate of the gladiators facing east.
     */
    private static void colosseum(ServerLevel level, StructurePositions.Structure s, int y) {
        int ax = SultharisBuilder.half(s.sizeX()) - 1, az = SultharisBuilder.half(s.sizeZ()) - 1;
        SultharisBuilder.pad(level, s.x() - ax - 1, s.z() - az - 1, s.x() + ax + 1, s.z() + az + 1, y, Blocks.SAND.defaultBlockState());
        int tiers = 9;
        for (int dx = -ax; dx <= ax; dx++) {
            for (int dz = -az; dz <= az; dz++) {
                double e = Math.sqrt((dx * dx) / (double) (ax * ax) + (dz * dz) / (double) (az * az));
                if (e > 1) continue;
                int x = s.x() + dx, z = s.z() + dz;
                boolean gate = dx > 0 && Math.abs(dz) <= 1;
                double ring = (1 - e) * Math.min(ax, az); // blocks in from the outer wall
                if (ring < 1.3) { // the outer wall: arches in two storeys
                    for (int h = 0; h < 18; h++) {
                        boolean arch = Math.floorMod(dx * 3 + dz * 5, 7) < 3 && (h >= 1 && h <= 5 || h >= 9 && h <= 13);
                        if (gate && h < 5 || arch) continue;
                        set(level, x, y + h, z, h == 7 || h == 15 ? b(SoFEBlocks.AUREUM_GOLD_MOSAIC) : marble(x, y + h, z, 0.35));
                    }
                } else if (ring < 1.3 + tiers && !gate) { // the seats, highest at the back
                    int height = (int) Math.round((tiers - (ring - 1.3)) * 1.1);
                    for (int h = 0; h < height; h++) set(level, x, y + h, z, marble(x, y + h, z, 0.3));
                    set(level, x, y + height, z, b(SoFEBlocks.AUREUM_MARBLE_BRICK_SLAB));
                }
            }
        }
        spawner(level, s.x() + ax - 3, y, s.z() - 4, EntityType.SKELETON);
        spawner(level, s.x() + ax - 3, y, s.z() + 4, EntityType.ZOMBIE);
        StructurePositions.get().gates().values().stream()
                .filter(g -> Math.abs(g.x() - s.x()) <= ax + 2 && Math.abs(g.z() - s.z()) <= az + 2)
                .forEach(g -> StructureBuilder.placeGate(level, g, y));
    }

    /**
     * The Shadow Throne (Envyris): the roofless ruin of the senate hall, broken columns and cracked marble black
     * with veins of gold, a throne on a dais at the west end, mirrors of black glass along the walls.
     */
    private static void shadowThrone(ServerLevel level, StructurePositions.Structure s, int y) {
        int x0 = minX(s), x1 = maxX(s), z0 = minZ(s), z1 = maxZ(s);
        Random random = new Random(s.x() * 5L + s.z());
        SultharisBuilder.pad(level, x0, z0, x1, z1, y, b(SoFEBlocks.CORRUPTED_AUREUM_MARBLE_BRICKS));
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                if (!edge) continue;
                boolean door = x == x1 && Math.abs(z - s.z()) <= 1;
                int height = 4 + (int) Math.abs(Math.sin(x * 0.3 + z * 0.2) * 9); // the walls broken at uneven heights
                for (int h = 0; h < height; h++) {
                    if (door && h < 4) continue;
                    set(level, x, y + h, z, marble(x, y + h, z, 0.75));
                }
                if (!door && Math.floorMod(x + z, 9) == 0 && height > 6) {
                    for (int h = 2; h < 5; h++) set(level, x, y + h, z, Blocks.TINTED_GLASS.defaultBlockState()); // the black mirrors
                }
            }
        }
        for (int x = x0 + 6; x < x1 - 4; x += 6) {
            for (int z : new int[]{z0 + 6, z1 - 6}) brokenColumn(level, x, y, z, 11, pillar(x, y, z, 0.8), random);
        }
        // the dais and the throne at the west end
        for (int step = 0; step < 3; step++) {
            for (int z = s.z() - 5 + step; z <= s.z() + 5 - step; z++) {
                for (int x = x0 + 2; x <= x0 + 8 - step * 2; x++) set(level, x, y + step, z, b(SoFEBlocks.AUREUM_GOLD_MOSAIC));
            }
        }
        int tx = x0 + 3;
        set(level, tx, y + 3, s.z(), Blocks.GOLD_BLOCK.defaultBlockState());
        set(level, tx + 1, y + 3, s.z(), b(SoFEBlocks.AUREUM_MARBLE_BRICK_STAIRS).setValue(StairBlock.FACING, Direction.WEST));
        for (int h = 3; h < 7; h++) set(level, tx - 1, y + h, s.z(), h == 6 ? Blocks.GOLD_BLOCK.defaultBlockState() : b(SoFEBlocks.CORRUPTED_AUREUM_MARBLE_PILLAR));
        set(level, tx, y + 4, s.z() - 1, Blocks.GOLD_BLOCK.defaultBlockState());
        set(level, tx, y + 4, s.z() + 1, Blocks.GOLD_BLOCK.defaultBlockState());
        spawner(level, x1 - 6, y, z0 + 5, EntityType.VEX);
        spawner(level, x1 - 6, y, z1 - 5, EntityType.WITHER_SKELETON);
    }

    // ------------------------------------------------------------------------------------------------ Act V: Sulthari besieged

    /** The height of the Celestial Spire's crown, where Prython waits (boss_lairs.json says the same). */
    static final int SPIRE_TOP = 200;
    /** The floor of the Inverted Throne, deep beneath Sulthari. */
    static final int THRONE_FLOOR = -20;

    /**
     * The Temple of Sulthari (Solrath): a hall of sandstone and gold under a golden dome, split by purple cracks where
     * Prython's light has corrupted it, columns down both sides and a sun disk over the altar at the east end.
     */
    private static void temple(ServerLevel level, StructurePositions.Structure s, int y) {
        int x0 = minX(s), x1 = maxX(s), z0 = minZ(s), z1 = maxZ(s);
        BlockState sand = b(SoFEBlocks.SULTHARI_SANDSTONE_BRICKS), gold = Blocks.GOLD_BLOCK.defaultBlockState();
        BlockState crack = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        SultharisBuilder.pad(level, x0, z0, x1, z1, y, b(SoFEBlocks.SULTHARI_GLAZED_TILES));
        walls(level, x0, z0, x1, z1, y, 14, (x, yy, z) -> ruin(x, yy, z, sand, crack, 0.12), (x, yy, z) -> b(SoFEBlocks.SULTHARI_BRASS_TRIM),
                b(SoFEBlocks.SULTHARI_BRASS_PLATING), true);
        roof(level, x0, z0, x1, z1, y + 14, b(SoFEBlocks.SULTHARI_SANDSTONE_BRICK_SLAB), b(SoFEBlocks.SULTHARI_AETHERIUM_LAMP));
        Architecture.openRoof(level, s.x(), y + 14, s.z(), 11);
        Architecture.dome(level, s.x(), y + 14, s.z(), 11, Blocks.HONEYCOMB_BLOCK.defaultBlockState(), gold, Blocks.LIGHTNING_ROD.defaultBlockState());
        for (int x = x0 + 5; x < x1 - 3; x += 6) {
            for (int z : new int[]{z0 + 6, z1 - 6}) column(level, x, y, z, 13, b(SoFEBlocks.SULTHARI_BRASS_TRIM), gold);
        }
        // the altar and the sun disk at the east end
        for (int dz = -3; dz <= 3; dz++) {
            for (int dx = 0; dx < 3; dx++) set(level, x1 - 3 - dx, y, s.z() + dz, gold);
        }
        for (int dy = 0; dy < 9; dy++) {
            for (int dz = -4; dz <= 4; dz++) {
                if ((dy - 4) * (dy - 4) + dz * dz <= 16) set(level, x1 - 1, y + 3 + dy, s.z() + dz, (dy + dz) % 3 == 0 ? Blocks.SHROOMLIGHT.defaultBlockState() : gold);
            }
        }
        // Prython's corruption: purple light in the cracks of the floor
        java.util.Random random = new java.util.Random(s.x() * 29L + s.z());
        for (int i = 0; i < 40; i++) {
            int x = x0 + 2 + random.nextInt(x1 - x0 - 3), z = z0 + 2 + random.nextInt(z1 - z0 - 3);
            set(level, x, y - 1, z, random.nextBoolean() ? crack : Blocks.AMETHYST_BLOCK.defaultBlockState());
        }
    }

    /**
     * The Celestial Spire (Prython): a tower of white quartz banded in gold rising from the land to its crown at
     * SPIRE_TOP, a stair winding up inside its wall, and on top an open platform ringed by spikes of gold, the halo of
     * Pride. Its door faces the city.
     */
    private static void spire(ServerLevel level, StructurePositions.Structure s) {
        int ground = SultharisBuilder.surfaceY(level, s.x(), s.z());
        int cx = s.x(), cz = s.z(), r = 8;
        BlockState wall = Blocks.QUARTZ_BRICKS.defaultBlockState(), band = Blocks.GOLD_BLOCK.defaultBlockState();
        SultharisBuilder.pad(level, cx - r - 3, cz - r - 3, cx + r + 3, cz + r + 3, ground, Blocks.SMOOTH_QUARTZ.defaultBlockState(), 6);
        for (int y = ground; y < SPIRE_TOP; y++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > r + 0.5) continue;
                    boolean door = dz == r && Math.abs(dx) <= 1 && y < ground + 4;
                    if (d > r - 0.7) {
                        if (!door) set(level, cx + dx, y, cz + dz, (y - ground) % 12 == 0 ? band : wall);
                    } else {
                        set(level, cx + dx, y, cz + dz, Blocks.AIR.defaultBlockState());
                    }
                }
            }
            // the stair: a step of the spiral every block of height, two wide, round the inside of the wall
            double a = (y - ground) * Math.PI / 10;
            for (int w = 0; w < 3; w++) {
                int sx = (int) Math.round(Math.cos(a) * (r - 1 - w * 0.9)), sz = (int) Math.round(Math.sin(a) * (r - 1 - w * 0.9));
                set(level, cx + sx, y, cz + sz, Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.TOP));
            }
            if ((y - ground) % 8 == 4) set(level, cx, y, cz, Blocks.END_ROD.defaultBlockState()); // light down the middle
        }
        // the crown: a platform wider than the tower, ringed by spikes of gold
        int pr = 16;
        for (int dx = -pr; dx <= pr; dx++) {
            for (int dz = -pr; dz <= pr; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > pr) continue;
                set(level, cx + dx, SPIRE_TOP, cz + dz, d > pr - 1.5 ? band : (Math.floorMod(dx + dz, 4) == 0 ? Blocks.CHISELED_QUARTZ_BLOCK : Blocks.SMOOTH_QUARTZ).defaultBlockState());
                for (int h = 1; h < 6; h++) set(level, cx + dx, SPIRE_TOP + h, cz + dz, Blocks.AIR.defaultBlockState());
            }
        }
        set(level, cx, SPIRE_TOP, cz, Blocks.AIR.defaultBlockState()); // the stair comes out in the middle
        for (int i = 0; i < 16; i++) {
            double a = i * Math.PI / 8;
            int x = cx + (int) Math.round(Math.cos(a) * (pr - 1)), z = cz + (int) Math.round(Math.sin(a) * (pr - 1));
            int tall = 4 + (i % 2) * 4;
            for (int h = 1; h <= tall; h++) set(level, x, SPIRE_TOP + h, z, h == tall ? Blocks.END_ROD.defaultBlockState() : band);
        }
        // the last steps up through the floor of the crown
        for (int h = 0; h < 3; h++) set(level, cx + 1, SPIRE_TOP - 2 + h, cz, Blocks.QUARTZ_STAIRS.defaultBlockState());
        StructurePositions.get().gates().values().stream()
                .filter(g -> Math.abs(g.x() - cx) <= r + 2 && Math.abs(g.z() - cz) <= r + 2)
                .forEach(g -> StructureBuilder.placeGate(level, g, ground));
    }

    /**
     * The Inverted Throne (Nahrazel): deep beneath Sulthari, where the Spire sank. A winding stair goes down from a
     * sealed arch beside the Spire into a vast cavern of blackstone and crying obsidian, spikes of obsidian hanging from
     * its roof like an upside-down city, purple light, and an obsidian throne at the far end.
     */
    private static void invertedThrone(ServerLevel level, StructurePositions.Structure s) {
        int cx = s.x(), cz = s.z(), r = 30, f = THRONE_FLOOR;
        java.util.Random random = new java.util.Random(cx * 41L + cz);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r) continue;
                int ceiling = (int) Math.round(22 * Math.sqrt(1 - (d / r) * (d / r))) + 6;
                set(level, cx + dx, f - 1, cz + dz, d < 18 && Math.floorMod(dx * 3 + dz, 7) == 0 ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                        : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                for (int h = 0; h < ceiling; h++) set(level, cx + dx, f + h, cz + dz, Blocks.AIR.defaultBlockState());
                set(level, cx + dx, f + ceiling, cz + dz, Blocks.BLACKSTONE.defaultBlockState());
                if (random.nextInt(18) == 0 && ceiling > 10) { // spikes hanging from the roof: the upside-down city
                    int len = 3 + random.nextInt(ceiling / 2);
                    for (int h = 1; h <= len; h++) set(level, cx + dx, f + ceiling - h, cz + dz, h > len - 2 ? Blocks.CRYING_OBSIDIAN.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState());
                }
                if (Math.abs(d - (r - 4)) < 0.6 && Math.floorMod(dx + dz, 9) == 0) {
                    set(level, cx + dx, f, cz + dz, Blocks.SOUL_LANTERN.defaultBlockState());
                }
            }
        }
        // the throne, at the north end
        for (int dx = -3; dx <= 3; dx++) {
            for (int step = 0; step < 3; step++) set(level, cx + dx, f + step, cz - r + 6 + step, Blocks.OBSIDIAN.defaultBlockState());
        }
        for (int h = 3; h < 12; h++) {
            set(level, cx - 2, f + h, cz - r + 6, Blocks.OBSIDIAN.defaultBlockState());
            set(level, cx + 2, f + h, cz - r + 6, Blocks.OBSIDIAN.defaultBlockState());
            if (h > 8) set(level, cx, f + h, cz - r + 6, Blocks.CRYING_OBSIDIAN.defaultBlockState());
        }
        // the way down: a winding stair from a sealed arch on the surface, south of the cavern
        int sx0 = cx, sz0 = cz + r + 6;
        int top = SultharisBuilder.surfaceY(level, sx0, sz0);
        for (int y = top + 4; y >= f; y--) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz);
                    if (d > 3.5) continue;
                    set(level, sx0 + dx, y, sz0 + dz, d > 2.6 ? Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState());
                }
            }
            double a = (top - y) * Math.PI / 6;
            set(level, sx0 + (int) Math.round(Math.cos(a) * 1.6), y, sz0 + (int) Math.round(Math.sin(a) * 1.6), Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.SlabBlock.TYPE, net.minecraft.world.level.block.state.properties.SlabType.TOP));
            if ((top - y) % 7 == 3) set(level, sx0, y, sz0, Blocks.SOUL_LANTERN.defaultBlockState());
        }
        for (int z = sz0 - 3; z >= cz + r - 1; z--) { // the passage into the cavern
            for (int dx = -1; dx <= 1; dx++) {
                set(level, sx0 + dx, f - 1, z, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                for (int h = 0; h < 4; h++) set(level, sx0 + dx, f + h, z, Blocks.AIR.defaultBlockState());
            }
        }
        StructurePositions.get().gates().values().stream()
                .filter(g -> Math.abs(g.x() - sx0) <= 4 && Math.abs(g.z() - sz0) <= 4)
                .forEach(g -> StructureBuilder.placeGate(level, g, top));
    }

    // ------------------------------------------------------------------------------------------------ Nordrath, below

    /** Depth of the Caverns' floor under the entrance's ground. */
    static final int CAVERNS_DEPTH = 28;
    /** How far south of the Caverns hall its entrance arch stands (the tunnel, about one step down every two blocks). */
    static final int TUNNEL = 60;

    /**
     * The Nordrath Caverns (Fenrath): from the sealed arch of the entrance a tunnel goes down northwards into a
     * cave hall of basalt and dripstone, with pools of acid (slime and green-lit water) where the Devourer waits.
     */
    private static void caverns(ServerLevel level, StructurePositions.Structure s) {
        int top = SultharisBuilder.surfaceY(level, s.x(), s.z() + SultharisBuilder.half(s.sizeZ()) + TUNNEL);
        int cy = top - CAVERNS_DEPTH;
        // the tunnel from the entrance (south of the hall) down to the hall's south side
        int entranceZ = s.z() + SultharisBuilder.half(s.sizeZ()) + TUNNEL, hallZ1 = maxZ(s);
        int length = entranceZ - hallZ1;
        for (int i = 0; i <= length; i++) {
            int z = entranceZ - i;
            int floor = top - (int) Math.round((double) CAVERNS_DEPTH * i / length);
            for (int dx = -2; dx <= 2; dx++) {
                set(level, s.x() + dx, floor - 1, z, Math.abs(dx) == 2 ? Blocks.SMOOTH_BASALT.defaultBlockState() : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
                for (int h = 0; h < 5; h++) set(level, s.x() + dx, floor + h, z, Blocks.AIR.defaultBlockState());
                set(level, s.x() + dx, floor + 5, z, Blocks.BASALT.defaultBlockState());
            }
            if (i % 8 == 4) set(level, s.x() - 2, floor + 2, z, Blocks.SOUL_LANTERN.defaultBlockState());
        }
        Random random = new Random(s.x() * 11L + s.z());
        int rx = SultharisBuilder.half(s.sizeX()) - 1, rz = SultharisBuilder.half(s.sizeZ()) - 1;
        for (int dx = -rx; dx <= rx; dx++) {
            for (int dz = -rz; dz <= rz; dz++) {
                double e = (dx * dx) / (double) (rx * rx) + (dz * dz) / (double) (rz * rz);
                if (e > 1) continue;
                int x = s.x() + dx, z = s.z() + dz;
                int ceiling = (int) Math.round(14 * (1 - e)) + 4;
                set(level, x, cy - 1, z, random.nextInt(5) == 0 ? Blocks.SMOOTH_BASALT.defaultBlockState() : Blocks.DEEPSLATE_TILES.defaultBlockState());
                for (int h = 0; h < ceiling; h++) set(level, x, cy + h, z, Blocks.AIR.defaultBlockState());
                set(level, x, cy + ceiling, z, Blocks.BASALT.defaultBlockState());
                if (random.nextInt(30) == 0 && ceiling > 6) {
                    set(level, x, cy + ceiling - 1, z, Blocks.POINTED_DRIPSTONE.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.PointedDripstoneBlock.TIP_DIRECTION, Direction.DOWN));
                }
                double acid = Math.sin(dx * 0.4) * Math.cos(dz * 0.35);
                if (acid > 0.75 && e > 0.25) {
                    set(level, x, cy - 1, z, Blocks.WATER.defaultBlockState());
                    set(level, x, cy - 2, z, Blocks.SLIME_BLOCK.defaultBlockState());
                    set(level, x, cy - 3, z, Blocks.VERDANT_FROGLIGHT.defaultBlockState());
                }
            }
        }
        for (int i = 0; i < 6; i++) {
            double a = i * Math.PI / 3;
            set(level, s.x() + (int) (Math.cos(a) * (rx - 4)), cy, s.z() + (int) (Math.sin(a) * (rz - 4)), SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
        }
        spawner(level, s.x() - rx + 5, cy, s.z(), EntityType.CAVE_SPIDER);
        spawner(level, s.x() + rx - 5, cy, s.z(), EntityType.SLIME);
    }

    /**
     * The Feast Halls (Gularth): north of the Caverns, at the same depth, a great hall of runestone and dark
     * timber with long tables still laid, barrels, bones and braziers. Its gate (Fenrath beaten) is its south door.
     */
    private static void feastHalls(ServerLevel level, StructurePositions.Structure s) {
        var caverns = StructurePositions.get().structure("sofe:nordrath/caverns");
        int top = caverns.map(c -> SultharisBuilder.surfaceY(level, c.x(), c.z() + SultharisBuilder.half(c.sizeZ()) + TUNNEL))
                .orElse(SultharisBuilder.surfaceY(level, s.x(), s.z()));
        int cy = top - CAVERNS_DEPTH;
        int x0 = minX(s), x1 = maxX(s), z0 = minZ(s), z1 = maxZ(s);
        BlockState runestone = b(SoFEBlocks.NORDRATH_RUNESTONE_BRICKS), timber = b(SoFEBlocks.NORDRATH_DARK_TIMBER);
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                set(level, x, cy - 1, z, b(SoFEBlocks.NORDRATH_DARK_PLANKS));
                for (int h = 0; h < 10; h++) {
                    boolean door = z == z1 && Math.abs(x - s.x()) <= 1 && h < 4;
                    BlockState wall = Math.floorMod(x - x0, 6) == 0 || Math.floorMod(z - z0, 6) == 0 ? timber : runestone;
                    set(level, x, cy + h, z, edge && !door ? ruin(x, cy + h, z, wall, b(SoFEBlocks.CORRUPTED_NORDRATH_RUNESTONE_BRICKS), 0.4) : Blocks.AIR.defaultBlockState());
                }
                set(level, x, cy + 10, z, runestone);
            }
        }
        // the passage from the Caverns' north side to this hall's south door
        caverns.ifPresent(c -> {
            for (int z = minZ(c) - 1; z > z1; z--) {
                for (int dx = -1; dx <= 1; dx++) {
                    set(level, s.x() + dx, cy - 1, z, b(SoFEBlocks.NORDRATH_DARK_PLANKS));
                    for (int h = 0; h < 4; h++) set(level, s.x() + dx, cy + h, z, Blocks.AIR.defaultBlockState());
                }
            }
        });
        Random random = new Random(s.x() * 19L + s.z());
        for (int x : new int[]{s.x() - 8, s.x() + 8}) { // two long tables, still laid
            for (int z = z0 + 5; z < z1 - 5; z++) {
                set(level, x, cy, z, Blocks.SPRUCE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
                set(level, x - 1, cy, z, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
                set(level, x + 1, cy, z, Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
                if (random.nextInt(3) == 0) {
                    BlockState[] food = {Blocks.CAKE.defaultBlockState(), Blocks.BONE_BLOCK.defaultBlockState(), Blocks.CANDLE.defaultBlockState(),
                            Blocks.PUMPKIN.defaultBlockState()};
                    set(level, x, cy + 1, z, food[random.nextInt(food.length)]);
                }
            }
        }
        for (int z = z0 + 3; z < z1 - 2; z += 6) {
            set(level, x0 + 1, cy, z, Blocks.BARREL.defaultBlockState());
            set(level, x1 - 1, cy, z, Blocks.BARREL.defaultBlockState());
            set(level, s.x(), cy, z, SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
        }
        StructurePositions.get().gates().values().stream()
                .filter(g -> Math.abs(g.x() - s.x()) <= SultharisBuilder.half(s.sizeX()) && Math.abs(g.z() - s.z()) <= SultharisBuilder.half(s.sizeZ()) + 1)
                .forEach(g -> StructureBuilder.placeGate(level, g, cy));
        spawner(level, x0 + 3, cy, z0 + 3, EntityType.ZOMBIE);
        spawner(level, x1 - 3, cy, z0 + 3, EntityType.HUSK);
    }
}
