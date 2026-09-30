package com.sofe.player;

import java.util.Optional;

/**
 * Which Bearer a player is. Chosen once (UC-01); after that it only changes through
 * admin tools or a future respec of the class itself. Plain Java so it is unit tested directly.
 */
public final class PlayerClassData {
    private PlayerClass playerClass;

    public enum ChoiceResult { CHOSEN, ALREADY_CHOSEN }

    public Optional<PlayerClass> get() {
        return Optional.ofNullable(playerClass);
    }

    public boolean hasClass() {
        return playerClass != null;
    }

    /** The player's own choice: only works while they have no class yet. */
    public ChoiceResult choose(PlayerClass chosen) {
        if (playerClass != null) {
            return ChoiceResult.ALREADY_CHOSEN;
        }
        playerClass = chosen;
        return ChoiceResult.CHOSEN;
    }

    /** Unconditional set, for loading saved data and client sync. Null clears it. */
    public void set(PlayerClass value) {
        playerClass = value;
    }

    public void copyFrom(PlayerClassData other) {
        playerClass = other.playerClass;
    }
}
