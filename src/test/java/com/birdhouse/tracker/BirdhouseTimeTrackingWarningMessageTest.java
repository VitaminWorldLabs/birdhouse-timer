package com.birdhouse.tracker;

import org.junit.Test;
import static org.junit.Assert.*;

public class BirdhouseTimeTrackingWarningMessageTest
{
    @Test
    public void warningIsActionableAndIdentifiesDependency()
    {
        assertTrue(BirdhouseNotificationDelivery.TIME_TRACKING_DISABLED_TEXT.contains("Time Tracking is disabled"));
        assertTrue(BirdhouseNotificationDelivery.TIME_TRACKING_DISABLED_TEXT.contains("Configuration"));
        assertTrue(BirdhouseNotificationDelivery.TIME_TRACKING_DISABLED_TEXT.contains("search \"Time Tracking\""));
        assertTrue(BirdhouseNotificationDelivery.TIME_TRACKING_DISABLED_TEXT.contains("enable"));
    }

    @Test
    public void dependencyWarningIsNotTheReadyNotification()
    {
        assertNotEquals(BirdhouseWarningMessage.READY_TEXT,
            BirdhouseNotificationDelivery.TIME_TRACKING_DISABLED_TEXT);
    }
}
