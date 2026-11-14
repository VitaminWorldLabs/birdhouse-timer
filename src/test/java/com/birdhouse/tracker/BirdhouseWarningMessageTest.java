package com.birdhouse.tracker;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/** Notifications identify the location without changing the warning cycle logic. */
public class BirdhouseWarningMessageTest
{
    private static final long NOW = 1_800_000_000L;

    private static BirdhouseTimer timer(BirdhouseSite site)
    {
        return BirdhouseTimer.parse(site, "3:" + (NOW - 2_500), NOW);
    }

    @Test
    public void oneLocationIsNamed()
    {
        assertEquals("Birdhouse ready within 10 minutes: Mushroom Meadow South.",
            BirdhouseWarningMessage.format(
                Collections.singletonList(timer(BirdhouseSite.MEADOW_SOUTH)), 10));
    }

    @Test
    public void multipleLocationsAppearInOneNotification()
    {
        assertEquals("Birdhouses ready within 5 minutes: Mushroom Meadow North, Verdant Valley SW.",
            BirdhouseWarningMessage.format(Arrays.asList(
                timer(BirdhouseSite.MEADOW_NORTH), timer(BirdhouseSite.VALLEY_SOUTH)), 5));
    }

    @Test
    public void allFourLocationsAreIncluded()
    {
        BirdhouseTimer[] timers = Arrays.stream(BirdhouseSite.values())
            .map(BirdhouseWarningMessageTest::timer).toArray(BirdhouseTimer[]::new);
        String message = BirdhouseWarningMessage.format(Arrays.asList(timers), 15);
        for (BirdhouseSite site : BirdhouseSite.values())
        {
            assertTrue(message.contains(site.displayLabel()));
        }
        assertTrue(message.startsWith("Birdhouses ready within 15 minutes:"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotFormatEmptyWarnings()
    {
        BirdhouseWarningMessage.format(Collections.emptyList(), 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cannotFormatDisabledWarning()
    {
        BirdhouseWarningMessage.format(Collections.singletonList(timer(BirdhouseSite.MEADOW_NORTH)), 0);
    }
}
