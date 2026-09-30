package com.sofe.skill;

import com.sofe.player.PlayerClass;

/**
 * What a skill is: its class, the level that unlocks it and its type. Numbers (cost, cooldown,
 * damage) are data in data/sofe/skills/&lt;class&gt;.json; names and descriptions are lang keys.
 */
public record SkillInfo(String id, PlayerClass owner, int level, SkillType type) {

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
}
