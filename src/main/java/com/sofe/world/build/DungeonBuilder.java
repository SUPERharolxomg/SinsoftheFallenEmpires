package com.sofe.world.build;

import com.sofe.registry.EntityRegistry;
import com.sofe.registry.SoFEBlocks;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;
import java.util.function.Supplier;

import static com.sofe.world.build.SultharisBuilder.set;

/**
 * The lesser dungeons of every region (docs/Mundo.md, W6), found on the way and given as dungeon quests: a small
 * Crypt (one hall of the dead, two spawners, a chest) and a medium Ruin (a court with a rune puzzle before a Sealed
 * Gate, and behind it a roofed hall with its spawners and its treasure; the guardian wakes when the runes burn).
 * Built in the region's own stone; the same place is built the same way every time.
 */
final class DungeonBuilder {
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    /**
     * The stone, trim, floor and light of a region, and the dead that haunt its dungeons (never the ruin's guardian,
     * so the guardian is the one a dungeon quest asks for: scripts/make_dungeons.py).
     */
    private record Palette(BlockState wall, BlockState trim, BlockState floor, BlockState light,
                           Supplier<? extends EntityType<?>> first, Supplier<? extends EntityType<?>> second) {
    }

    private DungeonBuilder() {
    }

    private static Palette palette(String region) {
        return switch (region) {
            case "nordrath" -> new Palette(b(SoFEBlocks.NORDRATH_RUNESTONE_BRICKS), b(SoFEBlocks.NORDRATH_DARK_PLANKS), Blocks.COBBLED_DEEPSLATE.defaultBlockState(),
                    Blocks.SOUL_LANTERN.defaultBlockState(), EntityRegistry.DRAUGR, () -> EntityType.STRAY);
            case "parsivan" -> new Palette(b(SoFEBlocks.PARSIVAN_WHITE_PLASTER), b(SoFEBlocks.PARSIVAN_TURQUOISE_TILES), b(SoFEBlocks.PARSIVAN_LAPIS_MOSAIC),
                    Blocks.LANTERN.defaultBlockState(), EntityRegistry.MIRAGE_DANCER, () -> EntityType.CAVE_SPIDER);
            case "khemet" -> new Palette(b(SoFEBlocks.KHEMET_CARVED_SANDSTONE), b(SoFEBlocks.KHEMET_GOLD_HIEROGLYPHS), b(SoFEBlocks.KHEMET_PAINTED_LIMESTONE),
                    Blocks.SOUL_LANTERN.defaultBlockState(), EntityRegistry.BOG_MUMMY, () -> EntityType.HUSK);
            case "aureum" -> new Palette(b(SoFEBlocks.AUREUM_MARBLE_BRICKS), b(SoFEBlocks.AUREUM_GOLD_MOSAIC), b(SoFEBlocks.AUREUM_POLISHED_MARBLE),
                    Blocks.LANTERN.defaultBlockState(), EntityRegistry.GILDED_LEGIONNAIRE, () -> EntityType.SKELETON);
            default -> new Palette(b(SoFEBlocks.SULTHARI_SANDSTONE_BRICKS), b(SoFEBlocks.SULTHARI_BRASS_PLATING), b(SoFEBlocks.SULTHARI_GLAZED_TILES),
                    b(SoFEBlocks.SULTHARI_AETHERIUM_LAMP), EntityRegistry.SAND_GHOUL, EntityRegistry.SAND_GHOUL);
        };
    }

    /** The build of a "&lt;region&gt;/crypt" or "&lt;region&gt;/ruin"; false for any other piece. */
    static boolean blockout(ServerLevel level, StructurePositions.Structure s, String piece) {
        String region = piece.substring(0, piece.indexOf('/'));
        int y = SultharisBuilder.surfaceY(level, s.x(), s.z());
        if (piece.endsWith("/crypt")) crypt(level, s, y, palette(region));
        else if (piece.endsWith("/ruin")) ruin(level, s, y, palette(region));
        else return false;
        StructureBuilder.placeGates(level, s, y);
        return true;
    }

    // ------------------------------------------------------------------------------------------------ the crypt

    /** A squat hall of the dead, its roof fallen in places: sarcophagi along the walls, two spawners, a chest at the back. */
    private static void crypt(ServerLevel level, StructurePositions.Structure s, int y, Palette p) {
        int x0 = s.x() - 8, x1 = s.x() + 8, z0 = s.z() - 8, z1 = s.z() + 8, height = 6;
        Random random = new Random(s.x() * 31L + s.z());
        SultharisBuilder.pad(level, x0, z0, x1, z1, y, p.floor(), height + 4);
        walls(level, x0, z0, x1, z1, y, height, p, random, true);
        roof(level, x0, z0, x1, z1, y + height, p, random, 0.25);
        door(level, s.x(), z1, y);
        for (int x = x0 + 2; x <= x1 - 2; x += 3) { // sarcophagi along both long walls
            for (int z : new int[]{z0 + 2, z1 - 3}) {
                if (Math.abs(x - s.x()) <= 1 && z == z1 - 3) continue; // the way in
                set(level, x, y, z, Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
            }
        }
        pillar(level, x0 + 4, z0 + 4, y, height, p);
        pillar(level, x1 - 4, z0 + 4, y, height, p);
        pillar(level, x0 + 4, z1 - 5, y, height, p);
        pillar(level, x1 - 4, z1 - 5, y, height, p);
        spawner(level, x0 + 3, y, s.z(), p.first().get());
        spawner(level, x1 - 3, y, s.z(), p.second().get());
        chest(level, s.x(), y, z0 + 1, "sofe:chests/dungeon_small", random);
        hang(level, s.x(), y + height - 1, s.z(), p);
        cobwebs(level, x0 + 1, z0 + 1, x1 - 1, z1 - 1, y, height, random);
    }

    // ------------------------------------------------------------------------------------------------ the ruin

    /**
     * An open court (where the rune puzzle stands, data/sofe/puzzles) behind a gate in the outer wall, then a wall with a
     * Sealed Gate that the puzzle opens, and behind it a roofed hall: four pillars, two spawners and two chests.
     */
    private static void ruin(ServerLevel level, StructurePositions.Structure s, int y, Palette p) {
        int x0 = s.x() - 18, x1 = s.x() + 18, z0 = s.z() - 14, z1 = s.z() + 14, height = 7, inner = s.z() - 3;
        Random random = new Random(s.x() * 17L + s.z());
        SultharisBuilder.pad(level, x0, z0, x1, z1, y, p.floor(), height + 5);
        walls(level, x0, z0, x1, z1, y, height, p, random, true);
        for (int x = x0 + 1; x < x1; x++) { // the inner wall: its Sealed Gate stands in the middle (structure_positions.json)
            for (int dy = 0; dy < height; dy++) set(level, x, y + dy, inner, dy == height - 1 ? p.trim() : p.wall());
        }
        roof(level, x0, z0, x1, inner, y + height, p, random, 0.15);
        door(level, s.x(), z1, y);
        // the court: broken columns and rubble
        for (int x = x0 + 4; x <= x1 - 4; x += 8) {
            for (int z : new int[]{inner + 4, z1 - 4}) {
                int h = 2 + random.nextInt(4);
                for (int dy = 0; dy < h; dy++) set(level, x, y + dy, z, dy == h - 1 && h > 4 ? p.trim() : p.wall());
            }
        }
        spawner(level, x1 - 3, y, z1 - 3, p.second().get());
        // the hall behind the seal
        for (int x : new int[]{s.x() - 9, s.x() + 9}) {
            pillar(level, x, z0 + 4, y, height, p);
            pillar(level, x, inner - 4, y, height, p);
        }
        spawner(level, x0 + 3, y, z0 + 3, p.first().get());
        spawner(level, x1 - 3, y, z0 + 3, p.first().get());
        set(level, s.x(), y, z0 + 2, p.trim()); // an altar with the treasure on either side
        set(level, s.x(), y + 1, z0 + 2, p.light());
        chest(level, s.x() - 2, y, z0 + 1, "sofe:chests/dungeon_medium", random);
        chest(level, s.x() + 2, y, z0 + 1, "sofe:chests/dungeon_medium", random);
        hang(level, s.x(), y + height - 1, (z0 + inner) / 2, p);
        cobwebs(level, x0 + 1, z0 + 1, x1 - 1, inner - 1, y, height, random);
    }

    // ------------------------------------------------------------------------------------------------ parts

    /** The outer walls, their top broken here and there. */
    private static void walls(ServerLevel level, int x0, int z0, int x1, int z1, int y, int height, Palette p, Random random, boolean ruined) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (x != x0 && x != x1 && z != z0 && z != z1) continue;
                int top = ruined && random.nextInt(5) == 0 ? height - 1 - random.nextInt(2) : height;
                for (int dy = 0; dy < top; dy++) {
                    boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
                    set(level, x, y + dy, z, corner || dy == 0 ? p.trim() : p.wall());
                }
            }
        }
    }

    /** A flat roof of the region's stone, a share of it fallen in (light and rain come through). */
    private static void roof(ServerLevel level, int x0, int z0, int x1, int z1, int y, Palette p, Random random, double fallen) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                if (!edge && random.nextDouble() < fallen) continue;
                set(level, x, y, z, edge ? p.trim() : p.wall());
            }
        }
    }

    /** The way in: three wide and three high in the south wall. */
    private static void door(ServerLevel level, int x, int z, int y) {
        for (int dx = -1; dx <= 1; dx++) for (int dy = 0; dy < 3; dy++) set(level, x + dx, y + dy, z, AIR);
    }

    private static void pillar(ServerLevel level, int x, int z, int y, int height, Palette p) {
        for (int dy = 0; dy < height; dy++) set(level, x, y + dy, z, dy == 0 || dy == height - 1 ? p.trim() : p.wall());
    }

    /** A lantern hung on a chain from the roof. */
    private static void hang(ServerLevel level, int x, int y, int z, Palette p) {
        set(level, x, y + 1, z, p.wall());
        BlockState light = p.light().getBlock() instanceof net.minecraft.world.level.block.LanternBlock
                ? p.light().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true) : p.light();
        set(level, x, y, z, light);
    }

    private static void cobwebs(ServerLevel level, int x0, int z0, int x1, int z1, int y, int height, Random random) {
        for (int i = 0; i < 10; i++) {
            int x = x0 + random.nextInt(x1 - x0 + 1), z = z0 + random.nextInt(z1 - z0 + 1), dy = height - 1 - random.nextInt(2);
            if (level.getBlockState(new BlockPos(x, y + dy, z)).isAir()) set(level, x, y + dy, z, Blocks.COBWEB.defaultBlockState());
        }
    }

    private static void spawner(ServerLevel level, int x, int y, int z, EntityType<?> type) {
        BlockPos pos = new BlockPos(x, y, z);
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) spawner.setEntityId(type, level.getRandom());
    }

    /** A chest that fills from its loot table the first time it is opened (gear rolled for the Bearer who opens it). */
    private static void chest(ServerLevel level, int x, int y, int z, String table, Random random) {
        BlockPos pos = new BlockPos(x, y, z);
        level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, net.minecraft.core.Direction.SOUTH), 2);
        if (level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity chest) {
            chest.setLootTable(new ResourceLocation(table), random.nextLong());
        }
    }

    private static BlockState b(net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.Block> block) {
        return block.get().defaultBlockState();
    }
}
