package com.sofe.skill;

import com.sofe.player.PlayerClass;

/**
 * What a skill is: its class, the level that unlocks it, its type, the skill it comes from in
 * the tree (the arrows of the Diablo II style tree; null for the first row) and its column.
 * Numbers (cost, cooldown, damage) are data in data/sofe/skills/&lt;class&gt;.json; names and
 * descriptions are lang keys.
 */
public record SkillInfo(String id, PlayerClass owner, int level, SkillType type, String parent, int column) {

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

    /** Row in the tree: 0 for level 1, 1 for level 11, 2 for level 21, 3 for level 30. */
    public int row() {
        return switch (level) {
            case 1 -> 0;
            case 11 -> 1;
            case 21 -> 2;
            default -> 3;
        };
    }
}
