package com.birdhouse.tracker;

/** Tracks Time Tracking availability and computes one warning per inactive episode. */
final class TimeTrackingDependencyStatus
{
    private volatile boolean active;
    private volatile boolean initialized;

    /**
     * Updates dependency state and indicates whether a warning should be sent now.
     * The caller persists the warning marker so a client restart does not repeat it.
     */
    boolean update(boolean active, boolean maySendWarning, boolean warningAlreadySent)
    {
        this.active = active;
        this.initialized = true;
        return !active && maySendWarning && !warningAlreadySent;
    }

    boolean isInitialized()
    {
        return initialized;
    }

    boolean isActive()
    {
        return active;
    }

    static boolean shouldShowOverlayWarning(boolean demoMode, boolean initialized, boolean active)
    {
        return !demoMode && initialized && !active;
    }
}
