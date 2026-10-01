package com.sofe.world.build;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Furniture for the interiors, from MrCrayfish's Furniture Mod: Refurbished (a dependency of SoFE),
 * looked up by id so SoFE does not compile against it. Only the pieces that suit the old empires are
 * used: chairs, tables, desks, drawers, cabinets, jars, crates, cutting boards, sofas as divans and
 * stools. If a piece is missing (a renamed id in a future version), a vanilla block stands in.
 */
final class Furniture {
    static final String MOD = "refurbished_furniture";

    private Furniture() {
    }

    static BlockState of(String id, BlockState fallback) {
        Block block = ForgeRegistries.BLOCKS.getValue(ResourceLocation.fromNamespaceAndPath(MOD, id));
        return block == null || block == Blocks.AIR ? fallback : block.defaultBlockState();
    }

    /** Turns a piece to face a direction, whatever its facing property is called. */
    static BlockState facing(BlockState state, Direction direction) {
        for (Property<?> property : state.getProperties()) {
            if (property instanceof DirectionProperty dir && dir.getPossibleValues().contains(direction)
                    && (property.getName().equals("facing") || property.getName().equals("direction"))) {
                return state.setValue(dir, direction);
            }
        }
        return state;
    }

    static boolean isFurniture(BlockState state) {
        return state.getBlock().getClass().getName().startsWith("com.mrcrayfish.furniture");
    }

    /** A chair looking toward direction (its back away from it). */
    static BlockState chair(String wood, Direction looking) {
        return facing(of(wood + "_chair", Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, looking.getOpposite())), looking);
    }

    static BlockState table(String wood) {
        return of(wood + "_table", Blocks.SPRUCE_FENCE.defaultBlockState());
    }

    static BlockState desk(String wood, Direction facing) {
        return facing(of(wood + "_desk", Blocks.SPRUCE_PLANKS.defaultBlockState()), facing);
    }

    static BlockState drawer(String wood, Direction facing) {
        return facing(of(wood + "_drawer", Blocks.BARREL.defaultBlockState()), facing);
    }

    static BlockState cabinet(String wood, Direction facing) {
        return facing(of(wood + "_storage_cabinet", Blocks.BOOKSHELF.defaultBlockState()), facing);
    }

    static BlockState jar(String wood, Direction facing) {
        return facing(of(wood + "_storage_jar", Blocks.DECORATED_POT.defaultBlockState()), facing);
    }

    static BlockState crate(String wood) {
        return of(wood + "_crate", Blocks.BARREL.defaultBlockState());
    }

    static BlockState cuttingBoard(String wood, Direction facing) {
        return facing(of(wood + "_cutting_board", Blocks.SPRUCE_PRESSURE_PLATE.defaultBlockState()), facing);
    }

    /** A divan: a sofa in the color of the house, its seat looking toward direction. */
    static BlockState divan(String color, Direction looking) {
        return facing(of(color + "_sofa", Blocks.RED_WOOL.defaultBlockState()), looking);
    }

    static BlockState stool(String color) {
        return of(color + "_stool", Blocks.SPRUCE_SLAB.defaultBlockState());
    }
}
