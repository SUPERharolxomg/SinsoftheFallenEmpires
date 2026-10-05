package com.sofe.mob;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Void hordes: every seventh nightfall, growing with the act and the group, never more than 30. */
class VoidHordesTest {

    @Test
    void theHordeComesAtTheNightfallOfEverySeventhDay() {
        assertTrue(VoidHordes.isHordeNight(6 * 24_000L + 13_000));
        assertTrue(VoidHordes.isHordeNight(13 * 24_000L + 13_000));
        assertFalse(VoidHordes.isHordeNight(5 * 24_000L + 13_000));
        assertFalse(VoidHordes.isHordeNight(6 * 24_000L + 12_999));
    }

    @Test
    void itGrowsWithTheActAndTheGroupButNeverPastThirty() {
        assertEquals(10, VoidHordes.size(1, 0));
        assertEquals(22, VoidHordes.size(5, 0));
        assertEquals(16, VoidHordes.size(1, 2));
        assertEquals(30, VoidHordes.size(5, 7));
    }
}
