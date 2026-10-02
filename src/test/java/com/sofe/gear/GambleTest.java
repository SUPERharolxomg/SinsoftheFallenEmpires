package com.sofe.gear;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GambleTest {

    private static Map<Gamble.Outcome, Integer> tally(GearSlot slot, int rolls) {
        Random random = new Random(7);
        Map<Gamble.Outcome, Integer> counts = new EnumMap<>(Gamble.Outcome.class);
        for (int i = 0; i < rolls; i++) counts.merge(Gamble.roll(random, slot), 1, Integer::sum);
        return counts;
    }

    @Test
    void mostVeilsHoldPlainGearAndAFewARelic() {
        Map<Gamble.Outcome, Integer> counts = tally(GearSlot.WEAPON, 100_000);
        assertEquals(0.08, counts.get(Gamble.Outcome.SWINDLE) / 100_000.0, 0.01);
        assertEquals(0.47, counts.get(Gamble.Outcome.COMMON) / 100_000.0, 0.01);
        assertEquals(0.03, counts.get(Gamble.Outcome.RELIC) / 100_000.0, 0.005);
    }

    @Test
    void jewelryVeilsHideRelicsMoreOften() {
        int weapon = tally(GearSlot.WEAPON, 100_000).get(Gamble.Outcome.RELIC);
        int jewelry = tally(GearSlot.JEWELRY, 100_000).get(Gamble.Outcome.RELIC);
        assertTrue(jewelry > weapon * 1.6, "jewelry " + jewelry + " vs weapon " + weapon);
    }

    @Test
    void aRelicMustFitTheSlotAndTheLevel() {
        List<GearDataManager.Relic> relics = List.of(
                new GearDataManager.Relic("low_ring", "sofe:a", 3, List.of(), true, GearSlot.JEWELRY, 2),
                new GearDataManager.Relic("high_ring", "sofe:b", 12, List.of(), true, GearSlot.JEWELRY, 11),
                new GearDataManager.Relic("boss_blade", "sofe:c", 8, List.of(), false, null, 8),
                new GearDataManager.Relic("sword", "sofe:d", 4, List.of(), true, GearSlot.WEAPON, 3));
        Random random = new Random(1);
        for (int i = 0; i < 50; i++) {
            assertEquals("low_ring", Gamble.pickRelic(relics, GearSlot.JEWELRY, 3, random).orElseThrow().id());
        }
        assertTrue(Gamble.pickRelic(relics, GearSlot.ARMOR, 20, random).isEmpty());
        assertEquals("sword", Gamble.pickRelic(relics, GearSlot.WEAPON, 1, random).orElseThrow().id());
    }

    @Test
    void veiledGearComesAtOrJustAboveTheBuyer() {
        Random random = new Random(3);
        for (int i = 0; i < 200; i++) {
            int level = Gamble.itemLevel(5, random);
            assertTrue(level >= 5 && level <= 5 + Gamble.LEVEL_SPREAD, "level " + level);
        }
    }
}
