package com.birdhouse.tracker;

import java.lang.reflect.Method;
import net.runelite.client.plugins.PluginDescriptor;
import org.junit.Test;
import static org.junit.Assert.*;

/** Production-specific safeguards to prevent developer controls reappearing. */
public class BirdhouseProductionConfigurationTest
{
    @Test
    public void configExposesNoDeveloperTestControls()
    {
        for (Method method : BirdhouseTrackerConfig.class.getDeclaredMethods())
        {
            assertFalse(method.getName(), method.getName().startsWith("devTest"));
        }
    }

    @Test
    public void pluginExposesNoDeveloperTestEventHandlers()
    {
        for (Method method : BirdhouseTrackerPlugin.class.getDeclaredMethods())
        {
            assertNotEquals("onConfigChanged", method.getName());
        }
    }

    @Test
    public void pluginTagsFollowPluginHubRules()
    {
        PluginDescriptor descriptor = BirdhouseTrackerPlugin.class.getAnnotation(PluginDescriptor.class);
        assertNotNull(descriptor);
        for (String tag : descriptor.tags())
        {
            assertTrue(tag, tag.matches("[a-z0-9_-]+"));
        }
    }
}
