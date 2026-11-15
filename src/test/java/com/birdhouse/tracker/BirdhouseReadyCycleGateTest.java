package com.birdhouse.tracker;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/** Regression: one READY dispatch per site and persisted cycle, not per game tick. */
public class BirdhouseReadyCycleGateTest
{
    @Test
    public void repeatedEvaluationsDoNotDispatchSameSiteCycleAgain()
    {
        Map<Integer, String> persistedMarkers = new HashMap<>();
        int siteVarp = 100;
        String cycle = "9:1800000000";

        assertTrue(BirdhouseReadyCycleGate.isNewCycle(persistedMarkers.get(siteVarp), cycle));
        persistedMarkers.put(siteVarp, cycle); // Production persists before dispatch.
        assertFalse(BirdhouseReadyCycleGate.isNewCycle(persistedMarkers.get(siteVarp), cycle));
        assertFalse(BirdhouseReadyCycleGate.isNewCycle(persistedMarkers.get(siteVarp), cycle));
    }

    @Test
    public void separateSitesHaveIndependentReadyMarkers()
    {
        Map<Integer, String> persistedMarkers = new HashMap<>();
        String cycle = "9:1800000000";
        persistedMarkers.put(100, cycle);

        assertFalse(BirdhouseReadyCycleGate.isNewCycle(persistedMarkers.get(100), cycle));
        assertTrue(BirdhouseReadyCycleGate.isNewCycle(persistedMarkers.get(200), cycle));
    }

    @Test
    public void aNewReplantCycleCanNotifyAgain()
    {
        String oldCycle = "9:1800000000";
        String newCycle = "9:1800003000";
        assertTrue(BirdhouseReadyCycleGate.isNewCycle(oldCycle, newCycle));
    }

    @Test
    public void nullCycleCannotTriggerNotification()
    {
        assertFalse(BirdhouseReadyCycleGate.isNewCycle(null, null));
    }
}
