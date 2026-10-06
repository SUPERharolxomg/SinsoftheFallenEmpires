package com.sofe.pact;

/**
 * The numbers of the Pact of the Empires (docs/Anexos.md, A5), as plain functions so they are unit tested; the server
 * config may change the values (SoFEConfig, [pact]). Playing alone is never weaker: every bonus is 0 for one player.
 */
public final class PactRules {
    public static final int MAX_MEMBERS = 5;
    public static final double TOGETHER_RANGE = 48;
    public static final double XP_BONUS_PER_MEMBER = 0.10, FIND_BONUS_PER_ALLY = 0.05, FIND_BONUS_CAP = 0.20, BOSS_HEALTH_PER_PLAYER = 0.60;

    private PactRules() {
    }

    /** What each of the members together gets of a kill's XP: the whole, plus a bonus per extra member, split among them. */
    public static long xpShare(long xp, int together, double bonusPerMember) {
        if (together <= 1) return xp;
        return Math.round(xp * (1 + bonusPerMember * (together - 1)) / together);
    }

    /** The better odds of rarity for a player with allies near: a bonus per ally, capped. */
    public static double findBonus(int allies, double perAlly, double cap) {
        return Math.max(0, Math.min(cap, allies * perAlly));
    }

    /** A boss's health for the players who fight it: more for each extra player, never more damage. */
    public static double bossHealthMultiplier(int players, double perPlayer) {
        return 1 + Math.max(0, players - 1) * perPlayer;
    }

    /** Whether a player may join: the Pact has room and they are in no other. */
    public static boolean canJoin(int members, boolean inAnother, int max) {
        return !inAnother && members < max;
    }
}
