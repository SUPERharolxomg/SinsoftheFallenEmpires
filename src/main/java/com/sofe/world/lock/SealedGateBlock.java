package com.sofe.world.lock;

import com.sofe.condition.ConditionManager;
import com.sofe.story.PlayerProgressView;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Optional;

/**
 * A Sealed Gate (docs/Mundo.md, W2): the door of a dungeon, vault or arena. It is always solid; a
 * player whose condition passes steps through it by using it, anyone else is told what is missing
 * ("Defeat Kaleth and Serath to open the Burning Citadel"). Which condition a gate uses comes from
 * the nearest gate in structure_positions.json.
 */
public class SealedGateBlock extends Block {
    /** A gate block belongs to the listed gate within this many blocks of it. */
    private static final int GATE_REACH = 4;

    public SealedGateBlock(Properties properties) {
        super(properties);
    }

    public static Optional<StructurePositions.Gate> gateAt(BlockPos pos) {
        return StructurePositions.get().gates().values().stream()
                .filter(g -> Math.abs(g.x() - pos.getX()) <= GATE_REACH && Math.abs(g.z() - pos.getZ()) <= GATE_REACH)
                .findFirst();
    }

    /**
     * Whether the player may pass: an operator bypassing locks, or the gate's condition passes; with gateMode =
     * pact_escort, also a player whose Pact member beside them (within 8 blocks) meets it.
     */
    public static boolean canPass(ServerPlayer player, StructurePositions.Gate gate) {
        if (LockAccess.bypasses(player) || meets(player, gate)) return true;
        if (!"pact_escort".equalsIgnoreCase(com.sofe.config.SoFEConfig.SERVER.gateMode.get())) return false;
        return com.sofe.pact.Pacts.together(player).stream()
                .anyMatch(m -> m != player && m.distanceToSqr(player) <= 64 && meets(m, gate));
    }

    private static boolean meets(ServerPlayer player, StructurePositions.Gate gate) {
        ResourceLocation id = ResourceLocation.tryParse(gate.condition());
        return id != null && ConditionManager.get(id).map(c -> c.test(PlayerProgressView.of(player))).orElse(false);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(player instanceof ServerPlayer server)) return InteractionResult.SUCCESS;
        Optional<StructurePositions.Gate> gate = gateAt(pos);
        if (gate.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.sofe.gate.unknown").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.CONSUME;
        }
        if (!canPass(server, gate.get())) {
            player.displayClientMessage(Component.translatable(gate.get().hint()).withStyle(ChatFormatting.LIGHT_PURPLE), true);
            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1f, 0.5f);
            return InteractionResult.CONSUME;
        }
        passThrough(server, pos, gate.get());
        return InteractionResult.CONSUME;
    }

    /** Steps the player to the other side of the gate, along its axis. */
    static void passThrough(ServerPlayer player, BlockPos pos, StructurePositions.Gate gate) {
        double x = player.getX(), z = player.getZ();
        if (gate.alongX()) {
            double side = Math.signum(x - (pos.getX() + 0.5));
            x = pos.getX() + 0.5 - (side == 0 ? 1 : side) * 1.6;
        } else {
            double side = Math.signum(z - (pos.getZ() + 0.5));
            z = pos.getZ() + 0.5 - (side == 0 ? 1 : side) * 1.6;
        }
        player.teleportTo(x, Math.floor(player.getY()), z);
        player.level().playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1f, 1.2f);
    }
}
