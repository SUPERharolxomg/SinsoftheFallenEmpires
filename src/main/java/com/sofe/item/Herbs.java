package com.sofe.item;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * The herbs of alchemy (docs/Pociones.md, "Ingredients = Minecraft farming"; docs/Mundo.md, W4):
 * a wild plant in its region that gives its fruit and seeds, and a crop that grows on farmland
 * like wheat, so the Bearer's Homestead can become an herb garden.
 */
public final class Herbs {

    private Herbs() {
    }

    /** The planted herb: 8 growth stages on farmland, its own seeds. */
    public static class Crop extends CropBlock {
        private final Supplier<? extends Item> seeds;

        public Crop(Supplier<? extends Item> seeds, Properties properties) {
            super(properties);
            this.seeds = seeds;
        }

        @Override
        protected ItemLike getBaseSeedId() {
            return seeds.get();
        }
    }

    /** The herb as it grows in the wild: on grass, dirt or sand, never planted by the player. */
    public static class Wild extends BushBlock {
        private static final VoxelShape SHAPE = BushBlock.box(2, 0, 2, 14, 13, 14);

        public Wild(Properties properties) {
            super(properties);
        }

        @Override
        protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
            return state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || super.mayPlaceOn(state, level, pos);
        }

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
            return SHAPE;
        }
    }
}
