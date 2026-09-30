package com.sofe.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerClassDataTest {

    @Test
    void startsWithoutAClass() {
        PlayerClassData data = new PlayerClassData();
        assertFalse(data.hasClass());
        assertTrue(data.get().isEmpty());
    }

    @Test
    void theFirstChoiceSticks() {
        PlayerClassData data = new PlayerClassData();
        assertEquals(PlayerClassData.ChoiceResult.CHOSEN, data.choose(PlayerClass.SORCERESS));
        assertEquals(PlayerClassData.ChoiceResult.ALREADY_CHOSEN, data.choose(PlayerClass.KING));
        assertEquals(PlayerClass.SORCERESS, data.get().orElseThrow());
    }

    @Test
    void copyKeepsTheClassAfterDeath() {
        PlayerClassData before = new PlayerClassData();
        before.choose(PlayerClass.THIEF);
        PlayerClassData afterRespawn = new PlayerClassData();
        afterRespawn.copyFrom(before);
        assertEquals(PlayerClass.THIEF, afterRespawn.get().orElseThrow());
    }

    @Test
    void setCanClear() {
        PlayerClassData data = new PlayerClassData();
        data.choose(PlayerClass.KNIGHT);
        data.set(null);
        assertFalse(data.hasClass());
    }
}
