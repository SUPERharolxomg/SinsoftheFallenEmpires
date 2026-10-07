package com.sofe.puzzle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/** A Rune Stone of a puzzle (data/sofe/puzzles): one of six runes carved in it, dark or burning. Pressed by using it. */
public class RuneStoneBlock extends Block {
    public static final IntegerProperty RUNE = IntegerProperty.create("rune", 0, PuzzleDefinition.Rune.values().length - 1);
    public static final BooleanProperty LIT = BooleanProperty.create("lit");

    public RuneStoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(RUNE, 0).setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RUNE, LIT);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level instanceof ServerLevel server && player instanceof ServerPlayer sp) PuzzleService.press(server, pos, sp);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
