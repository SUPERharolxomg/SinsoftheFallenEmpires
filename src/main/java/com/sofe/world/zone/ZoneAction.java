package com.sofe.world.zone;

/** Every way a block of a protected place can change (docs/Mundo.md, W2 layer 3). */
public enum ZoneAction {
    BREAK,
    PLACE,
    /** Buckets, flint and steel, hoes, axes and other items used on a block. */
    USE_ITEM_ON_BLOCK,
    EXPLOSION,
    MOB_GRIEFING,
    FLUID,
    PISTON,
    /** Fire catching or spreading. */
    FIRE,
    /**
     * Taking or moving what a place holds: opening its chests, barrels and furniture drawers, the plant in a pot, the
     * book on a lectern, glow berries, and the armor stands, item frames and paintings of its rooms.
     */
    TAKE
}
