package com.birdhouse.tracker;

/** Pure decision rule for a group's estimated READY deadline. */
final class BirdhouseGroupDeadlinePolicy
{
    enum Action
    {
        WAIT,
        DISPATCH,
        SKIP
    }

    private BirdhouseGroupDeadlinePolicy()
    {
    }

    static Action evaluate(BirdhousePlantingGroup group, long effectiveNow,
        boolean newlyDiscovered, boolean reconciliationBoundary, boolean clockDiscontinuity,
        long wallNow)
    {
        if (group == null || !group.isPending() || group.deadline() > effectiveNow)
        {
            return Action.WAIT;
        }

        // A deadline first observed as overdue is never caught up. This includes
        // startup after RuneLite was closed, Time Tracking reactivation, and
        // material wall-clock changes while the client was running.
        if (newlyDiscovered || reconciliationBoundary || clockDiscontinuity
            || wallNow < effectiveNow)
        {
            return Action.SKIP;
        }

        return Action.DISPATCH;
    }
}
