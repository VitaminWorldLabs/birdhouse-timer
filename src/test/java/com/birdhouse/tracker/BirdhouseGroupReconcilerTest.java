package com.birdhouse.tracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/** Reconciliation tests for partial replanting, pending merges and terminal fences. */
public class BirdhouseGroupReconcilerTest
{
    private static final long BASE = 1_800_000_000L;

    private static BirdhouseTimer timer(BirdhouseSite site, long offsetSeconds)
    {
        long plantedAt = BASE + offsetSeconds;
        return BirdhouseTimer.parse(site, "3:" + plantedAt, Math.max(plantedAt, BASE + 10_000));
    }

    @Test
    public void oneToFourActiveCyclesAreRetained()
    {
        for (int count = 1; count <= 4; count++)
        {
            List<BirdhouseTimer> timers = Arrays.asList(
                timer(BirdhouseSite.MEADOW_SOUTH, 0),
                timer(BirdhouseSite.MEADOW_NORTH, 60),
                timer(BirdhouseSite.VALLEY_SOUTH, 120),
                timer(BirdhouseSite.VALLEY_NORTH, 180)).subList(0, count);
            BirdhouseGroupReconciler.Result result = BirdhouseGroupReconciler.reconcile(
                Collections.<BirdhousePlantingGroup>emptyList(), timers, BASE + 10_000);
            assertEquals(1, result.groups.size());
            assertEquals(count, result.groups.get(0).cycleKeys.size());
        }
    }


    @Test
    public void pendingGroupsRemainIndependentAcrossGapGreaterThanFiveMinutes()
    {
        BirdhouseTimer first = timer(BirdhouseSite.MEADOW_SOUTH, 0);
        BirdhouseTimer second = timer(BirdhouseSite.MEADOW_NORTH, 3 * 60);
        BirdhouseTimer third = timer(BirdhouseSite.VALLEY_SOUTH, 10 * 60);
        BirdhouseTimer fourth = timer(BirdhouseSite.VALLEY_NORTH, 13 * 60);

        BirdhouseGroupReconciler.Result result = BirdhouseGroupReconciler.reconcile(
            Collections.<BirdhousePlantingGroup>emptyList(),
            Arrays.asList(first, second, third, fourth), BASE + 20 * 60);

        assertEquals(2, result.groups.size());
        assertEquals(Arrays.asList(first.cycleKey, second.cycleKey), result.groups.get(0).cycleKeys);
        assertEquals(Arrays.asList(third.cycleKey, fourth.cycleKey), result.groups.get(1).cycleKeys);
        assertEquals(BASE + 53 * 60L, result.groups.get(0).deadline());
        assertEquals(BASE + 63 * 60L, result.groups.get(1).deadline());
    }

    @Test
    public void pendingGroupsCanMergeWhenLateCycleBridgesThem()
    {
        BirdhousePlantingGroup first = BirdhousePlantingGroup.create(
            Collections.singletonList(timer(BirdhouseSite.MEADOW_SOUTH, 0)), BASE + 1);
        BirdhousePlantingGroup second = BirdhousePlantingGroup.create(
            Collections.singletonList(timer(BirdhouseSite.VALLEY_SOUTH, 10 * 60)), BASE + 2);
        BirdhouseTimer bridge = timer(BirdhouseSite.MEADOW_NORTH, 5 * 60);

        BirdhouseGroupReconciler.Result result = BirdhouseGroupReconciler.reconcile(
            Arrays.asList(first, second), Arrays.asList(
                timer(BirdhouseSite.MEADOW_SOUTH, 0), bridge,
                timer(BirdhouseSite.VALLEY_SOUTH, 10 * 60)), BASE + 10_000);

        assertEquals(1, result.groups.size());
        assertEquals(3, result.groups.get(0).cycleKeys.size());
        assertTrue(result.newlyObservedGroupIds.contains(result.groups.get(0).id));
    }


    @Test
    public void lateDiscoveredCycleJoiningOverduePendingGroupIsSkippedNotCaughtUp()
    {
        BirdhouseTimer first = timer(BirdhouseSite.MEADOW_SOUTH, 0);
        BirdhousePlantingGroup existing = BirdhousePlantingGroup.create(
            Collections.singletonList(first), BASE);
        // This cycle was planted four minutes later but is only discovered after
        // the extended group's estimated completion deadline has passed.
        BirdhouseTimer late = timer(BirdhouseSite.MEADOW_NORTH, 4 * 60);
        long now = BASE + 55 * 60L;
        BirdhouseGroupReconciler.Result result = BirdhouseGroupReconciler.reconcile(
            Collections.singletonList(existing), Arrays.asList(first, late), now);
        assertEquals(1, result.groups.size());
        BirdhousePlantingGroup group = result.groups.get(0);
        assertTrue(group.contains(late.cycleKey));
        assertTrue(result.newlyObservedGroupIds.contains(group.id));
        assertEquals(BirdhouseGroupDeadlinePolicy.Action.SKIP,
            BirdhouseGroupDeadlinePolicy.evaluate(group, now,
                result.newlyObservedGroupIds.contains(group.id), false, false, now));
    }

    @Test
    public void terminalGroupIsNeverMergedByLateCycle()
    {
        BirdhousePlantingGroup terminal = BirdhousePlantingGroup.create(
            Collections.singletonList(timer(BirdhouseSite.MEADOW_SOUTH, 0)), BASE)
            .terminal(BirdhousePlantingGroup.Status.DISPATCHED);
        BirdhouseTimer late = timer(BirdhouseSite.MEADOW_NORTH, 4 * 60);

        BirdhouseGroupReconciler.Result result = BirdhouseGroupReconciler.reconcile(
            Collections.singletonList(terminal), Arrays.asList(
                timer(BirdhouseSite.MEADOW_SOUTH, 0), late), BASE + 10_000);

        assertEquals(2, result.groups.size());
        assertEquals(BirdhousePlantingGroup.Status.DISPATCHED, result.groups.get(0).status);
        assertTrue(result.groups.stream().anyMatch(g -> g.isPending() && g.contains(late.cycleKey)));
    }

    @Test
    public void harvestedMaturedMemberRemainsPartOfItsPendingGroup()
    {
        BirdhousePlantingGroup group = BirdhousePlantingGroup.create(Arrays.asList(
            timer(BirdhouseSite.MEADOW_SOUTH, 0), timer(BirdhouseSite.MEADOW_NORTH, 180)), BASE);
        long now = BASE + 50 * 60L + 30;
        BirdhouseGroupReconciler.Result result = BirdhouseGroupReconciler.reconcile(
            Collections.singletonList(group),
            Collections.singletonList(timer(BirdhouseSite.MEADOW_NORTH, 180)), now);
        assertEquals(1, result.groups.size());
        assertEquals(2, result.groups.get(0).cycleKeys.size());
    }



    @Test
    public void overduePendingGroupIsSkippedWhileFutureGroupRemainsEligible()
    {
        BirdhousePlantingGroup overdue = BirdhousePlantingGroup.create(
            Collections.singletonList(timer(BirdhouseSite.MEADOW_SOUTH, 0)), BASE);
        BirdhousePlantingGroup future = BirdhousePlantingGroup.create(
            Collections.singletonList(timer(BirdhouseSite.MEADOW_NORTH, 20 * 60)), BASE);

        List<BirdhousePlantingGroup> result = BirdhouseGroupReconciler.skipOverdue(
            Arrays.asList(overdue, future), BASE + 51 * 60L);

        assertEquals(BirdhousePlantingGroup.Status.SKIPPED, result.get(0).status);
        assertTrue(result.get(1).isPending());
        assertEquals(BirdhousePlantingGroup.ChannelOutcome.NOT_ATTEMPTED, result.get(0).chatOutcome);
        assertEquals(BirdhousePlantingGroup.ChannelOutcome.NOT_ATTEMPTED, result.get(0).desktopOutcome);
    }

    @Test
    public void groupIsSkippedWhenEveryMemberHasBeenHarvestedOrReplaced()
    {
        BirdhousePlantingGroup group = BirdhousePlantingGroup.create(Arrays.asList(
            timer(BirdhouseSite.MEADOW_SOUTH, 0), timer(BirdhouseSite.MEADOW_NORTH, 180)), BASE);
        long now = BASE + 54 * 60L;
        BirdhouseTimer replantedSouth = timer(BirdhouseSite.MEADOW_SOUTH, 54 * 60L);
        BirdhouseTimer replantedNorth = timer(BirdhouseSite.MEADOW_NORTH, 54 * 60L + 1);

        BirdhouseGroupReconciler.Result result = BirdhouseGroupReconciler.reconcile(
            Collections.singletonList(group), Arrays.asList(replantedSouth, replantedNorth), now);

        assertTrue(result.groups.stream().anyMatch(g -> g.id.equals(group.id)
            && g.status == BirdhousePlantingGroup.Status.SKIPPED));
        assertTrue(result.groups.stream().anyMatch(g -> g.isPending()
            && g.contains(replantedSouth.cycleKey)));
    }

    @Test
    public void immatureRemovedMemberIsDroppedWithoutResettingOtherTimer()
    {
        BirdhousePlantingGroup group = BirdhousePlantingGroup.create(Arrays.asList(
            timer(BirdhouseSite.MEADOW_SOUTH, 0), timer(BirdhouseSite.MEADOW_NORTH, 180)), BASE);
        BirdhouseGroupReconciler.Result result = BirdhouseGroupReconciler.reconcile(
            Collections.singletonList(group),
            Collections.singletonList(timer(BirdhouseSite.MEADOW_NORTH, 180)), BASE + 60);
        assertEquals(1, result.groups.size());
        assertEquals(Collections.singletonList(timer(BirdhouseSite.MEADOW_NORTH, 180).cycleKey),
            result.groups.get(0).cycleKeys);
    }

    @Test
    public void temporarilyMissingTimeTrackingRecordsDoNotPermanentlySkipPendingGroup()
    {
        BirdhouseTimer south = timer(BirdhouseSite.MEADOW_SOUTH, 0);
        BirdhouseTimer north = timer(BirdhouseSite.MEADOW_NORTH, 60);
        BirdhousePlantingGroup original = BirdhousePlantingGroup.create(
            Arrays.asList(south, north), BASE);
        // The first member is mature, but the latest member's group deadline is
        // still one minute away. A temporarily empty snapshot must not skip it.
        long beforeGroupDeadline = BASE + 50 * 60L;

        BirdhouseGroupReconciler.Result duringOutage = BirdhouseGroupReconciler.reconcile(
            Collections.singletonList(original), Collections.<BirdhouseTimer>emptyList(),
            beforeGroupDeadline);
        assertEquals(1, duringOutage.groups.size());
        assertEquals(original.id, duringOutage.groups.get(0).id);
        assertEquals(BirdhousePlantingGroup.Status.PENDING, duringOutage.groups.get(0).status);
        assertEquals(original.cycleKeys, duringOutage.groups.get(0).cycleKeys);

        // An incomplete snapshot can still contain a valid record for another
        // site. That unrelated record must not make this absent group terminal.
        BirdhouseTimer unrelated = timer(BirdhouseSite.VALLEY_SOUTH, 10 * 60);
        BirdhouseGroupReconciler.Result duringPartialSnapshot = BirdhouseGroupReconciler.reconcile(
            Collections.singletonList(original), Collections.singletonList(unrelated),
            beforeGroupDeadline);
        BirdhousePlantingGroup retainedFromPartial = duringPartialSnapshot.groups.stream()
            .filter(group -> group.id.equals(original.id)).findFirst().orElse(null);
        assertTrue(retainedFromPartial != null);
        assertEquals(BirdhousePlantingGroup.Status.PENDING, retainedFromPartial.status);
        assertEquals(original.cycleKeys, retainedFromPartial.cycleKeys);

        BirdhouseGroupReconciler.Result afterRecordsReturn = BirdhouseGroupReconciler.reconcile(
            duringPartialSnapshot.groups, Arrays.asList(south, north, unrelated), beforeGroupDeadline);

        BirdhousePlantingGroup restored = afterRecordsReturn.groups.stream()
            .filter(group -> group.id.equals(original.id)).findFirst().orElse(null);
        assertTrue(restored != null);
        assertEquals(original.id, restored.id);
        assertEquals(BirdhousePlantingGroup.Status.PENDING, restored.status);
        assertEquals(original.cycleKeys, restored.cycleKeys);

        // Once the latest member's estimated deadline has passed while the
        // records are still absent, the no-catch-up rule remains authoritative.
        BirdhouseGroupReconciler.Result overdueOutage = BirdhouseGroupReconciler.reconcile(
            Collections.singletonList(original), Collections.<BirdhouseTimer>emptyList(),
            original.deadline());
        assertEquals(1, overdueOutage.groups.size());
        assertEquals(BirdhousePlantingGroup.Status.SKIPPED, overdueOutage.groups.get(0).status);
    }

    @Test
    public void newCycleAfterReplantCreatesFreshIdentity()
    {
        BirdhousePlantingGroup oldGroup = BirdhousePlantingGroup.create(
            Collections.singletonList(timer(BirdhouseSite.MEADOW_SOUTH, 0)), BASE)
            .terminal(BirdhousePlantingGroup.Status.DISPATCHED);
        BirdhouseTimer replanted = timer(BirdhouseSite.MEADOW_SOUTH, 60 * 60);
        BirdhouseGroupReconciler.Result result = BirdhouseGroupReconciler.reconcile(
            Collections.singletonList(oldGroup), Arrays.asList(
                timer(BirdhouseSite.MEADOW_SOUTH, 0), replanted), BASE + 60 * 60);
        assertEquals(2, result.groups.size());
        assertTrue(result.groups.stream().anyMatch(g -> g.isPending() && g.contains(replanted.cycleKey)));
        assertTrue(result.newlyObservedGroupIds.size() == 1);
    }
}
