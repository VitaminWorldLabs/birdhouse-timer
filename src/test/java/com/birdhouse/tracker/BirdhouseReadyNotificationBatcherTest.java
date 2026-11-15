package com.birdhouse.tracker;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/** Fixed-window grouping tests; no RuneLite client is required. */
public class BirdhouseReadyNotificationBatcherTest
{
    private static final long NOW = 1_800_000_000L;

    private static BirdhouseTimer timer(BirdhouseSite site, long plantedAt)
    {
        return BirdhouseTimer.parse(site, "3:" + plantedAt, NOW);
    }

    @Test
    public void firstEventIsHeldForAtMostTenSeconds()
    {
        BirdhouseReadyNotificationBatcher batcher = new BirdhouseReadyNotificationBatcher();
        batcher.add(timer(BirdhouseSite.MEADOW_SOUTH, NOW - 3000), NOW);
        assertFalse(batcher.isDue(NOW + 9));
        assertTrue(batcher.isDue(NOW + 10));
    }

    @Test
    public void slightlyStaggeredSitesShareOneBatch()
    {
        BirdhouseReadyNotificationBatcher batcher = new BirdhouseReadyNotificationBatcher();
        batcher.add(timer(BirdhouseSite.MEADOW_SOUTH, NOW - 3000), NOW);
        batcher.add(timer(BirdhouseSite.MEADOW_NORTH, NOW - 3001), NOW + 8);
        assertFalse(batcher.isDue(NOW + 9));
        assertTrue(batcher.isDue(NOW + 10));
        List<BirdhouseTimer> batch = batcher.drain();
        assertEquals(2, batch.size());
        assertEquals(0, batcher.pendingCount());
    }

    @Test
    public void duplicateSameSiteSameCycleIsCoalesced()
    {
        BirdhouseReadyNotificationBatcher batcher = new BirdhouseReadyNotificationBatcher();
        BirdhouseTimer first = timer(BirdhouseSite.MEADOW_SOUTH, NOW - 3000);
        batcher.add(first, NOW);
        batcher.add(first, NOW + 1);
        assertEquals(1, batcher.pendingCount());
    }

    @Test
    public void eventsAfterWindowStartANewBatch()
    {
        BirdhouseReadyNotificationBatcher batcher = new BirdhouseReadyNotificationBatcher();
        batcher.add(timer(BirdhouseSite.MEADOW_SOUTH, NOW - 3000), NOW);
        assertEquals(1, batcher.drain().size());
        batcher.add(timer(BirdhouseSite.MEADOW_NORTH, NOW - 3001), NOW + 11);
        assertFalse(batcher.isDue(NOW + 20));
        assertTrue(batcher.isDue(NOW + 21));
    }

    @Test
    public void newBatcherHasNoPersistedPendingState()
    {
        BirdhouseReadyNotificationBatcher first = new BirdhouseReadyNotificationBatcher();
        first.add(timer(BirdhouseSite.MEADOW_SOUTH, NOW - 3000), NOW);
        BirdhouseReadyNotificationBatcher afterRestart = new BirdhouseReadyNotificationBatcher();
        assertEquals(0, afterRestart.pendingCount());
        // Production cycle markers are deliberately written only on dispatch,
        // allowing a restart during the pending window to rediscover the cycle.
        assertEquals(NOW + 10, NOW + BirdhouseReadyNotificationBatcher.WINDOW_SECONDS);
    }
}
