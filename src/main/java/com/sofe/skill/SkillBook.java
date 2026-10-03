package com.sofe.skill;

import com.sofe.player.PlayerClass;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The skills a player has learned, their ranks and the Combat Bar slots (docs/Clases.md,
 * "Skill points and ranks"). Plain Java so the rules are unit tested directly.
 */
public final class SkillBook {
    /** Slots 0-4 hold actives (Left Alt + 1-5), slot 5 the ultimate (Left Alt + 6). */
    public static final int SLOTS = 6;
    public static final int ULTIMATE_SLOT = 5;

    public enum LearnResult { LEARNED, NO_POINTS, WRONG_CLASS, NEEDS_LEVEL, NEEDS_PARENT, MAX_RANK }

    private final Map<String, Integer> ranks = new HashMap<>();
    private final String[] slots = new String[SLOTS];

    public int rank(String skill) {
        return ranks.getOrDefault(skill, 0);
    }

    public Map<String, Integer> ranks() {
        return Map.copyOf(ranks);
    }

    public int pointsSpent() {
        return ranks.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** The level a player needs for the next rank: the skill's level, plus one per rank already taken. */
    public int levelForNextRank(SkillInfo skill) {
        return skill.level() + rank(skill.id());
    }

    public LearnResult check(SkillInfo skill, PlayerClass playerClass, int playerLevel, int freePoints) {
        if (skill.owner() != playerClass) return LearnResult.WRONG_CLASS;
        if (rank(skill.id()) >= skill.maxRank()) return LearnResult.MAX_RANK;
        if (skill.parent() != null && rank(skill.parent()) < 1) return LearnResult.NEEDS_PARENT;
        if (playerLevel < levelForNextRank(skill)) return LearnResult.NEEDS_LEVEL;
        if (freePoints <= 0) return LearnResult.NO_POINTS;
        return LearnResult.LEARNED;
    }

    /** Adds a rank if the rules allow it; the caller spends the point when the result is LEARNED. */
    public LearnResult learn(SkillInfo skill, PlayerClass playerClass, int playerLevel, int freePoints) {
        LearnResult result = check(skill, playerClass, playerLevel, freePoints);
        if (result != LearnResult.LEARNED) return result;
        int newRank = rank(skill.id()) + 1;
        ranks.put(skill.id(), newRank);
        if (newRank == 1) {
            assignSlot(skill);
        }
        return LearnResult.LEARNED;
    }

    /** A newly learned active goes to the first free slot, the ultimate to its own slot; passives have none. */
    private void assignSlot(SkillInfo skill) {
        if (skill.type() == SkillType.ULTIMATE) {
            slots[ULTIMATE_SLOT] = skill.id();
        } else if (skill.type() == SkillType.ACTIVE) {
            for (int i = 0; i < ULTIMATE_SLOT; i++) {
                if (slots[i] == null) {
                    slots[i] = skill.id();
                    return;
                }
            }
        }
    }

    /**
     * Puts a learned active skill in one of the five Combat Bar slots (the ultimate keeps its own). A skill
     * already in that slot moves to the one the new skill leaves, so nothing learned is lost.
     */
    public boolean assign(SkillInfo skill, int slot) {
        if (skill.type() != SkillType.ACTIVE || rank(skill.id()) < 1 || slot < 0 || slot >= ULTIMATE_SLOT) return false;
        int from = -1;
        for (int i = 0; i < ULTIMATE_SLOT; i++) {
            if (skill.id().equals(slots[i])) from = i;
        }
        String displaced = slots[slot];
        slots[slot] = skill.id();
        if (from >= 0 && from != slot) slots[from] = displaced;
        return true;
    }

    public Optional<String> slot(int index) {
        return index >= 0 && index < SLOTS ? Optional.ofNullable(slots[index]) : Optional.empty();
    }

    public List<String> slots() {
        return Arrays.asList(slots.clone());
    }

    /** Respec: forgets every skill and returns the points spent. */
    public int reset() {
        int spent = pointsSpent();
        ranks.clear();
        Arrays.fill(slots, null);
        return spent;
    }

    /** Loading from the save; unknown skills are dropped. */
    public void load(Map<String, Integer> savedRanks, List<String> savedSlots) {
        ranks.clear();
        savedRanks.forEach((id, r) -> SkillCatalog.byId(id).ifPresent(info -> ranks.put(id, Math.max(0, Math.min(info.maxRank(), r)))));
        Arrays.fill(slots, null);
        for (int i = 0; i < Math.min(SLOTS, savedSlots.size()); i++) {
            String id = savedSlots.get(i);
            slots[i] = id != null && !id.isEmpty() && ranks.containsKey(id) ? id : null;
        }
    }
}
