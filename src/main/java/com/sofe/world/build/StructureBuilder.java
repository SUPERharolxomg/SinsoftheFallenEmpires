package com.sofe.world.build;

import com.sofe.SoFEMod;
import com.sofe.registry.SoFEBlocks;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.Optional;

/**
 * Builds the story places of every empire (docs/Mundo.md, W6). A structure uses its hand-made
 * template (data/sofe/structures/&lt;empire&gt;/&lt;piece&gt;.nbt, saved with Structure Blocks) when it
 * exists; until then its empire's blockout stands in, so each act is playable before the final builds.
 * A template brings its own Sealed Gate blocks; a blockout sets them in its walls with {@link #placeGates}.
 */
public final class StructureBuilder {

    private StructureBuilder() {
    }

    /** Builds one structure; returns false when it has no template and no blockout. */
    public static boolean build(ServerLevel level, StructurePositions.Structure structure) {
        String piece = structure.id().substring(structure.id().indexOf(':') + 1);
        boolean built = placeTemplate(level, structure, piece);
        if (!built) {
            if (piece.startsWith("sulthari/")) built = SultharisBuilder.blockout(level, structure, piece);
            else if (piece.startsWith("nordrath/")) built = NordrathBuilder.blockout(level, structure, piece);
            if (!built && (piece.endsWith("/crypt") || piece.endsWith("/ruin"))) built = DungeonBuilder.blockout(level, structure, piece);
            if (!built) built = EmpireBuilder.blockout(level, structure, piece);
            SultharisBuilder.settleLanterns(level); // no lantern hangs from the air
        }
        return built;
    }

    /**
     * A template (saved with /sofe export or Structure Blocks), placed with its center on the
     * structure's position and its bottom layer, the floor, on the top layer of the ground.
     */
    private static boolean placeTemplate(ServerLevel level, StructurePositions.Structure structure, String piece) {
        ResourceLocation id = SoFEMod.id(piece);
        Optional<StructureTemplate> template = level.getStructureManager().get(id);
        if (template.isEmpty() || template.get().getSize().getX() == 0) return false;
        var size = template.get().getSize();
        int y = SultharisBuilder.surfaceY(level, structure.x(), structure.z()) - 1; // a template holds its own floor layer
        BlockPos corner = new BlockPos(structure.x() - size.getX() / 2, y, structure.z() - size.getZ() / 2);
        template.get().placeInWorld(level, corner, corner, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
        SoFEMod.LOGGER.info("Placed template {} at {}", id, corner);
        return true;
    }

    /** Every gate whose center lies in this structure's footprint, standing on the floor at height y. */
    static void placeGates(ServerLevel level, StructurePositions.Structure structure, int y) {
        int halfX = structure.sizeX() / 2, halfZ = structure.sizeZ() / 2;
        for (StructurePositions.Gate gate : StructurePositions.get().gates().values()) {
            if (Math.abs(gate.x() - structure.x()) > halfX || Math.abs(gate.z() - structure.z()) > halfZ) continue;
            placeGate(level, gate, y);
        }
    }

    /** An opening 3 wide and 4 high filled with the gate. */
    public static void placeGate(ServerLevel level, StructurePositions.Gate gate, int y) {
        var state = SoFEBlocks.SEALED_GATE.get().defaultBlockState();
        for (int i = -1; i <= 1; i++) {
            for (int dy = 0; dy < 4; dy++) {
                int x = gate.alongX() ? gate.x() : gate.x() + i;
                int z = gate.alongX() ? gate.z() + i : gate.z();
                SultharisBuilder.set(level, x, y + dy, z, state);
            }
        }
    }
}
