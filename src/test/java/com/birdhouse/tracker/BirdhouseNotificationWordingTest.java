package com.birdhouse.tracker;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

public class BirdhouseNotificationWordingTest
{
    private static final long NOW = 1_800_000_000L;

    private static BirdhouseTimer timer(BirdhouseSite site)
    {
        return BirdhouseTimer.parse(site, "3:" + (NOW - 3000), NOW);
    }

    @Test
    public void approvedMessageIsExact()
    {
        assertEquals("Birdhouse Timer: Your birdhouse traps may now be ready to reset.",
            BirdhouseWarningMessage.READY_TEXT);
    }

    @Test
    public void oneOrFourReadyTimersProduceSameSingleMessage()
    {
        String one = BirdhouseWarningMessage.formatReady(Collections.singletonList(timer(BirdhouseSite.MEADOW_NORTH)));
        String many = BirdhouseWarningMessage.formatReady(Arrays.asList(
            timer(BirdhouseSite.MEADOW_SOUTH), timer(BirdhouseSite.MEADOW_NORTH),
            timer(BirdhouseSite.VALLEY_SOUTH), timer(BirdhouseSite.VALLEY_NORTH)));
        assertEquals(BirdhouseWarningMessage.READY_TEXT, one);
        assertEquals(one, many);
    }
}
