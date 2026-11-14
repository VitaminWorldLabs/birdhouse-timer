package com.birdhouse.tracker;

import org.junit.Test;
import static org.junit.Assert.*;

/** Focused visual-state regression tests for v0.1.5. */
public class BirdhousePresentationTest
{
    @Test
    public void emptyStateHasExactPlayerRequestedMessage()
    {
        assertEquals("No active birdhouses", BirdhouseOverlay.EMPTY_STATE_MESSAGE);
    }

    @Test
    public void colorTransitionsOccurAtTenMinutesAndMaturity()
    {
        assertEquals(BirdhouseOverlay.GROWING_COLOR, BirdhouseOverlay.statusColor(3_000, WarningLeadTime.TEN));
        assertEquals(BirdhouseOverlay.GROWING_COLOR, BirdhouseOverlay.statusColor(601, WarningLeadTime.TEN));
        assertEquals(BirdhouseOverlay.SOON_COLOR, BirdhouseOverlay.statusColor(600, WarningLeadTime.TEN));
        assertEquals(BirdhouseOverlay.SOON_COLOR, BirdhouseOverlay.statusColor(1, WarningLeadTime.TEN));
        assertEquals(BirdhouseOverlay.READY_COLOR, BirdhouseOverlay.statusColor(0, WarningLeadTime.TEN));
        assertEquals(BirdhouseOverlay.READY_COLOR, BirdhouseOverlay.statusColor(-100, WarningLeadTime.TEN));
    }

    @Test
    public void colorsAreDistinctAndUseStrongRedBrightYellowAndGreen()
    {
        assertEquals(new java.awt.Color(255, 80, 80), BirdhouseOverlay.GROWING_COLOR);
        assertEquals(new java.awt.Color(255, 220, 50), BirdhouseOverlay.SOON_COLOR);
        assertEquals(new java.awt.Color(110, 230, 130), BirdhouseOverlay.READY_COLOR);
        assertNotEquals(BirdhouseOverlay.GROWING_COLOR, BirdhouseOverlay.SOON_COLOR);
        assertNotEquals(BirdhouseOverlay.SOON_COLOR, BirdhouseOverlay.READY_COLOR);
    }

    @Test
    public void yellowFollowsEachConfiguredAdvanceThresholdExactly()
    {
        for (WarningLeadTime lead : new WarningLeadTime[] {
            WarningLeadTime.FIVE, WarningLeadTime.TEN, WarningLeadTime.FIFTEEN})
        {
            long boundary = lead.minutes * 60L;
            assertEquals(BirdhouseOverlay.GROWING_COLOR,
                BirdhouseOverlay.statusColor(boundary + 1, lead));
            assertEquals(BirdhouseOverlay.SOON_COLOR,
                BirdhouseOverlay.statusColor(boundary, lead));
            assertEquals(BirdhouseOverlay.SOON_COLOR,
                BirdhouseOverlay.statusColor(1, lead));
            assertEquals(BirdhouseOverlay.READY_COLOR,
                BirdhouseOverlay.statusColor(0, lead));
        }
    }

    @Test
    public void offNeverShowsYellowButReadyRemainsGreen()
    {
        for (long remaining : new long[] {3_000, 900, 600, 300, 1})
        {
            assertEquals(BirdhouseOverlay.GROWING_COLOR,
                BirdhouseOverlay.statusColor(remaining, WarningLeadTime.OFF));
        }
        assertEquals(BirdhouseOverlay.READY_COLOR,
            BirdhouseOverlay.statusColor(0, WarningLeadTime.OFF));
        assertEquals(BirdhouseOverlay.READY_COLOR,
            BirdhouseOverlay.statusColor(-3_600, WarningLeadTime.OFF));
    }

    @Test
    public void changingSettingUpdatesColorWithoutChangingTimer()
    {
        long remaining = 12 * 60;
        assertEquals(BirdhouseOverlay.GROWING_COLOR,
            BirdhouseOverlay.statusColor(remaining, WarningLeadTime.FIVE));
        assertEquals(BirdhouseOverlay.GROWING_COLOR,
            BirdhouseOverlay.statusColor(remaining, WarningLeadTime.TEN));
        assertEquals(BirdhouseOverlay.SOON_COLOR,
            BirdhouseOverlay.statusColor(remaining, WarningLeadTime.FIFTEEN));
        assertEquals(BirdhouseOverlay.GROWING_COLOR,
            BirdhouseOverlay.statusColor(remaining, WarningLeadTime.OFF));
    }

    @Test
    public void compactWidthRemainsUnchanged()
    {
        assertEquals(190, BirdhouseOverlay.COMPACT_WIDTH_PX);
    }
}
