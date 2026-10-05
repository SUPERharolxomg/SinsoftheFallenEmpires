package com.sofe.world.build;

import com.sofe.registry.SoFEBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The corrupted building blocks of every empire and the intact block each becomes when its region is liberated
 * (docs/Mundo.md, W6: "the landscape heals"). Written by scripts/make_empire_blocks.py.
 */
public final class EmpireBlocks {
    private static final Map<Block, Block> HEALED = new HashMap<>();

    private EmpireBlocks() {
    }

    private static Map<Block, Block> healed() {
        if (HEALED.isEmpty()) {
        HEALED.put(SoFEBlocks.CORRUPTED_PARSIVAN_TURQUOISE_TILES.get(), SoFEBlocks.PARSIVAN_TURQUOISE_TILES.get());
        HEALED.put(SoFEBlocks.CORRUPTED_PARSIVAN_WHITE_PLASTER.get(), SoFEBlocks.PARSIVAN_WHITE_PLASTER.get());
        HEALED.put(SoFEBlocks.CORRUPTED_KHEMET_CARVED_SANDSTONE.get(), SoFEBlocks.KHEMET_CARVED_SANDSTONE.get());
        HEALED.put(SoFEBlocks.CORRUPTED_KHEMET_PAINTED_LIMESTONE.get(), SoFEBlocks.KHEMET_PAINTED_LIMESTONE.get());
        HEALED.put(SoFEBlocks.CORRUPTED_AUREUM_MARBLE_BRICKS.get(), SoFEBlocks.AUREUM_MARBLE_BRICKS.get());
        HEALED.put(SoFEBlocks.CORRUPTED_AUREUM_MARBLE_PILLAR.get(), SoFEBlocks.AUREUM_MARBLE_PILLAR.get());
        HEALED.put(SoFEBlocks.CORRUPTED_NORDRATH_RUNESTONE_BRICKS.get(), SoFEBlocks.NORDRATH_RUNESTONE_BRICKS.get());
        HEALED.put(SoFEBlocks.CORRUPTED_NORDRATH_DARK_TIMBER.get(), SoFEBlocks.NORDRATH_DARK_TIMBER.get());
        }
        return HEALED;
    }

    /** The intact state of a corrupted block (its axis kept), or empty when the block is not corrupted. */
    public static Optional<BlockState> heal(BlockState state) {
        Block intact = healed().get(state.getBlock());
        if (intact == null) return Optional.empty();
        BlockState out = intact.defaultBlockState();
        for (Property<?> property : state.getProperties()) {
            if (out.hasProperty(property)) out = copy(state, out, property);
        }
        return Optional.of(out);
    }

    private static <T extends Comparable<T>> BlockState copy(BlockState from, BlockState to, Property<T> property) {
        return to.setValue(property, from.getValue(property));
    }
}
