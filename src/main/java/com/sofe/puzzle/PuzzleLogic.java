package com.sofe.puzzle;

import java.util.Arrays;

/** Pressing the runes of a puzzle, without Minecraft: which burn afterwards and whether it is solved. */
public final class PuzzleLogic {

    public enum Result { NOTHING, LIT, WRONG, SOLVED }

    /** The runes burning now, and for ORDER how many of the solution were pressed. */
    public static final class State {
        public final boolean[] lit;
        public int progress;

        public State(boolean[] lit, int progress) {
            this.lit = lit;
            this.progress = progress;
        }
    }

    private PuzzleLogic() {
    }

    public static State start(PuzzleDefinition puzzle) {
        boolean[] lit = new boolean[puzzle.runes().size()];
        if (puzzle.kind() == PuzzleDefinition.Kind.LIGHTS) for (int i = 0; i < lit.length; i++) lit[i] = puzzle.start().get(i);
        return new State(lit, 0);
    }

    public static Result press(PuzzleDefinition puzzle, State state, int stone) {
        if (stone < 0 || stone >= state.lit.length) return Result.NOTHING;
        if (puzzle.kind() == PuzzleDefinition.Kind.ORDER) {
            if (state.lit[stone]) return Result.NOTHING;
            if (puzzle.runes().get(stone) != puzzle.solution().get(state.progress)) {
                Arrays.fill(state.lit, false);
                state.progress = 0;
                return Result.WRONG;
            }
            state.lit[stone] = true;
            state.progress++;
            return state.progress == puzzle.solution().size() ? Result.SOLVED : Result.LIT;
        }
        for (int i = stone - 1; i <= stone + 1; i++) if (i >= 0 && i < state.lit.length) state.lit[i] = !state.lit[i];
        for (boolean b : state.lit) if (!b) return Result.LIT;
        return Result.SOLVED;
    }

    /**
     * How many presses a LIGHTS puzzle needs at the least, or -1 when no set of presses solves it (with some rows,
     * some starts cannot be solved). Pressing a rune twice undoes it, so every set of presses is tried once.
     */
    public static int fewestPresses(PuzzleDefinition puzzle) {
        if (puzzle.kind() == PuzzleDefinition.Kind.ORDER) return puzzle.solution().size();
        int n = puzzle.runes().size(), best = -1;
        for (int mask = 1; mask < 1 << n; mask++) {
            State s = start(puzzle);
            Result last = Result.NOTHING;
            for (int i = 0; i < n; i++) if ((mask & 1 << i) != 0) last = press(puzzle, s, i);
            if (last == Result.SOLVED && (best < 0 || Integer.bitCount(mask) < best)) best = Integer.bitCount(mask);
        }
        return best;
    }
}
