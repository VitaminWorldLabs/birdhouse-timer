package com.birdhouse.tracker;

import org.junit.Test;
import static org.junit.Assert.*;

/** Regression coverage for source-derived, per-location birdhouse countdowns. */
public class BirdhouseTimerRegressionTest
{
    private static final long NOW = 1_800_000_000L;

    @Test
    public void allFourSitesRemainIndependent()
    {
        BirdhouseSite[] sites = BirdhouseSite.values();
        assertEquals(4, sites.length);
        for (int i = 0; i < sites.length; i++)
        {
            BirdhouseTimer timer = BirdhouseTimer.parse(sites[i], "3:" + (NOW - i * 60), NOW);
            assertNotNull(timer);
            assertEquals(3_000L - i * 60L, timer.secondsRemaining(NOW));
        }
    }

    @Test
    public void allNineSeededBirdhouseTiersAreAccepted()
    {
        for (int tier = 1; tier <= 9; tier++)
        {
            int seededValue = tier * 3;
            BirdhouseTimer timer = BirdhouseTimer.parse(
                BirdhouseSite.MEADOW_NORTH, seededValue + ":" + NOW, NOW);
            assertNotNull(timer);
            assertEquals(3_000L, timer.secondsRemaining(NOW));
        }
    }

    @Test
    public void emptyAndUnseededStatesAreNotDisplayed()
    {
        assertNull(BirdhouseTimer.parse(BirdhouseSite.MEADOW_NORTH, "0:" + NOW, NOW));
        for (int tier = 0; tier <= 8; tier++)
        {
            int first = tier * 3 + 1;
            int second = tier * 3 + 2;
            assertNull(BirdhouseTimer.parse(BirdhouseSite.MEADOW_NORTH, first + ":" + NOW, NOW));
            assertNull(BirdhouseTimer.parse(BirdhouseSite.MEADOW_NORTH, second + ":" + NOW, NOW));
        }
    }

    @Test
    public void maturityBoundaryAndOfflineReadyAreStable()
    {
        BirdhouseTimer timer = BirdhouseTimer.parse(BirdhouseSite.MEADOW_SOUTH,
            "6:" + (NOW - 2_999L), NOW);
        assertNotNull(timer);
        assertEquals("00:01", timer.display(NOW));
        assertEquals("READY", timer.display(NOW + 1));
        assertEquals("READY", timer.display(NOW + 86_400));
    }

    @Test
    public void corruptedAndOutOfRangeRecordsNeverCreateTimers()
    {
        String[] badRecords = {null, "", "missing", "3", "3:1:2", "3:abc",
            "-3:" + NOW, "30:" + NOW, "3:0", "3:1499999999",
            "3:" + (NOW + 3_601L), "999999999999999999999:" + NOW};
        for (String bad : badRecords)
        {
            assertNull(BirdhouseTimer.parse(BirdhouseSite.VALLEY_NORTH, bad, NOW));
        }
    }

    @Test
    public void replantingCreatesDifferentWarningCycleMarker()
    {
        BirdhouseTimer initial = BirdhouseTimer.parse(BirdhouseSite.VALLEY_SOUTH,
            "9:" + (NOW - 100), NOW);
        BirdhouseTimer replanted = BirdhouseTimer.parse(BirdhouseSite.VALLEY_SOUTH,
            "9:" + NOW, NOW);
        assertNotNull(initial);
        assertNotNull(replanted);
        if (initial.cycleKey.equals(replanted.cycleKey))
        {
            fail("Replacement must have a distinct warning cycle key");
        }
    }

    @Test
    public void warningLeadTimeOptionsRemainExpected()
    {
        assertEquals(0, WarningLeadTime.OFF.minutes);
        assertEquals(5, WarningLeadTime.FIVE.minutes);
        assertEquals(10, WarningLeadTime.TEN.minutes);
        assertEquals(15, WarningLeadTime.FIFTEEN.minutes);
    }

    @Test
    public void demoValuesAreIndependentFromNormalSavedRecords()
    {
        java.util.List<BirdhouseTimer> demos = BirdhouseTimer.demoTimers(NOW);
        assertEquals(4, demos.size());
        assertEquals("READY", demos.get(0).display(NOW));
        assertEquals("04:00", demos.get(1).display(NOW));
        assertNull(BirdhouseTimer.parse(BirdhouseSite.VALLEY_NORTH, null, NOW));
    }
}
