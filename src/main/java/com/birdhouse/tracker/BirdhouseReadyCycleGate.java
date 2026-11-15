package com.birdhouse.tracker;

/** Pure per-location cycle guard used before persisting and dispatching READY alerts. */
final class BirdhouseReadyCycleGate
{
    private BirdhouseReadyCycleGate()
    {
    }

    static boolean isNewCycle(String previouslyNotifiedCycle, String currentCycle)
    {
        return currentCycle != null && !currentCycle.equals(previouslyNotifiedCycle);
    }
}
