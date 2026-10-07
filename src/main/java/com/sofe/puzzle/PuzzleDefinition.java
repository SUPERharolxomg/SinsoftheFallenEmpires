package com.sofe.puzzle;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * A rune puzzle from data/sofe/puzzles/*.json: a row of Rune Stones and a Riddle Tablet before a dungeon's seal or
 * inside a ruin. Its riddle is lang text: puzzle.sofe.&lt;name&gt;.riddle.1 to .&lt;lines&gt;.
 *
 * @param runes    the rune carved on each stone, left to right as the player faces the row
 * @param solution ORDER: the runes in the order they must be pressed
 * @param start    LIGHTS: which stones burn at the start
 * @param gate     the Sealed Gate (structure_positions.json) it opens, or null for a ruin's puzzle that only a quest asks for
 * @param boss     a boss whose fall also opens the gate, so a Bearer who beat it never has to solve it again
 * @param anchor   where it stands: a Waystone of structure_positions.json, the row put outward of it
 * @param x        or a place of its own (x, z), the row facing {@code facing}
 */
public record PuzzleDefinition(String id, Kind kind, List<Rune> runes, List<Rune> solution, List<Boolean> start, String gate,
                               String boss, String anchor, Integer x, Integer z, String facing, int riddleLines) {

    public enum Kind {
        /** Press the runes in the order the riddle tells; a wrong rune puts them all out. */
        ORDER,
        /** Pressing a rune turns it and its neighbours: make every rune burn. */
        LIGHTS
    }

    public enum Rune {
        SUN, MOON, STAR, FLAME, WAVE, EYE;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Optional<Rune> byId(String id) {
            for (Rune r : values()) if (r.id().equals(id)) return Optional.of(r);
            return Optional.empty();
        }
    }

    public PuzzleDefinition {
        runes = List.copyOf(runes);
        solution = List.copyOf(solution);
        start = List.copyOf(start);
        if (runes.size() < 2 || runes.size() > 6) throw new IllegalArgumentException(id + ": a puzzle has 2 to 6 runes");
        if (kind == Kind.ORDER && (solution.isEmpty() || !runes.containsAll(solution) || solution.size() != runes.size()))
            throw new IllegalArgumentException(id + ": the solution must use every rune of the row once");
        if (kind == Kind.ORDER && solution.stream().distinct().count() != solution.size())
            throw new IllegalArgumentException(id + ": a rune appears twice in the solution");
        if (kind == Kind.LIGHTS && start.size() != runes.size()) throw new IllegalArgumentException(id + ": start must give every rune");
        if (anchor == null && (x == null || z == null)) throw new IllegalArgumentException(id + ": a puzzle needs an anchor or x and z");
    }

    /** "sofe:nordrath_forge" -> "puzzle.sofe.nordrath_forge". */
    public String translationKey() {
        return "puzzle.sofe." + id.substring(id.indexOf(':') + 1).replace('/', '.');
    }
}
