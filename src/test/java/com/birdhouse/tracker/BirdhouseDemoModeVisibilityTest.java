package com.birdhouse.tracker;

import java.lang.reflect.Method;
import net.runelite.client.config.ConfigItem;
import org.junit.Test;
import static org.junit.Assert.*;

/** BH-DEC-001: public configuration cannot enable the developer-only preview. */
public class BirdhouseDemoModeVisibilityTest
{
    @Test
    public void demoModeIsAbsentFromPublicConfiguration() throws Exception
    {
        for (Method method : BirdhouseTrackerConfig.class.getDeclaredMethods())
        {
            assertNotEquals("demoMode", method.getName());
            ConfigItem item = method.getAnnotation(ConfigItem.class);
            if (item != null)
            {
                assertNotEquals("demoMode", item.keyName());
                assertFalse(item.name().toLowerCase(java.util.Locale.ROOT).contains("demo mode"));
            }
        }
    }

    @Test
    public void developerControlsAreNotPartOfPlayerConfiguration()
    {
        for (Method method : BirdhouseTrackerConfig.class.getMethods())
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
