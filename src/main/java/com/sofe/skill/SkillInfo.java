package com.sofe.skill;

import com.sofe.player.PlayerClass;

/**
 * What a skill is: its class, the level that unlocks it, its type, the skill it comes from in
 * the tree (the arrows of the Diablo II style tree; null for the first row), its branch (one of the
 * three tabs of the tree) and its column in it, and for an upgrade the skills it improves.
 * Numbers (cost, cooldown, damage) are data in data/sofe/skills/&lt;class&gt;.json; names and
 * descriptions are lang keys.
 */
public record SkillInfo(String id, PlayerClass owner, int level, SkillType type, String parent, int column, int tab, java.util.List<String> upgrades) {

    /** The levels that start a row of the tree. */
    public static final int[] ROW_LEVELS = {1, 6, 11, 16, 21, 26, 30};

    public SkillInfo {
        upgrades = java.util.List.copyOf(upgrades);
    }

    /** An upgrade: a passive that improves other skills (Diablo II synergies and changes). */
    public boolean isUpgrade() {
        return !upgrades.isEmpty();
    }

    public static final int MAX_RANK = 5;

    public String translationKey() {
        return "skill.sofe." + id;
    }

    public String descriptionKey() {
        return translationKey() + ".desc";
    }

    /** Icon in textures/gui/skill/&lt;id&gt;.png (docs/Arte.md). */
    public String iconPath() {
        return "textures/gui/skill/" + id + ".png";
    }

    /** Ultimates have a single rank; the others go up to rank 5 (docs/Clases.md). */
    public int maxRank() {
        return type == SkillType.ULTIMATE ? 1 : MAX_RANK;
    }

    /** Row in the tree: one per unlock level of {@link #ROW_LEVELS}. */
    public int row() {
        int row = 0;
        for (int i = 0; i < ROW_LEVELS.length; i++) {
            if (level >= ROW_LEVELS[i]) row = i;
        }
        return row;
    }
}
