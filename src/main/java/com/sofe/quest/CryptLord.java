package com.sofe.quest;

import com.sofe.story.StoryCapability;
import com.sofe.story.StoryProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The lord of a dungeon's depths (a step's "lair", QuestDefinition.Lair): a Crypt is gone down through, hall after hall
 * of its dead, and in its deepest chamber its lord rises when the Bearer gets there, an elite with its own name. Only
 * this one counts for the step ("lord:&lt;entity&gt;", QuestEngine.onKill), never one of its kind met on the way.
 */
public final class CryptLord {
    /** The tag of a lord, and how close to its lair (across) the Bearer must come. */
    public static final String TAG = "sofe_lord";
    static final int NEAR = 14;

    private CryptLord() {
    }

    /** Every second (QuestEngine.onPlayerTick): wakes the lord of a lair step when the Bearer is in its depths. */
    public static void wake(ServerPlayer player) {
        StoryProgress story = StoryCapability.get(player).orElse(null);
        if (story == null) return;
        story.quests().forEach((id, state) -> {
            if (state.completed()) return;
            StoryDataManager.quest(id).flatMap(q -> q.step(state.step())).ifPresent(step -> {
                if (step.lair() != null && step.objective() instanceof Objective.Kill kill) rise(player, step.lair(), kill.entity().substring(Objective.LORD.length()));
                if (step.haunt() != null && step.objective() instanceof Objective.Kill kill) haunt(player, step.haunt(), kill.count() - state.count());
            });
        });
    }

    /** The tag of the dead a dungeon raises for a Bearer, and how many of them stand at once. */
    public static final String RISEN = "sofe_risen";
    static final int AT_ONCE = 3;

    /** Whether the Bearer is inside a haunted dungeon: near its middle across, and under its ground. */
    static boolean inside(ServerPlayer player, QuestDefinition.Haunt haunt) {
        double dx = player.getX() - haunt.x(), dz = player.getZ() - haunt.z();
        if (dx * dx + dz * dz > (double) haunt.reach() * haunt.reach()) return false;
        return underground(player, haunt.x(), haunt.z(), haunt.reach(), haunt.depth());
    }

    /**
     * Whether the Bearer is this deep in a dungeon: under a roof, and this far below the highest ground round its place
     * (its hill, its court). Measured against the ground right over a chamber, a crypt by the sea or a lake (a low shore
     * over its deepest chamber) never counted its Bearer as deep enough, and its lord never rose.
     */
    static boolean underground(ServerPlayer player, int x, int z, int reach, int depth) {
        ServerLevel level = player.serverLevel();
        if (level.canSeeSky(player.blockPosition())) return false;
        int highest = level.getMinBuildHeight();
        int step = Math.max(4, reach / 2);
        for (int dx = -reach; dx <= reach; dx += step) {
            for (int dz = -reach; dz <= reach; dz += step) {
                highest = Math.max(highest, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x + dx, z + dz));
            }
        }
        return player.getY() <= highest - depth;
    }

    /**
     * The dead the step still asks for rise near the Bearer inside the dungeon, never more than a few at once, out of
     * sight a few blocks off; each counts for the Bearer whoever strikes it (QuestEngine.onKill).
     */
    static void haunt(ServerPlayer player, QuestDefinition.Haunt haunt, int remaining) {
        if (remaining <= 0 || !inside(player, haunt)) return;
        ServerLevel level = player.serverLevel();
        String mine = VoidInvasion.forTag(player.getUUID());
        int alive = level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(40), m -> m.isAlive() && m.getTags().contains(RISEN) && m.getTags().contains(mine)).size();
        int come = Math.min(AT_ONCE, remaining) - alive;
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(haunt.entity()));
        if (type == null) return;
        for (int i = 0; i < come; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * (5 + level.random.nextInt(4)));
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * (5 + level.random.nextInt(4)));
            BlockPos near = com.sofe.world.Grounding.near(level, x, player.getBlockY(), z, 4);
            BlockPos at = com.sofe.world.Grounding.roomFor(level, type, near, 4);
            if (!com.sofe.world.Grounding.fits(level, type, at) || Math.abs(at.getY() - player.getBlockY()) > 6) continue; // never through a floor
            if (!(type.spawn(level, at, MobSpawnType.EVENT) instanceof Mob dead)) continue;
            dead.addTag(RISEN);
            dead.addTag(mine);
            dead.setPersistenceRequired();
            dead.setTarget(player);
            level.sendParticles(ParticleTypes.SOUL, dead.getX(), dead.getY() + 1, dead.getZ(), 20, 0.4, 0.8, 0.4, 0.02);
            level.playSound(null, at, SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.HOSTILE, 0.6f, 0.6f);
        }
    }

    /** Whether the Bearer is in the lair: near it across, and deep enough under the ground above it. */
    static boolean inLair(ServerPlayer player, QuestDefinition.Lair lair) {
        double dx = player.getX() - lair.x(), dz = player.getZ() - lair.z();
        if (dx * dx + dz * dz > NEAR * NEAR) return false;
        return underground(player, lair.x(), lair.z(), NEAR, lair.depth());
    }

    private static void rise(ServerPlayer player, QuestDefinition.Lair lair, String entity) {
        if (!inLair(player, lair)) return;
        ServerLevel level = player.serverLevel();
        String mine = VoidInvasion.forTag(player.getUUID());
        if (!level.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(48), m -> m.isAlive() && m.getTags().contains(TAG) && m.getTags().contains(mine)).isEmpty()) return;
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(entity));
        if (type == null) return;
        BlockPos at = com.sofe.world.Grounding.near(level, lair.x(), player.getBlockY(), lair.z(), 6);
        if (!(type.spawn(level, at, MobSpawnType.EVENT) instanceof Mob lord)) return;
        com.sofe.mob.EliteMobs.make(lord, StoryCapability.get(player).map(StoryProgress::act).orElse(1), level.random);
        lord.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(lord.getMaxHealth() * 2);
        lord.setHealth(lord.getMaxHealth());
        lord.addTag(TAG);
        lord.addTag(mine);
        lord.setPersistenceRequired();
        if (lair.name() != null) {
            lord.setCustomName(Component.translatable(lair.name()).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
            lord.setCustomNameVisible(true);
        }
        lord.setTarget(player);
        level.sendParticles(ParticleTypes.SOUL, lord.getX(), lord.getY() + 1, lord.getZ(), 60, 0.6, 1.2, 0.6, 0.05);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, lord.getX(), lord.getY() + 0.5, lord.getZ(), 30, 0.8, 0.5, 0.8, 0.02);
        level.playSound(null, at, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.7f, 0.7f);
        player.displayClientMessage(Component.translatable("message.sofe.lord_awakens", lord.getDisplayName()).withStyle(ChatFormatting.RED), true);
    }
}
