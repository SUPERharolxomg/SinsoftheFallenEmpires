package com.sofe.mob;

import net.minecraft.world.Difficulty;

/**
 * Boss strength by vanilla difficulty (docs/Jugabilidad.md, G6). Peaceful never happens in a journey
 * (it is replaced by Easy), but it is treated as Easy here too.
 */
public final class BossDifficulty {

    private BossDifficulty() {
    }

    /** Multiplier for boss health and damage: Easy −25%, Normal base, Hard +25%. */
    public static double multiplier(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL, EASY -> 0.75;
            case NORMAL -> 1.0;
            case HARD -> 1.25;
        };
    }

    /** Hard adds extra mechanics in phase 2. */
    public static boolean extraMechanics(Difficulty difficulty) {
        return difficulty == Difficulty.HARD;
    }
}
