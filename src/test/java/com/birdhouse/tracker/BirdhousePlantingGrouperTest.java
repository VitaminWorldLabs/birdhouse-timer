package com.birdhouse.tracker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/** Exact rolling five-minute consecutive-gap grouping behavior. */
public class BirdhousePlantingGrouperTest
{
    private static final long BASE = 1_800_000_000L;

    private static BirdhouseTimer timer(BirdhouseSite site, long offsetSeconds)
    {
        long plantedAt = BASE + offsetSeconds;
        return BirdhouseTimer.parse(site, "3:" + plantedAt, plantedAt);
    }

    @Test
    public void exampleProducesTwoGroupsAndExpectedDeadlines()
    {
        List<List<BirdhouseTimer>> groups = BirdhousePlantingGrouper.group(Arrays.asList(
            timer(BirdhouseSite.MEADOW_NORTH, 15 * 60),
            timer(BirdhouseSite.MEADOW_SOUTH, 7 * 60),
            timer(BirdhouseSite.VALLEY_NORTH, 3 * 60),
            timer(BirdhouseSite.VALLEY_SOUTH, 0)));

        assertEquals(2, groups.size());
        assertEquals(3, groups.get(0).size());
        assertEquals(BASE + 57 * 60L,
            groups.get(0).get(groups.get(0).size() - 1).plantedAt + BirdhouseTimer.MATURITY_SECONDS);
        assertEquals(1, groups.get(1).size());
        assertEquals(BASE + 65 * 60L,
            groups.get(1).get(0).plantedAt + BirdhouseTimer.MATURITY_SECONDS);
    }

    @Test
    public void exactFiveMinuteGapRemainsInSameGroup()
    {
        List<List<BirdhouseTimer>> groups = BirdhousePlantingGrouper.group(Arrays.asList(
            timer(BirdhouseSite.MEADOW_NORTH, 5 * 60),
            timer(BirdhouseSite.MEADOW_SOUTH, 0)));
        assertEquals(1, groups.size());
        assertEquals(2, groups.get(0).size());
    }

    @Test
    public void gapGreaterThanFiveMinutesStartsAnotherGroup()
    {
        List<List<BirdhouseTimer>> groups = BirdhousePlantingGrouper.group(Arrays.asList(
            timer(BirdhouseSite.MEADOW_NORTH, 5 * 60 + 1),
            timer(BirdhouseSite.MEADOW_SOUTH, 0)));
        assertEquals(2, groups.size());
    }

    @Test
    public void rollingGroupCanSpanMoreThanFiveMinutes()
    {
        List<List<BirdhouseTimer>> groups = BirdhousePlantingGrouper.group(Arrays.asList(
            timer(BirdhouseSite.MEADOW_NORTH, 12 * 60),
            timer(BirdhouseSite.MEADOW_SOUTH, 8 * 60),
            timer(BirdhouseSite.VALLEY_NORTH, 4 * 60),
            timer(BirdhouseSite.VALLEY_SOUTH, 0)));
        assertEquals(1, groups.size());
        assertEquals(4, groups.get(0).size());
    }

    @Test
    public void discoveryOrderDoesNotChangeGrouping()
    {
        List<BirdhouseTimer> values = Arrays.asList(
            timer(BirdhouseSite.MEADOW_NORTH, 7 * 60),
            timer(BirdhouseSite.MEADOW_SOUTH, 0),
            timer(BirdhouseSite.VALLEY_NORTH, 3 * 60));
        List<List<BirdhouseTimer>> forward = BirdhousePlantingGrouper.group(values);
        List<List<BirdhouseTimer>> reverse = BirdhousePlantingGrouper.group(Arrays.asList(
            values.get(2), values.get(0), values.get(1)));
        assertEquals(1, forward.size());
        assertEquals(1, reverse.size());
        assertEquals(forward.get(0).get(0).cycleKey, reverse.get(0).get(0).cycleKey);
        assertEquals(forward.get(0).get(2).cycleKey, reverse.get(0).get(2).cycleKey);
    }


    @Test
    public void sameTierAndSameSecondAtDifferentSitesRemainDistinctCycles()
    {
        BirdhouseTimer south = timer(BirdhouseSite.MEADOW_SOUTH, 0);
        BirdhouseTimer north = timer(BirdhouseSite.MEADOW_NORTH, 0);

        assertNotEquals(south.cycleKey, north.cycleKey);
        assertEquals(south.plantedAt, north.plantedAt);
        assertEquals(1, BirdhousePlantingGrouper.group(Arrays.asList(south, north)).size());
        assertEquals(2, BirdhousePlantingGroup.create(Arrays.asList(south, north), BASE).cycleKeys.size());
        assertEquals(south.site.varp, BirdhousePlantingGroup.siteVarp(south.cycleKey));
        assertEquals(north.site.varp, BirdhousePlantingGroup.siteVarp(north.cycleKey));
        assertEquals(BASE, BirdhousePlantingGroup.plantedAt(south.cycleKey));
    }

    @Test
    public void emptyInputProducesNoGroups()
    {
        assertTrue(BirdhousePlantingGrouper.group(Collections.<BirdhouseTimer>emptyList()).isEmpty());
    }
}
