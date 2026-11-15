package com.birdhouse.tracker;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import net.runelite.client.config.ConfigItem;
import org.junit.Test;
import static org.junit.Assert.*;

/** v1.1.0 regression: independently selectable READY delivery channels. */
public class BirdhouseReadyChannelPreferencesTest
{
    @Test
    public void bothDefaultsAreOnAndHaveDistinctPersistedKeys() throws Exception
    {
        BirdhouseTrackerConfig defaults = new BirdhouseTrackerConfig() { };
        assertTrue(defaults.readyRedChatEnabled());
        assertTrue(defaults.readyDesktopEnabled());
        Method chat = BirdhouseTrackerConfig.class.getMethod("readyRedChatEnabled");
        Method desktop = BirdhouseTrackerConfig.class.getMethod("readyDesktopEnabled");
        ConfigItem chatItem = chat.getAnnotation(ConfigItem.class);
        ConfigItem desktopItem = desktop.getAnnotation(ConfigItem.class);
        assertNotNull(chatItem);
        assertNotNull(desktopItem);
        assertEquals("readyRedChatEnabled", chatItem.keyName());
        assertEquals("readyDesktopEnabled", desktopItem.keyName());
        assertNotEquals(chatItem.keyName(), desktopItem.keyName());
    }

    @Test
    public void bothOnSendsOneOfEach()
    {
        assertDispatch(true, true, 1, 1);
    }

    @Test
    public void chatOnlyNeverRequestsDesktop()
    {
        assertDispatch(true, false, 1, 0);
    }

    @Test
    public void desktopOnlyNeverRequestsChat()
    {
        assertDispatch(false, true, 0, 1);
    }

    @Test
    public void bothOffSendsNothing()
    {
        assertDispatch(false, false, 0, 0);
    }

    @Test
    public void chatFailureStillAttemptsDesktopWhenEnabled()
    {
        AtomicInteger desktop = new AtomicInteger();
        try
        {
            BirdhouseNotificationDelivery.dispatchReady(true, true,
                () -> { throw new IllegalStateException("chat unavailable"); }, desktop::incrementAndGet);
            fail("Expected chat delivery failure");
        }
        catch (IllegalStateException expected)
        {
            assertEquals("chat unavailable", expected.getMessage());
        }
        assertEquals(1, desktop.get());
    }

    @Test
    public void disabledChatCannotThrowOrBlockDesktop()
    {
        AtomicInteger desktop = new AtomicInteger();
        BirdhouseNotificationDelivery.dispatchReady(false, true,
            () -> { throw new AssertionError("Disabled chat must not run"); }, desktop::incrementAndGet);
        assertEquals(1, desktop.get());
    }

    private static void assertDispatch(boolean chatOn, boolean desktopOn,
        int expectedChat, int expectedDesktop)
    {
        AtomicInteger chat = new AtomicInteger();
        AtomicInteger desktop = new AtomicInteger();
        BirdhouseNotificationDelivery.dispatchReady(chatOn, desktopOn,
            chat::incrementAndGet, desktop::incrementAndGet);
        assertEquals(expectedChat, chat.get());
        assertEquals(expectedDesktop, desktop.get());
    }
}
