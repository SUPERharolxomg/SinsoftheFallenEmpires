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
    FIRE
}
