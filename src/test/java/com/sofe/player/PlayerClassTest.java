package com.sofe.player;

import com.sofe.story.Sin;
import com.sofe.world.region.Region;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PlayerClassTest {

    @Test
    void everyBearerHasADifferentResourceTemptationAndOrigin() {
        Set<ResourceType> resources = Arrays.stream(PlayerClass.values()).map(PlayerClass::resource).collect(Collectors.toSet());
        Set<Sin> temptations = Arrays.stream(PlayerClass.values()).map(PlayerClass::temptation).collect(Collectors.toSet());
        Set<Region> origins = Arrays.stream(PlayerClass.values()).map(PlayerClass::origin).collect(Collectors.toSet());

        assertEquals(5, resources.size());
        assertEquals(5, temptations.size());
        assertEquals(5, origins.size());
        assertFalse(origins.contains(Region.OCEAN));
    }

    @Test
    void matchesTheStoryDocument() {
        assertEquals(ResourceType.RESOLVE, PlayerClass.KNIGHT.resource());
        assertEquals(Sin.WRATH, PlayerClass.KNIGHT.temptation());
        assertEquals(Region.KHEMET, PlayerClass.NECROMANCER.origin());
        assertEquals(Sin.LUST, PlayerClass.SORCERESS.temptation());
        assertEquals(Region.NORDRATH, PlayerClass.THIEF.origin());
        assertEquals(ResourceType.AUTHORITY, PlayerClass.KING.resource());
    }

    @Test
    void idsRoundTrip() {
        for (PlayerClass c : PlayerClass.values()) {
            assertEquals(c, PlayerClass.byId(c.id()).orElseThrow());
        }
        assertTrue(PlayerClass.byId("vanguard").isEmpty());
    }

    @Test
    void envyAndGluttonyAreNoBearersTemptation() {
        EnumSet<Sin> unused = EnumSet.allOf(Sin.class);
        Arrays.stream(PlayerClass.values()).map(PlayerClass::temptation).forEach(unused::remove);
        assertEquals(EnumSet.of(Sin.GLUTTONY, Sin.ENVY), unused);
    }
}
