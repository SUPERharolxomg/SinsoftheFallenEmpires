package com.sofe.world.region;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RegionTrackerTest {
    private final RegionTracker tracker = new RegionTracker();
    private final UUID alice = UUID.randomUUID();
    private final UUID bob = UUID.randomUUID();

    @Test
    void firstCheckAfterJoiningShowsTheRegion() {
        assertEquals(Optional.of(Region.SULTHARI), tracker.update(alice, Region.SULTHARI));
    }

    @Test
    void staysQuietInsideTheSameRegion() {
        tracker.update(alice, Region.SULTHARI);
        assertTrue(tracker.update(alice, Region.SULTHARI).isEmpty());
    }

    @Test
    void reportsEachCrossing() {
        tracker.update(alice, Region.SULTHARI);
        assertEquals(Optional.of(Region.NORDRATH), tracker.update(alice, Region.NORDRATH));
        assertEquals(Optional.of(Region.SULTHARI), tracker.update(alice, Region.SULTHARI));
    }

    @Test
    void playersAreTrackedSeparately() {
        tracker.update(alice, Region.SULTHARI);
        assertEquals(Optional.of(Region.SULTHARI), tracker.update(bob, Region.SULTHARI));
    }

    @Test
    void forgettingAPlayerShowsTheTitleAgainOnReturn() {
        tracker.update(alice, Region.AUREUM);
        tracker.forget(alice);
        assertEquals(Optional.of(Region.AUREUM), tracker.update(alice, Region.AUREUM));
    }
}
