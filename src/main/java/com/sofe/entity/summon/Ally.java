package com.sofe.entity.summon;

import com.sofe.skill.SkillTargeting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;
import java.util.UUID;

/**
 * A Bearer's summon (the Clay Wardens, the Janissary Guard, the risen): it belongs to a player, fights what
 * they fight, can be given a target by the King's Command, and never harms a Bearer. The goals here are
 * shared by every kind of summon.
 */
public interface Ally {
    UUID owner();

    /** The King's Command: attack this target for a while. */
    void command(LivingEntity target, int ticks);

    /** The commanded target while the command lasts, else null. */
    LivingEntity commanded();

    default ServerPlayer ownerPlayer(PathfinderMob self) {
        return owner() != null && self.level() instanceof ServerLevel level && level.getPlayerByUUID(owner()) instanceof ServerPlayer p ? p : null;
    }

    /** Keeps close to the owner when there is nothing to fight; catches up by teleport when left far behind. */
    class FollowOwner<T extends PathfinderMob & Ally> extends Goal {
        private final T ally;

        public FollowOwner(T ally) {
            this.ally = ally;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            ServerPlayer o = ally.ownerPlayer(ally);
            return o != null && ally.getTarget() == null && ally.distanceToSqr(o) > 25;
        }

        @Override
        public void tick() {
            ServerPlayer o = ally.ownerPlayer(ally);
            if (o == null) return;
            if (ally.distanceToSqr(o) > 400) ally.teleportTo(o.getX(), o.getY(), o.getZ());
            else ally.getNavigation().moveTo(o, 1.2);
        }
    }

    /** Whatever the owner fights, or what fights the owner, or what the King commanded. */
    final class OwnersFoe<T extends PathfinderMob & Ally> extends TargetGoal {
        private final T ally;
        private LivingEntity foe;

        public OwnersFoe(T ally) {
            super(ally, false);
            this.ally = ally;
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            ServerPlayer o = ally.ownerPlayer(ally);
            if (o == null) return false;
            LivingEntity c = ally.commanded();
            if (c != null) {
                foe = c;
                return true;
            }
            LivingEntity hit = o.getLastHurtMob();
            if (hit != null && o.tickCount - o.getLastHurtMobTimestamp() < 200 && SkillTargeting.isEnemy(o, hit)) {
                foe = hit;
                return true;
            }
            LivingEntity by = o.getLastHurtByMob();
            if (by != null && SkillTargeting.isEnemy(o, by)) {
                foe = by;
                return true;
            }
            return false;
        }

        @Override
        public void start() {
            ally.setTarget(foe);
            super.start();
        }
    }
}
