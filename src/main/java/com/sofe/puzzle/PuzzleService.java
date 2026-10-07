package com.sofe.puzzle;

import com.sofe.SoFEMod;
import com.sofe.quest.QuestEngine;
import com.sofe.quest.QuestEvent;
import com.sofe.quest.StoryDataManager;
import com.sofe.registry.SoFEBlocks;
import com.sofe.story.StoryCapability;
import com.sofe.world.Grounding;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The rune puzzles in the world (docs/Mundo.md, W6): puts them down, presses their runes, reads their riddles, credits
 * the Bearers who solve them and tells a Sealed Gate whether a Bearer has.
 */
public final class PuzzleService {
    /** Every Bearer this close when a puzzle is solved shares the credit (a Pact solves it together). */
    private static final double SHARE = 24;
    /** A solved puzzle burns this long, then goes dark for the next Bearer. */
    private static final long RESET_TICKS = 20 * 8;
    private static final int ROW_OUT = 4, TABLET_OUT = 2;

    private PuzzleService() {
    }

    // --- putting a puzzle in the world

    /** Places a puzzle's stones and tablet on the ground; false when its anchor is not in the layout. */
    public static boolean place(ServerLevel level, PuzzleDefinition puzzle, StructurePositions.Layout layout) {
        Optional<int[]> origin = origin(puzzle, layout);
        if (origin.isEmpty()) {
            SoFEMod.LOGGER.warn("Puzzle {}: its anchor {} is not in structure_positions.json", puzzle.id(), puzzle.anchor());
            return false;
        }
        int ax = origin.get()[0], az = origin.get()[1];
        Direction out = Direction.from2DDataValue(origin.get()[2]);
        Direction right = out.getOpposite().getClockWise(); // as the Bearer faces the row, coming from outside
        int n = puzzle.runes().size();
        int cx = ax + out.getStepX() * ROW_OUT, cz = az + out.getStepZ() * ROW_OUT;
        PuzzleLogic.State state = PuzzleLogic.start(puzzle);
        List<BlockPos> stones = new ArrayList<>();
        for (int k = 0; k < n; k++) {
            int offset = k * 2 - (n - 1);
            BlockPos at = Grounding.groundFloor(level, cx + right.getStepX() * offset, cz + right.getStepZ() * offset);
            level.setBlock(at, stone(puzzle, k, state.lit[k]), Block.UPDATE_ALL);
            stones.add(at);
        }
        BlockPos tablet = Grounding.groundFloor(level, cx + out.getStepX() * TABLET_OUT, cz + out.getStepZ() * TABLET_OUT);
        level.setBlock(tablet, SoFEBlocks.RIDDLE_TABLET.get().defaultBlockState(), Block.UPDATE_ALL);
        PuzzleData.get(level.getServer()).put(new PuzzleData.Placed(puzzle.id(), stones, tablet, state, 0));
        return true;
    }

    /** Where the row goes from (x, z) and which way is out (a 2D direction value). */
    public static Optional<int[]> origin(PuzzleDefinition puzzle, StructurePositions.Layout layout) {
        if (puzzle.anchor() == null) {
            return Optional.of(new int[]{puzzle.x(), puzzle.z(), Direction.byName(puzzle.facing()).get2DDataValue()});
        }
        BlockPos w = layout.waystones().get(puzzle.anchor());
        if (w == null) return Optional.empty();
        // out: from the dungeon's middle toward its Waystone, which stands outside its door
        Direction out = layout.structure(puzzle.anchor()).map(st -> {
            int dx = w.getX() - st.x(), dz = w.getZ() - st.z();
            return Math.abs(dx) > Math.abs(dz) ? (dx > 0 ? Direction.EAST : Direction.WEST) : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
        }).orElse(Direction.SOUTH);
        return Optional.of(new int[]{w.getX(), w.getZ(), out.get2DDataValue()});
    }

    private static net.minecraft.world.level.block.state.BlockState stone(PuzzleDefinition puzzle, int k, boolean lit) {
        return SoFEBlocks.RUNE_STONE.get().defaultBlockState()
                .setValue(RuneStoneBlock.RUNE, puzzle.runes().get(k).ordinal()).setValue(RuneStoneBlock.LIT, lit);
    }

    // --- playing it

    public static void press(ServerLevel level, BlockPos pos, ServerPlayer player) {
        PuzzleData data = PuzzleData.get(level.getServer());
        Optional<PuzzleData.Placed> placed = data.at(pos);
        Optional<PuzzleDefinition> puzzle = placed.map(p -> StoryDataManager.puzzles().get(p.id));
        if (placed.isEmpty() || puzzle.isEmpty()) return;
        PuzzleData.Placed p = placed.get();
        int k = p.stones.indexOf(pos);
        PuzzleDefinition.Rune rune = puzzle.get().runes().get(k);
        player.displayClientMessage(Component.translatable("rune.sofe." + rune.id()).withStyle(ChatFormatting.GOLD), true);
        if (p.resetAt > 0) return; // solved: it burns a moment for everyone to see, then waits for the next Bearer
        PuzzleLogic.Result result = PuzzleLogic.press(puzzle.get(), p.state, k);
        show(level, p, puzzle.get());
        data.setDirty();
        switch (result) {
            case LIT -> level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1f, 0.8f + 0.1f * p.state.progress);
            case WRONG -> {
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1f, 0.5f);
                player.displayClientMessage(Component.translatable("message.sofe.puzzle.wrong").withStyle(ChatFormatting.RED), true);
            }
            case SOLVED -> solved(level, p, puzzle.get(), player);
            default -> {
            }
        }
    }

    private static void solved(ServerLevel level, PuzzleData.Placed p, PuzzleDefinition puzzle, ServerPlayer presser) {
        p.resetAt = level.getGameTime() + RESET_TICKS;
        BlockPos at = p.stones.get(p.stones.size() / 2);
        level.playSound(null, at, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1.2f);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, at.getX() + 0.5, at.getY() + 1.5, at.getZ() + 0.5, 40, 2, 0.5, 2, 0.05);
        List<ServerPlayer> near = new ArrayList<>(level.getPlayers(pl -> pl.distanceToSqr(presser) <= SHARE * SHARE));
        if (!near.contains(presser)) near.add(presser);
        for (ServerPlayer player : near) credit(player, puzzle);
    }

    /** The puzzle is this Bearer's: the gate opens for them, and a quest that asked for it moves on. */
    public static void credit(ServerPlayer player, PuzzleDefinition puzzle) {
        StoryCapability.get(player).ifPresent(story -> {
            if (story.solvePuzzle(puzzle.id())) {
                player.sendSystemMessage(Component.translatable(puzzle.gate() != null ? "message.sofe.puzzle.solved_gate" : "message.sofe.puzzle.solved")
                        .withStyle(ChatFormatting.GOLD));
            }
        });
        QuestEngine.event(player, new QuestEvent.PuzzleSolved(puzzle.id()));
        QuestEngine.sync(player);
    }

    private static void show(ServerLevel level, PuzzleData.Placed p, PuzzleDefinition puzzle) {
        for (int k = 0; k < p.stones.size(); k++) {
            BlockPos s = p.stones.get(k);
            if (level.getBlockState(s).getBlock() instanceof RuneStoneBlock) level.setBlock(s, stone(puzzle, k, p.state.lit[k]), Block.UPDATE_ALL);
        }
    }

    public static void read(ServerLevel level, BlockPos pos, ServerPlayer player) {
        PuzzleData.get(level.getServer()).at(pos).map(p -> StoryDataManager.puzzles().get(p.id)).ifPresent(puzzle -> {
            player.sendSystemMessage(Component.translatable(puzzle.translationKey()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            for (int i = 1; i <= puzzle.riddleLines(); i++) {
                player.sendSystemMessage(Component.translatable(puzzle.translationKey() + ".riddle." + i).withStyle(ChatFormatting.ITALIC));
            }
            player.sendSystemMessage(Component.translatable(puzzle.kind() == PuzzleDefinition.Kind.ORDER
                    ? "message.sofe.puzzle.how_order" : "message.sofe.puzzle.how_lights").withStyle(ChatFormatting.GRAY));
            level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1f, 0.9f);
        });
    }

    /** Every second: a solved puzzle whose moment has passed goes back to its start, ready for the next Bearer. */
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 20 != 0) return;
        ServerLevel level = event.getServer().overworld();
        PuzzleData data = PuzzleData.get(event.getServer());
        for (PuzzleData.Placed p : data.all()) {
            if (p.resetAt == 0 || level.getGameTime() < p.resetAt) continue;
            PuzzleDefinition puzzle = StoryDataManager.puzzles().get(p.id);
            if (puzzle == null) continue;
            p.resetAt = 0;
            p.state = PuzzleLogic.start(puzzle);
            if (level.isLoaded(p.tablet)) show(level, p, puzzle);
            data.setDirty();
        }
    }

    // --- the gates

    /** The puzzle that seals a gate (by its key in structure_positions.json), if one does. */
    public static Optional<PuzzleDefinition> forGate(String gate) {
        return StoryDataManager.puzzles().values().stream().filter(p -> gate.equals(p.gate())).findFirst();
    }

    /** Whether a gate's puzzle lets this Bearer through: solved, or its boss already beaten, or no puzzle at all. */
    public static boolean opens(ServerPlayer player, String gate) {
        Optional<PuzzleDefinition> puzzle = forGate(gate);
        if (puzzle.isEmpty()) return true;
        return StoryCapability.get(player).map(s -> s.hasSolved(puzzle.get().id())
                || puzzle.get().boss() != null && s.hasDefeated(puzzle.get().boss())).orElse(false);
    }
}
