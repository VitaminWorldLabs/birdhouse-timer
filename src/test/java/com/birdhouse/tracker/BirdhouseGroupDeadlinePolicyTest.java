package com.birdhouse.tracker;

import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/** Startup, outage, delayed-discovery and live deadline dispatch policy. */
public class BirdhouseGroupDeadlinePolicyTest
{
    private static final long BASE = 1_800_000_000L;

    private static BirdhousePlantingGroup group(long plantedOffset)
    {
        long plantedAt = BASE + plantedOffset;
        BirdhouseTimer timer = BirdhouseTimer.parse(BirdhouseSite.MEADOW_SOUTH,
            "3:" + plantedAt, plantedAt);
        return BirdhousePlantingGroup.create(Collections.singletonList(timer), BASE);
    }

    @Test
    public void futureGroupWaitsAtStartupAndDuringNormalRuntime()
    {
        BirdhousePlantingGroup group = group(0);
        assertEquals(BirdhouseGroupDeadlinePolicy.Action.WAIT,
            BirdhouseGroupDeadlinePolicy.evaluate(group, BASE + 49 * 60L,
                false, true, false, BASE + 49 * 60L));
        assertEquals(BirdhouseGroupDeadlinePolicy.Action.WAIT,
            BirdhouseGroupDeadlinePolicy.evaluate(group, BASE + 49 * 60L,
                false, false, false, BASE + 49 * 60L));
    }

    @Test
    public void overdueGroupIsSkippedAtStartupAndOutageReconciliation()
    {
        BirdhousePlantingGroup group = group(0);
        long now = BASE + 51 * 60L;
        assertEquals(BirdhouseGroupDeadlinePolicy.Action.SKIP,
            BirdhouseGroupDeadlinePolicy.evaluate(group, now, false, true, false, now));
    }

    @Test
    public void overdueGroupDispatchesOnlyWhenObservedWhileClientAndTrackingAreActive()
    {
        BirdhousePlantingGroup group = group(0);
        long now = BASE + 51 * 60L;
        assertEquals(BirdhouseGroupDeadlinePolicy.Action.DISPATCH,
            BirdhouseGroupDeadlinePolicy.evaluate(group, now, false, false, false, now));
    }

    @Test
    public void overdueNewlyDiscoveredGroupIsSkippedInsteadOfCaughtUp()
    {
        BirdhousePlantingGroup group = group(0);
        long now = BASE + 51 * 60L;
        assertEquals(BirdhouseGroupDeadlinePolicy.Action.SKIP,
            BirdhouseGroupDeadlinePolicy.evaluate(group, now, true, false, false, now));
    }

    @Test
    public void clockDiscontinuitySkipsOverdueGroup()
    {
        BirdhousePlantingGroup group = group(0);
        long now = BASE + 51 * 60L;
        assertEquals(BirdhouseGroupDeadlinePolicy.Action.SKIP,
            BirdhouseGroupDeadlinePolicy.evaluate(group, now, false, false, true, now));
    }
}
