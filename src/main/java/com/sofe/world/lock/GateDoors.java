package com.sofe.world.lock;

import com.sofe.registry.SoFEBlocks;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Sealed Gate opens before a Bearer it lets through (its runes solved, its condition met): its blocks fade away while
 * they are near, and the seal closes again behind them once nobody it lets through is near. Before, a gate only let a
 * Bearer through when they used it (right-click), and a Bearer who had solved the runes saw a gate that stayed shut.
 * A Bearer it does not let through is still told what is missing when they use it (SealedGateBlock).
 */
public final class GateDoors {
    /** How near a Bearer must come for the gate to open, how often the gates are looked at, and how long one stays open after. */
    static final int NEAR = 6, EVERY = 10, LINGER = 60;

    /** An open gate: where its blocks stood, and until when it stays open. */
    private static final class Open {
        final List<BlockPos> blocks;
        long until;

        Open(List<BlockPos> blocks, long until) {
            this.blocks = blocks;
            this.until = until;
        }
    }

    private static final Map<String, Open> OPEN = new HashMap<>();

    private GateDoors() {
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % EVERY != 0) return;
        ServerLevel level = event.getServer().overworld();
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) continue;
            for (StructurePositions.Gate gate : StructurePositions.get().gates().values()) {
                double dx = player.getX() - gate.x(), dz = player.getZ() - gate.z();
                if (dx * dx + dz * dz > NEAR * NEAR || !SealedGateBlock.canPass(player, gate)) continue;
                Open open = OPEN.get(gate.id());
                if (open != null) {
                    open.until = now + LINGER;
                    continue;
                }
                List<BlockPos> blocks = blocks(level, gate, player.getBlockY());
                if (blocks.isEmpty()) continue;
                for (BlockPos pos : blocks) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.02);
                }
                level.playSound(null, blocks.get(0), SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1.3f);
                level.playSound(null, blocks.get(0), SoundEvents.IRON_DOOR_OPEN, SoundSource.BLOCKS, 1f, 0.6f);
                player.displayClientMessage(Component.translatable("message.sofe.gate.opens").withStyle(ChatFormatting.LIGHT_PURPLE), true);
                OPEN.put(gate.id(), new Open(blocks, now + LINGER));
            }
        }
        OPEN.entrySet().removeIf(e -> {
            if (now < e.getValue().until) return false;
            for (BlockPos pos : e.getValue().blocks) {
                if (level.isLoaded(pos) && level.getBlockState(pos).isAir()) level.setBlock(pos, SoFEBlocks.SEALED_GATE.get().defaultBlockState(), Block.UPDATE_ALL);
            }
            if (!e.getValue().blocks.isEmpty()) level.playSound(null, e.getValue().blocks.get(0), SoundEvents.IRON_DOOR_CLOSE, SoundSource.BLOCKS, 1f, 0.6f);
            return true;
        });
    }

    /** The gate's blocks round its place, near this height. */
    static List<BlockPos> blocks(ServerLevel level, StructurePositions.Gate gate, int y) {
        List<BlockPos> found = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -6; dy <= 6; dy++) {
                    BlockPos pos = new BlockPos(gate.x() + dx, y + dy, gate.z() + dz);
                    if (level.getBlockState(pos).is(SoFEBlocks.SEALED_GATE.get())) found.add(pos);
                }
            }
        }
        return found;
    }

    /** The server stops: every open gate closes, so none is left open in the saved world. */
    public static void onServerStopping(net.minecraftforge.event.server.ServerStoppingEvent event) {
        ServerLevel level = event.getServer().overworld();
        for (Open open : OPEN.values()) {
            for (BlockPos pos : open.blocks) if (level.getBlockState(pos).isAir()) level.setBlock(pos, SoFEBlocks.SEALED_GATE.get().defaultBlockState(), Block.UPDATE_ALL);
        }
        OPEN.clear();
    }

    /** For GameTests: whether this gate stands open now. */
    public static boolean isOpen(String gate) {
        return OPEN.containsKey(gate);
    }
}
