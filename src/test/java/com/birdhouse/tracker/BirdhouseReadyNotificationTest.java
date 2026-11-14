package com.birdhouse.tracker;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/** READY notification formatting and exact maturity boundaries. */
public class BirdhouseReadyNotificationTest
{
    private static final long NOW = 1_800_000_000L;

    private static BirdhouseTimer timer(BirdhouseSite site, long plantedAt)
    {
        return BirdhouseTimer.parse(site, "3:" + plantedAt, NOW);
    }

    @Test
    public void beforeMaturityIsNotReady()
    {
        assertEquals(1L, timer(BirdhouseSite.MEADOW_NORTH, NOW - 2999).secondsRemaining(NOW));
    }

    @Test
    public void exactlyMatureIsReady()
    {
        BirdhouseTimer t = timer(BirdhouseSite.MEADOW_NORTH, NOW - 3000);
        assertEquals(0L, t.secondsRemaining(NOW));
        assertEquals("READY", t.display(NOW));
    }

    @Test
    public void afterMaturityRemainsReady()
    {
        BirdhouseTimer t = timer(BirdhouseSite.MEADOW_NORTH, NOW - 3001);
        assertTrue(t.secondsRemaining(NOW) < 0);
        assertEquals("READY", t.display(NOW));
    }

    @Test
    public void singleReadyMessageNamesLocation()
    {
        assertEquals(BirdhouseWarningMessage.READY_TEXT,
            BirdhouseWarningMessage.formatReady(Collections.singletonList(
                timer(BirdhouseSite.MEADOW_SOUTH, NOW - 3000))));
    }

    @Test
    public void simultaneousReadyMessageGroupsLocations()
    {
        assertEquals(BirdhouseWarningMessage.READY_TEXT,
            BirdhouseWarningMessage.formatReady(Arrays.asList(
                timer(BirdhouseSite.MEADOW_NORTH, NOW - 3000),
                timer(BirdhouseSite.VALLEY_SOUTH, NOW - 3001))));
    }

    @Test
    public void readyWorksIndependentlyOfAdvanceWarningSetting()
    {
        BirdhouseTimer t = timer(BirdhouseSite.VALLEY_NORTH, NOW - 3000);
        assertEquals(BirdhouseWarningMessage.READY_TEXT,
            BirdhouseWarningMessage.formatReady(Collections.singletonList(t)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyReadyMessageIsInvalid()
    {
        BirdhouseWarningMessage.formatReady(Collections.emptyList());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullReadyMessageIsInvalid()
    {
        BirdhouseWarningMessage.formatReady(null);
    }
}
