package com.birdhouse.tracker;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/** Verifies the fixed, independent and clearly illustrative demo countdowns. */
public class BirdhouseDemoModeTest
{
    private static final long START = 1_800_000_000L;

    @Test
    public void previewsFourDifferentStates()
    {
        List<BirdhouseTimer> timers = BirdhouseTimer.demoTimers(START);
        assertEquals(4, timers.size());
        assertEquals("READY", timers.get(0).display(START));
        assertEquals("04:00", timers.get(1).display(START));
        assertEquals("15:00", timers.get(2).display(START));
        assertEquals("42:00", timers.get(3).display(START));
    }

    @Test
    public void demoCountsDownAndReadyStaysVisible()
    {
        List<BirdhouseTimer> timers = BirdhouseTimer.demoTimers(START);
        assertEquals("03:00", timers.get(1).display(START + 60));
        assertEquals("READY", timers.get(1).display(START + 5 * 60));
        assertEquals("READY", timers.get(0).display(START + 5 * 60));
    }

    @Test
    public void demoUsesFourDistinctLocationsAndCannotBeChanged()
    {
        List<BirdhouseTimer> timers = BirdhouseTimer.demoTimers(START);
        assertEquals(4, timers.stream().map(t -> t.site).distinct().count());
        try
        {
            timers.clear();
            fail("Demo list should be immutable");
        }
        catch (UnsupportedOperationException expected)
        {
            // Pass.
        }
    }
}
