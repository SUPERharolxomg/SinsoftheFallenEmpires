package com.sofe.crafting;

import com.sofe.network.StationPackets;
import com.sofe.network.SoFENetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** The Imperial Forge and the Alembic: using one opens its recipe list. */
public class StationBlock extends Block {
    private final StationRecipe.Kind kind;

    public StationBlock(StationRecipe.Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public StationRecipe.Kind kind() {
        return kind;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer server) {
            StationService.opened(server, pos, kind);
            SoFENetwork.sendTo(server, new StationPackets.Open(kind));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
