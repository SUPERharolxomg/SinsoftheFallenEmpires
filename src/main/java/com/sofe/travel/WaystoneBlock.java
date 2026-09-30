package com.sofe.travel;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * An Aetherium Waystone (docs/Jugabilidad.md, G2): touching it activates it for that player;
 * using it opens the list of the player's Waystones to travel for free.
 */
public class WaystoneBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public WaystoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer server) {
            WaystoneService.activate(server, pos);
            WaystoneService.openList(server, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (entity instanceof ServerPlayer player) WaystoneService.activate(player, pos);
        super.stepOn(level, pos, state, entity);
    }
}
