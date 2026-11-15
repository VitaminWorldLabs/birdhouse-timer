package com.birdhouse.tracker;

import net.runelite.client.config.ConfigItem;
import org.junit.Test;
import static org.junit.Assert.*;

/** The released plugin keeps the visual-only Demo Mode available to players. */
public class BirdhouseDemoModeVisibilityTest
{
    @Test
    public void demoModeIsPlayerVisibleAndUsesTheExistingPublicKey() throws Exception
    {
        ConfigItem item = BirdhouseTrackerConfig.class.getMethod("demoMode").getAnnotation(ConfigItem.class);
        assertNotNull(item);
        assertFalse(item.hidden());
        assertEquals("demoMode", item.keyName());
        assertEquals("Demo mode", item.name());
        assertTrue(item.description().contains("visual-only"));
    }

    @Test
    public void demoModeDefaultsOff()
    {
        BirdhouseTrackerConfig config = new BirdhouseTrackerConfig() { };
        assertFalse(config.demoMode());
    }

    @Test
    public void developerControlsAreNotPartOfPlayerConfiguration()
    {
        for (java.lang.reflect.Method method : BirdhouseTrackerConfig.class.getMethods())
        {
            ConfigItem item = method.getAnnotation(ConfigItem.class);
            if (item != null)
            {
                assertFalse(item.name().startsWith("DEV TEST:"));
                assertFalse(item.keyName().startsWith("devTest"));
            }
        }
    }
}
