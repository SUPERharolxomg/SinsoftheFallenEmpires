package com.sofe.skill;

import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.player.PlayerClassData;
import com.sofe.progression.ProgressionCapability;
import com.sofe.skill.data.SkillDataManager;
import com.sofe.skill.data.SkillStats;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * What a Bearer's skills leave running for a while, kept on the server only (lost on logout, as a
 * buff would be): the Knight's stance and timers, the Necromancer's stacks, the Thief's heist, the King's
 * decree, the Sorceress's eclipse. Times are game times in ticks.
 */
public final class ClassState {
    public enum Stance { SHIELD, CHARGE }

    private static final Map<UUID, ClassState> STATES = new HashMap<>();

    public Stance stance = Stance.SHIELD;
    // Knight
    public long shieldWallUntil, lastStandUntil, oathUntil;
    public float containedWrath;
    // Necromancer
    public int riteStacks;
    public long riteUntil;
    // Thief
    public long heistUntil;
    /** The enemy last marked, whose Marks the HUD shows. */
    public java.util.UUID lastMarked;
    // King
    public String decree;
    public long decreeUntil;
    public double decreeRadius;
    public net.minecraft.world.phys.Vec3 decreeCenter;
    public long crownUntil;
    // Sorceress
    public long eclipseUntil;

    public static ClassState of(Player player) {
        return STATES.computeIfAbsent(player.getUUID(), id -> new ClassState());
    }

    public static void forget(Player player) {
        STATES.remove(player.getUUID());
    }

    public static boolean active(long until, Player player) {
        return player.level().getGameTime() < until;
    }

    public static Optional<PlayerClass> classOf(Player player) {
        return PlayerClassCapability.get(player).flatMap(PlayerClassData::get);
    }

    /** A learned upgrade's value times its rank (0 when not learned), for the passives' code. */
    public static double upgradeValue(Player player, String upgrade, String value) {
        int rank = rank(player, upgrade);
        if (rank <= 0) return 0;
        return SkillCatalog.byId(upgrade).flatMap(i -> SkillDataManager.forClass(i.owner())).flatMap(d -> d.skill(upgrade))
                .map(s -> s.param(value, 0) * rank).orElse(0.0);
    }

    /** The rank the player has in a skill (0 when not learned). */
    public static int rank(Player player, String skill) {
        return ProgressionCapability.get(player).map(p -> p.skills().rank(skill)).orElse(0);
    }

    /** A learned passive's values at its rank, or empty when the player does not have it. */
    public static Optional<SkillStats> passive(Player player, String skill) {
        int rank = rank(player, skill);
        if (rank <= 0) return Optional.empty();
        return SkillCatalog.byId(skill).flatMap(info -> SkillDataManager.forClass(info.owner())).flatMap(d -> d.skill(skill))
                .map(s -> Upgrades.apply(skill, s.withRank(rank), id -> rank(player, id)));
    }
}
