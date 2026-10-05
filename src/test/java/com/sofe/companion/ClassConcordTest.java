package com.sofe.companion;

import com.sofe.player.PlayerClass;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClassConcordTest {

    @Test
    void aBearerAloneHasNoConcord() {
        assertTrue(ClassConcord.concord(1, EnumSet.of(PlayerClass.KNIGHT)).isEmpty());
    }

    @Test
    void everyDifferentClassOfTheGroupCounts() {
        var concord = ClassConcord.concord(2, EnumSet.of(PlayerClass.KNIGHT, PlayerClass.SORCERESS));
        assertEquals(EnumSet.of(PlayerClass.KNIGHT, PlayerClass.SORCERESS), concord);
        assertFalse(ClassConcord.pillars(concord));
    }

    @Test
    void twoBearersOfTheSameClassGiveOneConcord() {
        assertEquals(EnumSet.of(PlayerClass.THIEF), ClassConcord.concord(2, EnumSet.of(PlayerClass.THIEF)));
    }

    @Test
    void allFiveAreTheFivePillars() {
        assertTrue(ClassConcord.pillars(ClassConcord.concord(5, EnumSet.allOf(PlayerClass.class))));
    }

    @Test
    void theNecromancersConcordStealsMoreLifeFromTheEdgeOfDeath() {
        assertEquals(0.03, ClassConcord.lifeSteal(0.8f), 1e-9);
        assertEquals(0.03, ClassConcord.lifeSteal(0.25f), 1e-9);
        assertEquals(0.05, ClassConcord.lifeSteal(0.2f), 1e-9);
    }
}
