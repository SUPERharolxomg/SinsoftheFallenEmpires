package com.sofe.client;

import com.sofe.config.SoFEConfig;
import com.sofe.entity.npc.StoryNpcEntity;
import com.sofe.network.SyncStoryPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

/**
 * The way to the tracked quest, drawn in the world (docs/Mundo.md, W3): a trail of golden motes on the
 * ground running from the player toward the Quest Compass target, a column of light over the place, and
 * a crown of light over the NPC the player has to talk to. Only the local player sees them.
 */
public final class QuestPath {
    private static final DustParticleOptions GOLD = new DustParticleOptions(new Vector3f(0.91f, 0.71f, 0.29f), 1.1f);
    private static final int EVERY_TICKS = 3, TRAIL = 8;
    private static final double TRAIL_START = 2.0, TRAIL_STEP = 1.75;
    /** Closer than this the trail stops: the place (or the person) is in front of the player. */
    private static final double ARRIVED = 6;
    private static final double BEACON_RANGE = 160, NPC_RANGE = 48;

    private QuestPath() {
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null || mc.isPaused() || level.dimension() != Level.OVERWORLD) return;
        if (!SoFEConfig.CLIENT.showQuestPath.get() || player.tickCount % EVERY_TICKS != 0) return;
        Optional<SyncStoryPacket> story = ClientStoryData.get();
        if (story.isEmpty()) return;
        Optional<SyncStoryPacket.Target> target = story.get().corpse().or(() -> story.get().compass());
        if (target.isEmpty()) return;
        SyncStoryPacket.Target t = target.get();

        Optional<StoryNpcEntity> npc = t.npc().isEmpty() ? Optional.empty() : findNpc(level, player, t.npc());
        double tx = npc.map(StoryNpcEntity::getX).orElse(t.x() + 0.5), tz = npc.map(StoryNpcEntity::getZ).orElse(t.z() + 0.5);
        double dx = tx - player.getX(), dz = tz - player.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance > ARRIVED) trail(level, player, dx / distance, dz / distance, distance);
        if (npc.isPresent()) {
            crown(level, npc.get());
        } else if (distance < BEACON_RANGE && level.hasChunkAt(BlockPos.containing(tx, 0, tz))) {
            beacon(level, tx, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(tx), (int) Math.floor(tz)), tz);
        }
    }

    /** Motes on the floor ahead of the player, sliding forward so the eye follows them. */
    private static void trail(ClientLevel level, LocalPlayer player, double ux, double uz, double distance) {
        double phase = (player.tickCount / EVERY_TICKS % 4) / 4.0 * TRAIL_STEP;
        for (int i = 0; i < TRAIL; i++) {
            double d = TRAIL_START + i * TRAIL_STEP + phase;
            if (d > distance - 1) break;
            double x = player.getX() + ux * d, z = player.getZ() + uz * d;
            floor(level, x, player.getY(), z).ifPresent(y -> level.addParticle(GOLD, x, y + 0.15, z, 0, 0.01, 0));
        }
    }

    /** The top of the floor near a height (the trail follows stairs and slopes, and stays inside a hall). */
    private static Optional<Double> floor(ClientLevel level, double x, double feet, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int fx = (int) Math.floor(x), fz = (int) Math.floor(z), fy = (int) Math.floor(feet);
        for (int y = fy + 2; y >= fy - 4; y--) {
            pos.set(fx, y, fz);
            if (!level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                double top = level.getBlockState(pos).getCollisionShape(level, pos).max(Direction.Axis.Y);
                return level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                        ? Optional.of(y + top) : Optional.empty();
            }
        }
        return Optional.empty();
    }

    /** A column of light rising from the place, seen from afar. */
    private static void beacon(ClientLevel level, double x, int ground, double z) {
        for (int i = 0; i < 4; i++) {
            double y = ground + level.random.nextDouble() * 24;
            level.addParticle(ParticleTypes.END_ROD, x + (level.random.nextDouble() - 0.5) * 0.6, y,
                    z + (level.random.nextDouble() - 0.5) * 0.6, 0, 0.05, 0);
        }
        level.addParticle(GOLD, x, ground + 0.3, z, 0, 0, 0);
    }

    /** A turning crown of golden light over the head of the one to talk to. */
    private static void crown(ClientLevel level, StoryNpcEntity npc) {
        double y = npc.getY() + npc.getBbHeight() + 0.5;
        double turn = npc.tickCount * 0.15;
        for (int i = 0; i < 3; i++) {
            double a = turn + i * Math.PI * 2 / 3;
            level.addParticle(GOLD, npc.getX() + Math.cos(a) * 0.45, y, npc.getZ() + Math.sin(a) * 0.45, 0, 0, 0);
        }
        level.addParticle(ParticleTypes.END_ROD, npc.getX(), y + 0.35, npc.getZ(), 0, 0.02, 0);
    }

    private static Optional<StoryNpcEntity> findNpc(ClientLevel level, LocalPlayer player, String id) {
        List<StoryNpcEntity> near = level.getEntitiesOfClass(StoryNpcEntity.class, new AABB(player.blockPosition()).inflate(NPC_RANGE),
                n -> id.equals(n.npcId()));
        return near.stream().min(java.util.Comparator.comparingDouble(player::distanceToSqr));
    }
}
