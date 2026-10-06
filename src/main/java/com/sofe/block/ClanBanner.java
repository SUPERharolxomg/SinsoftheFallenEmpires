package com.sofe.block;

import com.sofe.registry.SoFEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A great banner: a bar against a wall, from which a cloth three blocks wide and six long hangs and
 * moves in the wind (drawn by ClanBannerRenderer). FACING is the side the cloth faces, away from the
 * wall it hangs on. Vanilla banners are one block; the concept art of Skarnhold has banners as tall
 * as a tower. The clans of Nordrath have theirs, and so do Parsivan, Khemet and Aureum, each cloth
 * with the empire's emblem (textures/entity/&lt;cloth&gt;.png, scripts/make_clan_banner.py and
 * scripts/make_empire_banners.py).
 */
public class ClanBanner extends HorizontalDirectionalBlock implements EntityBlock {
    public static final int WIDTH = 3, LENGTH = 6;
    private final net.minecraft.resources.ResourceLocation cloth;
    private static final VoxelShape BAR_NS = Block.box(-8, 12, 0, 24, 16, 4);
    private static final VoxelShape BAR_EW = Block.box(0, 12, -8, 4, 16, 24);

    public ClanBanner(Properties properties) {
        this(properties, "clan_banner");
    }

    public ClanBanner(Properties properties, String cloth) {
        super(properties);
        this.cloth = com.sofe.SoFEMod.id("textures/entity/" + cloth + ".png");
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    /** The texture of the cloth. */
    public net.minecraft.resources.ResourceLocation cloth() {
        return cloth;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? BAR_NS : BAR_EW;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL; // the bar is a model, the cloth is drawn by the renderer
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new Entity(pos, state);
    }

    public static BlockEntityType<Entity> type() {
        return BlockEntityType.Builder.of(Entity::new, SoFEBlocks.CLAN_BANNER.get(), SoFEBlocks.PARSIVAN_BANNER.get(),
                SoFEBlocks.KHEMET_BANNER.get(), SoFEBlocks.AUREUM_BANNER.get()).build(null);
    }

    public static class Entity extends BlockEntity {
        public Entity(BlockPos pos, BlockState state) {
            super(SoFEBlocks.CLAN_BANNER_ENTITY.get(), pos, state);
        }

        @Override
        public AABB getRenderBoundingBox() {
            BlockPos p = getBlockPos();
            return new AABB(p.getX() - 2, p.getY() - LENGTH, p.getZ() - 2, p.getX() + 3, p.getY() + 1, p.getZ() + 3);
        }
    }
}
