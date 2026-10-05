package com.sofe.entity.boss;

import com.sofe.entity.projectile.SpellBolt;
import com.sofe.gear.ranged.Spell;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * What the bosses of Acts III and IV share: bolts of magic, minions with a cap, and blocks they change only for a
 * while (webs, pits, devoured floor) that always come back, so a fight never leaves an arena broken.
 */
public final class BossKit {
    public static final String MINION_OF = "sofe_minion_of";

    private BossKit() {
    }

    /** A bolt from the boss's eyes at a target. */
    public static void bolt(ServerLevel level, LivingEntity boss, LivingEntity target, Spell spell, float damage, float speed, float spread) {
        Vec3 from = boss.getEyePosition();
        Vec3 aim = target.getEyePosition().subtract(from);
        SpellBolt shot = new SpellBolt(level, boss, spell, damage, false);
        shot.setPos(from.x, from.y, from.z);
        shot.shoot(aim.x, aim.y, aim.z, speed, spread);
        level.addFreshEntity(shot);
    }

    /** Minions of this boss round it, never more than the cap alive at once; they hunt the nearest fighter. */
    public static <T extends Mob> List<T> minions(ServerLevel level, LivingEntity boss, EntityType<T> type, int count, int cap, double radius,
                                                  List<ServerPlayer> fighters) {
        List<T> out = new ArrayList<>();
        long alive = level.getEntitiesOfClass(Mob.class, boss.getBoundingBox().inflate(48),
                m -> m.isAlive() && boss.getStringUUID().equals(m.getPersistentData().getString(MINION_OF))).size();
        for (int i = 0; i < count && alive + out.size() < cap; i++) {
            double a = boss.getRandom().nextDouble() * Math.PI * 2;
            BlockPos at = BlockPos.containing(boss.getX() + Math.cos(a) * radius, boss.getY(), boss.getZ() + Math.sin(a) * radius);
            T mob = type.spawn(level, at, MobSpawnType.MOB_SUMMONED);
            if (mob == null) continue;
            mob.getPersistentData().putString(MINION_OF, boss.getStringUUID());
            mob.getPersistentData().putBoolean(com.sofe.mob.EliteMobs.CHECKED, true);
            if (!fighters.isEmpty()) mob.setTarget(fighters.get(boss.getRandom().nextInt(fighters.size())));
            out.add(mob);
        }
        return out;
    }

    /** Removes a boss's minions (after the fight). */
    public static void dismissMinions(ServerLevel level, LivingEntity boss) {
        level.getEntitiesOfClass(Mob.class, boss.getBoundingBox().inflate(64),
                m -> boss.getStringUUID().equals(m.getPersistentData().getString(MINION_OF))).forEach(Mob::discard);
    }

    // --- blocks changed for a while

    private record Temporary(ServerLevel level, BlockPos pos, BlockState original, long until, String owner) {
    }

    private static final List<Temporary> TEMPORARY = new ArrayList<>();

    /** Puts a block for some ticks; the one that was there comes back afterwards (or when the boss's fight ends). */
    public static void temporary(ServerLevel level, BlockPos pos, BlockState state, int ticks, LivingEntity owner) {
        BlockState original = level.getBlockState(pos);
        for (Temporary t : TEMPORARY) if (t.level() == level && t.pos().equals(pos)) return; // already changed: keep the true original
        if (original.hasBlockEntity() || original.getDestroySpeed(level, pos) < 0) return; // never chests, gates or bedrock
        TEMPORARY.add(new Temporary(level, pos.immutable(), original, level.getGameTime() + ticks, owner.getStringUUID()));
        level.setBlock(pos, state, 3);
    }

    /** Puts back every block a boss changed. */
    public static void restore(LivingEntity owner) {
        for (Iterator<Temporary> it = TEMPORARY.iterator(); it.hasNext(); ) {
            Temporary t = it.next();
            if (!t.owner().equals(owner.getStringUUID())) continue;
            t.level().setBlock(t.pos(), t.original(), 3);
            it.remove();
        }
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || TEMPORARY.isEmpty()) return;
        for (Iterator<Temporary> it = TEMPORARY.iterator(); it.hasNext(); ) {
            Temporary t = it.next();
            if (t.level().getGameTime() < t.until()) continue;
            t.level().setBlock(t.pos(), t.original(), 3);
            it.remove();
        }
    }

    /** How many blocks are changed for a while now (for tests). */
    public static int temporaryCount() {
        return TEMPORARY.size();
    }
}
