package com.sofe.skill;

import com.sofe.combat.CombatData;
import com.sofe.combat.Rune;
import com.sofe.skill.data.ClassSkillData;
import com.sofe.skill.data.SkillStats;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * The effect of one active skill (Strategy pattern, docs/Arquitectura.md). Cost, cooldown and
 * validation are handled by {@link SkillCaster}; a skill only does its effect.
 */
@FunctionalInterface
public interface Skill {

    Result cast(Context context);

    record Context(ServerPlayer player, SkillInfo info, SkillStats stats, CombatData combat, ClassSkillData classData) {
    }

    /** Where the skill landed (constellations happen there) and the rune it leaves, if any. */
    record Result(Vec3 impact, Optional<Rune> rune) {
        public static Result at(Vec3 impact) {
            return new Result(impact, Optional.empty());
        }

        public static Result withRune(Vec3 impact, Rune rune) {
            return new Result(impact, Optional.of(rune));
        }
    }
}
