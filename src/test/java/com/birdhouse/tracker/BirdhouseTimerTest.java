package com.birdhouse.tracker;

import org.junit.Test;
import static org.junit.Assert.*;

public class BirdhouseTimerTest
{
    private static final long NOW = 1_800_000_000L;

    @Test
    public void seededSiteCountsDownFromPlacement()
    {
        BirdhouseTimer timer = BirdhouseTimer.parse(BirdhouseSite.MEADOW_NORTH, "3:" + (NOW - 60), NOW);
        assertNotNull(timer);
        assertEquals(2940, timer.secondsRemaining(NOW));
        assertEquals("49:00", timer.display(NOW));
    }

    @Test
    public void readyPersistsAfterMaturity()
    {
        BirdhouseTimer timer = BirdhouseTimer.parse(BirdhouseSite.VALLEY_SOUTH, "27:" + (NOW - 4000), NOW);
        assertNotNull(timer);
        assertEquals("READY", timer.display(NOW));
    }

    @Test
    public void unseededAndEmptyAreHidden()
    {
        assertNull(BirdhouseTimer.parse(BirdhouseSite.MEADOW_NORTH, "0:" + NOW, NOW));
        assertNull(BirdhouseTimer.parse(BirdhouseSite.MEADOW_NORTH, "1:" + NOW, NOW));
        assertNull(BirdhouseTimer.parse(BirdhouseSite.MEADOW_NORTH, "2:" + NOW, NOW));
        assertNull(BirdhouseTimer.parse(BirdhouseSite.MEADOW_NORTH, "28:" + NOW, NOW));
    }

    @Test
    public void missingCorruptRecordsAreHidden()
    {
        assertNull(BirdhouseTimer.parse(BirdhouseSite.MEADOW_NORTH, null, NOW));
        assertNull(BirdhouseTimer.parse(BirdhouseSite.MEADOW_NORTH, "bogus", NOW));
        assertNull(BirdhouseTimer.parse(BirdhouseSite.MEADOW_NORTH, "3:not-a-timestamp", NOW));
        assertNull(BirdhouseTimer.parse(BirdhouseSite.MEADOW_NORTH, "3:0", NOW));
    }
}
