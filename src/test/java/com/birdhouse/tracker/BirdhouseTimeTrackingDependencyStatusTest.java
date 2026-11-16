package com.birdhouse.tracker;

import org.junit.Test;
import static org.junit.Assert.*;

/** Dependency warning lifecycle tests; no RuneLite client or account data required. */
public class BirdhouseTimeTrackingDependencyStatusTest
{
    @Test
    public void inactiveDependencyWarnsOnceWhenLoggedIn()
    {
        TimeTrackingDependencyStatus status = new TimeTrackingDependencyStatus();
        assertFalse(status.isInitialized()); // unknown until first runtime sample
        assertFalse(status.update(false, false, false)); // defer while logged out
        assertTrue(status.update(false, true, false));
        assertFalse(status.update(false, true, true)); // persisted marker prevents repeated warnings
    }

    @Test
    public void reactivationResetsWarningForNextInactiveEpisode()
    {
        TimeTrackingDependencyStatus status = new TimeTrackingDependencyStatus();
        assertTrue(status.update(false, true, false));
        assertFalse(status.update(false, true, true));
        assertFalse(status.update(true, true, true));
        assertTrue(status.isActive());
        assertTrue(status.update(false, true, false));
        assertFalse(status.update(false, true, true));
    }

    @Test
    public void overlayWarningDependsOnlyOnKnownTrackingStatus()
    {
        assertTrue(TimeTrackingDependencyStatus.shouldShowOverlayWarning(true, false));
        assertFalse(TimeTrackingDependencyStatus.shouldShowOverlayWarning(true, true));
        assertFalse(TimeTrackingDependencyStatus.shouldShowOverlayWarning(false, false));
    }

    @Test
    public void dependencyWarningRemainsPendingUntilLoggedIn()
    {
        TimeTrackingDependencyStatus status = new TimeTrackingDependencyStatus();
        assertFalse(status.update(false, false, false));
        assertFalse(status.update(false, false, false));
        assertTrue(status.update(false, true, false));
    }

    @Test
    public void firstObservationDoesNotPretendTheDependencyIsInactive()
    {
        TimeTrackingDependencyStatus status = new TimeTrackingDependencyStatus();
        assertFalse(status.isInitialized()); // unknown until first runtime sample
        assertFalse(status.update(false, false, false));
        assertTrue(status.isInitialized());
        assertFalse(status.isActive());
    }

    @Test
    public void persistedWarningMarkerSurvivesAStatusObjectRestart()
    {
        TimeTrackingDependencyStatus firstRun = new TimeTrackingDependencyStatus();
        assertTrue(firstRun.update(false, true, false));

        TimeTrackingDependencyStatus afterRestart = new TimeTrackingDependencyStatus();
        assertFalse(afterRestart.update(false, true, true));
    }
}
