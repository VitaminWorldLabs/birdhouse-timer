package com.birdhouse.tracker;

import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/** Stable identity, terminal state and serialization safeguards. */
public class BirdhousePlantingGroupPersistenceTest
{
    private static final long BASE = 1_800_000_000L;

    private static BirdhouseTimer timer(BirdhouseSite site, long offsetSeconds)
    {
        long plantedAt = BASE + offsetSeconds;
        return BirdhouseTimer.parse(site, "3:" + plantedAt, plantedAt);
    }

    @Test
    public void groupRoundTripsMembershipDeadlineAndChannelState()
    {
        BirdhousePlantingGroup original = BirdhousePlantingGroup.create(Arrays.asList(
            timer(BirdhouseSite.MEADOW_SOUTH, 0), timer(BirdhouseSite.MEADOW_NORTH, 180)), BASE + 200);
        original = original.withChatOutcome(BirdhousePlantingGroup.ChannelOutcome.ATTEMPTED)
            .withDesktopOutcome(BirdhousePlantingGroup.ChannelOutcome.FAILED);

        BirdhousePlantingGroup restored = BirdhousePlantingGroup.restore(original.id, original.serialize());
        assertNotNull(restored);
        assertEquals(original.id, restored.id);
        assertEquals(original.cycleKeys, restored.cycleKeys);
        assertEquals(original.deadline(), restored.deadline());
        assertEquals(BirdhousePlantingGroup.ChannelOutcome.ATTEMPTED, restored.chatOutcome);
        assertEquals(BirdhousePlantingGroup.ChannelOutcome.FAILED, restored.desktopOutcome);
    }


    @Test
    public void groupIdentityIsDeterministicWhenPlantingTimesTie()
    {
        BirdhouseTimer south = timer(BirdhouseSite.MEADOW_SOUTH, 0);
        BirdhouseTimer north = timer(BirdhouseSite.MEADOW_NORTH, 0);
        BirdhousePlantingGroup forward = BirdhousePlantingGroup.create(Arrays.asList(south, north), BASE);
        BirdhousePlantingGroup reverse = BirdhousePlantingGroup.create(Arrays.asList(north, south), BASE);
        assertEquals(forward.id, reverse.id);
        assertEquals(2, forward.cycleKeys.size());
        assertEquals(forward.cycleKeys, reverse.cycleKeys);
    }

    @Test
    public void terminalGroupsCannotChangeMembership()
    {
        BirdhousePlantingGroup group = BirdhousePlantingGroup.create(
            Collections.singletonList(timer(BirdhouseSite.MEADOW_SOUTH, 0)), BASE);
        BirdhousePlantingGroup dispatched = group.terminal(BirdhousePlantingGroup.Status.DISPATCHED);
        try
        {
            dispatched.withMembers(Arrays.asList(group.cycleKeys.get(0),
                timer(BirdhouseSite.MEADOW_NORTH, 60).cycleKey));
            fail("Terminal membership must be immutable");
        }
        catch (IllegalStateException expected)
        {
            assertEquals(BirdhousePlantingGroup.Status.DISPATCHED, dispatched.status);
        }
    }

    @Test
    public void statusAndChannelOutcomesPersistIndependently()
    {
        BirdhousePlantingGroup group = BirdhousePlantingGroup.create(
            Collections.singletonList(timer(BirdhouseSite.MEADOW_SOUTH, 0)), BASE)
            .terminal(BirdhousePlantingGroup.Status.DISPATCHED)
            .withChatOutcome(BirdhousePlantingGroup.ChannelOutcome.ATTEMPTED)
            .withDesktopOutcome(BirdhousePlantingGroup.ChannelOutcome.DISABLED);
        BirdhousePlantingGroup restored = BirdhousePlantingGroup.restore(group.id, group.serialize());
        assertEquals(BirdhousePlantingGroup.Status.DISPATCHED, restored.status);
        assertEquals(BirdhousePlantingGroup.ChannelOutcome.ATTEMPTED, restored.chatOutcome);
        assertEquals(BirdhousePlantingGroup.ChannelOutcome.DISABLED, restored.desktopOutcome);
    }
}
