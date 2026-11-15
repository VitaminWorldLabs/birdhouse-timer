package com.birdhouse.tracker;

import net.runelite.client.config.Notification;
import net.runelite.client.config.RequestFocusType;
import org.junit.Test;
import static org.junit.Assert.*;

/** Regression: desktop READY must obey RuneLite's global focused-notification preference. */
public class BirdhouseDesktopFocusPreferenceTest
{
    @Test
    public void desktopNotificationSuppressesWhileFocusedWhenGlobalPreferenceIsOff()
    {
        Notification notification = BirdhouseNotificationDelivery.desktopNotification(false);
        assertFalse(notification.isSendWhenFocused());
        assertTrue(notification.isTray());
        assertFalse(notification.isGameMessage());
        assertEquals(RequestFocusType.OFF, notification.getRequestFocus());
    }

    @Test
    public void desktopNotificationAllowsFocusedDeliveryWhenGlobalPreferenceIsOn()
    {
        Notification notification = BirdhouseNotificationDelivery.desktopNotification(true);
        assertTrue(notification.isSendWhenFocused());
        assertTrue(notification.isTray());
        assertFalse(notification.isGameMessage());
    }
}
