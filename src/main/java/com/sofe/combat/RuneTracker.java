package com.sofe.combat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** The runes waiting to form a constellation. The third rune completes it and the list starts again. */
public final class RuneTracker {
    public static final int RUNES_PER_CONSTELLATION = 3;

    public record Completed(Constellation constellation, List<Rune> runes) {
    }

    private final List<Rune> runes = new ArrayList<>();

    public Optional<Completed> add(Rune rune) {
        runes.add(rune);
        if (runes.size() < RUNES_PER_CONSTELLATION) {
            return Optional.empty();
        }
        List<Rune> used = List.copyOf(runes);
        runes.clear();
        return Optional.of(new Completed(Constellation.of(used), used));
    }

    public List<Rune> current() {
        return List.copyOf(runes);
    }

    public void set(List<Rune> value) {
        runes.clear();
        runes.addAll(value.subList(0, Math.min(value.size(), RUNES_PER_CONSTELLATION - 1)));
    }

    public void clear() {
        runes.clear();
    }
}
