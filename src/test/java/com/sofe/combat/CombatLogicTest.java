package com.sofe.combat;

import com.sofe.skill.data.ResourceRules;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.sofe.combat.Rune.*;
import static org.junit.jupiter.api.Assertions.*;

class CombatLogicTest {

    @Test
    void manaIsSpentOnlyWhenThereIsEnough() {
        ResourcePool mana = new ResourcePool(new ResourceRules(100, 20, 2));
        assertTrue(mana.spend(12));
        assertEquals(8, mana.current());
        assertFalse(mana.spend(10), "8 mana cannot pay 10");
        assertEquals(8, mana.current(), "a refused spend changes nothing");
    }

    @Test
    void manaRegeneratesPerSecondAndStopsAtTheMaximum() {
        ResourcePool mana = new ResourcePool(new ResourceRules(100, 99, 2));
        for (int i = 0; i < 10; i++) mana.tick(); // half a second: +1
        assertEquals(100, mana.current(), 0.001);
        for (int i = 0; i < 100; i++) mana.tick();
        assertEquals(100, mana.current(), 0.001);
    }

    @Test
    void newRulesKeepTheValueInRange() {
        ResourcePool pool = new ResourcePool(new ResourceRules(100, 100, 0));
        pool.setRules(new ResourceRules(60, 30, 0));
        assertEquals(60, pool.current());
        pool.reset();
        assertEquals(30, pool.current());
    }

    @Test
    void rulesRejectBadValues() {
        assertThrows(IllegalArgumentException.class, () -> new ResourceRules(0, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new ResourceRules(100, 150, 1));
        assertThrows(IllegalArgumentException.class, () -> new ResourceRules(100, 10, -1));
    }

    @Test
    void cooldownsCountGameTicks() {
        CooldownTracker cooldowns = new CooldownTracker();
        assertTrue(cooldowns.isReady("frost_lance", 1000));
        cooldowns.start("frost_lance", 1000, 40);
        assertFalse(cooldowns.isReady("frost_lance", 1039));
        assertEquals(1, cooldowns.remaining("frost_lance", 1039));
        assertTrue(cooldowns.isReady("frost_lance", 1040));
        assertTrue(cooldowns.isReady("ember_verse", 1000), "each skill has its own cooldown");
    }

    @Test
    void threeRunesCompleteAConstellationAndStartOver() {
        RuneTracker runes = new RuneTracker();
        assertTrue(runes.add(FIRE).isEmpty());
        assertTrue(runes.add(FROST).isEmpty());
        Optional<RuneTracker.Completed> done = runes.add(STORM);
        assertEquals(Constellation.STEAM_BURST, done.orElseThrow().constellation());
        assertTrue(runes.current().isEmpty(), "runes clear after a constellation");
    }

    @Test
    void combinationsFromTheClassesDocument() {
        assertEquals(Constellation.STEAM_BURST, Constellation.of(List.of(STORM, FIRE, FROST)));
        assertEquals(Constellation.SOLAR_FLARE, Constellation.of(List.of(FIRE, FIRE, FIRE)));
        assertEquals(Constellation.WINTERS_GRASP, Constellation.of(List.of(FROST, FROST, FROST)));
        assertEquals(Constellation.TEMPEST_CROWN, Constellation.of(List.of(STORM, STORM, STORM)));
        assertEquals(Constellation.LESSER_CONSTELLATION, Constellation.of(List.of(FIRE, STORM, FIRE)));
        assertEquals(FIRE, Constellation.majority(List.of(FIRE, STORM, FIRE)));
    }
}
