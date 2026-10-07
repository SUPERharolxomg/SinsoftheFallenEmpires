package com.sofe.quest;

import com.sofe.story.StoryCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The Void's attack on the lower district of Sulthari (Act I, "drive the Void creatures out of the lower district"):
 * the creatures come out of the cracks in the district itself, where the Quest Compass points, once the Bearer gets
 * there, never more than four at a time, and keep coming until the step's count is reached. Before, they rose round
 * the Bearer wherever the step began (often the plaza), so the district was empty; and fewer came than the step
 * asked for, while monsters never spawn by themselves in a city, so the step could not be finished.
 */
public final class VoidInvasion {
    /** How close to the district the Bearer must come, how far round it the creatures come out, and how many at once. */
    static final int REACH = 40, MIN_RADIUS = 5, MAX_RADIUS = 14, AT_ONCE = 4;
    private static final String WRETCH = "sofe:void_wretch", STALKER = "sofe:void_stalker";

    private VoidInvasion() {
    }

    /** The tag of the creatures that came for this Bearer. */
    static String tag(ServerPlayer player) {
        return "sofe_invasion_" + player.getUUID();
    }

    /** Every few seconds (QuestEngine.onPlayerTick): calls up the creatures still to come for the Bearer near the district. */
    public static void tick(ServerPlayer player) {
        var story = StoryCapability.get(player).orElse(null);
        if (story == null) return;
        var state = story.quest(QuestEngine.FIRST_QUEST).orElse(null);
        if (state == null || state.completed()) return;
        var step = StoryDataManager.quest(QuestEngine.FIRST_QUEST).flatMap(q -> q.step(state.step())).orElse(null);
        if (step == null || step.target() == null || !(step.objective() instanceof Objective.Kill kill) || !kill.entity().startsWith("sofe:void")) return;
        int tx = step.target().x(), tz = step.target().z();
        double dx = player.getX() - tx, dz = player.getZ() - tz;
        if (dx * dx + dz * dz > (double) REACH * REACH) return;
        int remaining = kill.count() - state.count();
        if (remaining <= 0) return;
        ServerLevel level = player.serverLevel();
        String tag = tag(player);
        AABB district = new AABB(tx - REACH * 2, level.getMinBuildHeight(), tz - REACH * 2, tx + REACH * 2, level.getMaxBuildHeight(), tz + REACH * 2);
        int alive = level.getEntitiesOfClass(Mob.class, district, m -> m.isAlive() && m.getTags().contains(tag)).size();
        int come = Math.min(AT_ONCE, remaining) - alive;
        for (int i = 0; i < come; i++) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(level.random.nextInt(3) == 0 ? STALKER : WRETCH));
            if (type == null) return;
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = MIN_RADIUS + level.random.nextDouble() * (MAX_RADIUS - MIN_RADIUS);
            int x = (int) Math.floor(tx + Math.cos(angle) * distance), z = (int) Math.floor(tz + Math.sin(angle) * distance);
            BlockPos pos = com.sofe.world.Grounding.groundFloor(level, x, z);
            if (!(type.spawn(level, pos, MobSpawnType.EVENT) instanceof Mob mob)) continue;
            mob.setPersistenceRequired(); // they stay until driven out, even when the Bearer steps away
            mob.addTag(tag);
            mob.setTarget(player);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, mob.getX(), mob.getY() + 1, mob.getZ(), 40, 0.4, 0.9, 0.4, 0.05);
            level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8f, 0.6f);
        }
    }
}
