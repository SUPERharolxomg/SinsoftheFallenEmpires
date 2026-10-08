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
        switch (piece) {
            case "nordrath/city" -> {
                NordrathCity.build(level, s);
                return true;
            }
            case "nordrath/forge", "nordrath/arena", "nordrath/burning_citadel" -> {
                return BossKeeps.build(level, s, piece); // halls cut into mountains, their gates set at their own floor
            }
            case "nordrath/caverns_entrance" -> {
                DeepHalls.cavernsEntrance(level, s); // a crag of ice and basalt over the way down (the user's concept)
                return true;
            }
            case "nordrath/forge_boss_room", "nordrath/arena_floor", "nordrath/citadel_arena" -> {
                return true; // zones only, inside the buildings above
            }
            default -> {
                return false;
            }
        }
    }

    private static BlockState runestone() {
        return SoFEBlocks.NORDRATH_RUNESTONE_BRICKS.get().defaultBlockState();
    }

}
