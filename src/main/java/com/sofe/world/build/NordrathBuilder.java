package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;

/**
 * The blockouts of Nordrath (docs/Mundo.md, W6): the Nordrath Forge (Kaleth), the Nordrath Arena
 * (Serath), the Burning Citadel (Vorath) and the sealed entrance of the Nordrath Caverns. Runestone
 * and dark timber; the Citadel in their corrupted, scorched versions. Each has its gate on the side
 * facing Sulthari (south). Boss rooms and arenas are only protected zones and have no build of their own.
 */
final class NordrathBuilder {

    private NordrathBuilder() {
    }

    static boolean blockout(ServerLevel level, StructurePositions.Structure s, String piece) {
        int y = SultharisBuilder.surfaceY(level, s.x(), s.z());
        switch (piece) {
            case "nordrath/city" -> {
                NordrathCity.build(level, s);
                return true;
            }
            case "nordrath/forge" -> forge(level, s, y);
            case "nordrath/arena" -> arena(level, s, y);
            case "nordrath/burning_citadel" -> citadel(level, s, y);
            case "nordrath/caverns_entrance" -> cavernsEntrance(level, s, y);
            case "nordrath/forge_boss_room", "nordrath/arena_floor", "nordrath/citadel_arena" -> {
                return true; // zones only, inside the buildings above
            }
            default -> {
                return false;
            }
        }
        StructureBuilder.placeGates(level, s, y);
        return true;
    }

    private static BlockState runestone() {
        return SoFEBlocks.NORDRATH_RUNESTONE_BRICKS.get().defaultBlockState();
    }

    /** A long hall with timber pillars, braziers and two spawners; the boss room is the far (north) third. */
    private static void forge(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - SultharisBuilder.half(s.sizeX()), maxX = s.x() + SultharisBuilder.half(s.sizeX()) - 1;
        int minZ = s.z() - SultharisBuilder.half(s.sizeZ()), maxZ = s.z() + SultharisBuilder.half(s.sizeZ()) - 1;
        hall(level, minX, minZ, maxX, maxZ, y, 12, runestone(), SoFEBlocks.NORDRATH_DARK_TIMBER.get().defaultBlockState());
        // inner wall with an opening: the boss room behind it
        int innerZ = minZ + (maxZ - minZ) / 3;
        for (int x = minX + 1; x < maxX; x++) {
            if (Math.abs(x - s.x()) <= 2) continue;
            for (int dy = 0; dy < 12; dy++) SultharisBuilder.set(level, x, y + dy, innerZ, runestone());
        }
        // anvils and furnaces of the forge along the walls
        for (int z = innerZ + 4; z < maxZ - 3; z += 6) {
            SultharisBuilder.set(level, minX + 2, y, z, Blocks.BLAST_FURNACE.defaultBlockState());
            SultharisBuilder.set(level, maxX - 2, y, z, Blocks.ANVIL.defaultBlockState());
        }
        spawner(level, new BlockPos(s.x() - 8, y, innerZ + 12), EntityType.ZOMBIE);
        spawner(level, new BlockPos(s.x() + 8, y, innerZ + 12), EntityType.SKELETON);
    }

    /** A round arena with steps for the crowd of dead warriors that never came. */
    private static void arena(ServerLevel level, StructurePositions.Structure s, int y) {
        int r = Math.min(SultharisBuilder.half(s.sizeX()), SultharisBuilder.half(s.sizeZ())) - 2;
        SultharisBuilder.pad(level, s.x() - r - 1, s.z() - r - 1, s.x() + r + 1, s.z() + r + 1, y, Blocks.PACKED_MUD.defaultBlockState());
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > r) continue;
                if (d > r - 1.2) {
                    boolean gate = dz > 0 && Math.abs(dx) <= 1;
                    for (int dy = 0; dy < 9; dy++) {
                        if (gate && dy < 4) continue;
                        SultharisBuilder.set(level, s.x() + dx, y + dy, s.z() + dz, runestone());
                    }
                    SultharisBuilder.set(level, s.x() + dx, y + 9, s.z() + dz, SoFEBlocks.NORDRATH_DARK_TIMBER.get().defaultBlockState());
                } else if (d > r - 5 && !(dz > 0 && Math.abs(dx) <= 1)) {
                    int step = (int) (d - (r - 5)); // seating rises toward the wall
                    for (int dy = 0; dy < step; dy++) SultharisBuilder.set(level, s.x() + dx, y + dy, s.z() + dz, SoFEBlocks.NORDRATH_RUNESTONE_BRICK_SLAB.get().defaultBlockState());
                } else if (Math.floorMod(dx * 7 + dz * 3, 23) == 0) {
                    SultharisBuilder.set(level, s.x() + dx, y - 1, s.z() + dz, Blocks.GRAVEL.defaultBlockState());
                }
            }
        }
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            int bx = s.x() + (int) Math.round(Math.cos(a) * (r - 6)), bz = s.z() + (int) Math.round(Math.sin(a) * (r - 6));
            SultharisBuilder.set(level, bx, y, bz, SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
        }
    }

    /** Scorched walls, four burning towers and an open arena of black stone in the middle. */
    private static void citadel(ServerLevel level, StructurePositions.Structure s, int y) {
        int minX = s.x() - SultharisBuilder.half(s.sizeX()), maxX = s.x() + SultharisBuilder.half(s.sizeX()) - 1;
        int minZ = s.z() - SultharisBuilder.half(s.sizeZ()), maxZ = s.z() + SultharisBuilder.half(s.sizeZ()) - 1;
        BlockState scorched = SoFEBlocks.CORRUPTED_NORDRATH_RUNESTONE_BRICKS.get().defaultBlockState();
        BlockState burning = SoFEBlocks.CORRUPTED_NORDRATH_DARK_TIMBER.get().defaultBlockState();
        SultharisBuilder.pad(level, minX, minZ, maxX, maxZ, y, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean edge = x == minX || x == maxX || z == minZ || z == maxZ;
                if (!edge) continue;
                boolean gate = z == maxZ && Math.abs(x - s.x()) <= 1;
                for (int dy = 0; dy < 14; dy++) {
                    if (gate && dy < 4) continue;
                    SultharisBuilder.set(level, x, y + dy, z, scorched);
                }
                if (Math.floorMod(x + z, 2) == 0) SultharisBuilder.set(level, x, y + 14, z, scorched); // battlements
            }
        }
        for (int[] c : new int[][]{{minX, minZ}, {maxX, minZ}, {minX, maxZ}, {maxX, maxZ}}) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    if (Math.abs(dx) != 3 && Math.abs(dz) != 3) continue;
                    for (int dy = 0; dy < 22; dy++) SultharisBuilder.set(level, c[0] + dx, y + dy, c[1] + dz, dy % 7 == 6 ? burning : scorched);
                }
            }
            SultharisBuilder.set(level, c[0], y + 22, c[1], Blocks.MAGMA_BLOCK.defaultBlockState());
            SultharisBuilder.set(level, c[0], y + 23, c[1], Blocks.FIRE.defaultBlockState());
        }
        // the arena in the middle: black stone ringed by braziers
        int r = Math.min(SultharisBuilder.half(s.sizeX()), SultharisBuilder.half(s.sizeZ())) / 2;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d <= r) SultharisBuilder.set(level, s.x() + dx, y - 1, s.z() + dz, d > r - 1.5 ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.BLACKSTONE.defaultBlockState());
            }
        }
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            SultharisBuilder.set(level, s.x() + (int) Math.round(Math.cos(a) * (r + 2)), y, s.z() + (int) Math.round(Math.sin(a) * (r + 2)),
                    SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
        }
    }

    /** An arch of runestone over the way down; its gate stays sealed until Act IV. */
    private static void cavernsEntrance(ServerLevel level, StructurePositions.Structure s, int y) {
        SultharisBuilder.pad(level, s.x() - 4, s.z() - 4, s.x() + 4, s.z() + 4, y, runestone());
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = 0; dy < 6; dy++) {
                boolean opening = Math.abs(dx) <= 1 && dy < 4;
                if (!opening) SultharisBuilder.set(level, s.x() + dx, y + dy, s.z(), runestone());
            }
        }
        SultharisBuilder.set(level, s.x() - 3, y + 6, s.z(), SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
        SultharisBuilder.set(level, s.x() + 3, y + 6, s.z(), SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
    }

    /** A walled hall with a roof, timber pillars at the corners and a door in the middle of the south wall. */
    private static void hall(ServerLevel level, int minX, int minZ, int maxX, int maxZ, int y, int height, BlockState walls, BlockState pillars) {
        SultharisBuilder.pad(level, minX, minZ, maxX, maxZ, y, SoFEBlocks.NORDRATH_DARK_PLANKS.get().defaultBlockState());
        int cx = (minX + maxX) / 2;
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                boolean edge = x == minX || x == maxX || z == minZ || z == maxZ;
                if (!edge) continue;
                boolean door = z == maxZ && Math.abs(x - cx) <= 1;
                boolean pillar = Math.floorMod(x - minX, 8) == 0 || Math.floorMod(z - minZ, 8) == 0;
                for (int dy = 0; dy < height; dy++) {
                    if (door && dy < 4) continue;
                    SultharisBuilder.set(level, x, y + dy, z, pillar ? pillars : walls);
                }
            }
        }
        for (int x = minX + 1; x < maxX; x++) {
            for (int z = minZ + 1; z < maxZ; z++) {
                SultharisBuilder.set(level, x, y + height, z, SoFEBlocks.NORDRATH_RUNESTONE_BRICK_SLAB.get().defaultBlockState());
                if (Math.floorMod(x - minX, 8) == 4 && Math.floorMod(z - minZ, 8) == 4) {
                    SultharisBuilder.set(level, x, y + height - 1, z, SoFEBlocks.NORDRATH_IRON_BRAZIER.get().defaultBlockState());
                }
            }
        }
    }

    private static void spawner(ServerLevel level, BlockPos pos, EntityType<?> type) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(type, level.getRandom());
        }
    }
}
