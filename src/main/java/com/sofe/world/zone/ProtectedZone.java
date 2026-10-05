package com.sofe.world.zone;

import java.util.Locale;

/**
 * A protected place of the story (docs/Mundo.md, W2 layer 3): a box of blocks and the kind of
 * place it is, which sets what players may change inside.
 */
public record ProtectedZone(String id, Kind kind, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    /** What each kind of place allows (the table in docs/Mundo.md). */
    public enum Kind {
        CITY(false, false),
        HOMESTEAD(true, true),
        CAMP(false, false),
        DUNGEON(false, false),
        ARENA(false, false);

        private final boolean canBreak, canPlace;

        Kind(boolean canBreak, boolean canPlace) {
            this.canBreak = canBreak;
            this.canPlace = canPlace;
        }

        public boolean allows(ZoneAction action) {
            return switch (action) {
                case BREAK -> canBreak;
                case PLACE, USE_ITEM_ON_BLOCK -> canPlace;
                case EXPLOSION, MOB_GRIEFING, FLUID, PISTON, FIRE -> this == HOMESTEAD;
            };
        }

        public static Kind byId(String id) {
            return valueOf(id.toUpperCase(Locale.ROOT));
        }

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public ProtectedZone {
        if (minX > maxX || minY > maxY || minZ > maxZ) throw new IllegalArgumentException(id + ": empty zone");
    }

    /** Inclusive on every side. */
    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
}
