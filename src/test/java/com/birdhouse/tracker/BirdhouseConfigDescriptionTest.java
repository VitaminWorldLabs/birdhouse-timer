package com.birdhouse.tracker;

import net.runelite.client.config.ConfigItem;
import net.runelite.client.plugins.PluginDescriptor;
import org.junit.Test;
import static org.junit.Assert.*;

/** Founder-approved explanation must appear in the existing plugin configuration. */
public class BirdhouseConfigDescriptionTest
{
    private static final String READY_DEFINITION =
        "READY: The standard 50-minute birdhouse timer has elapsed. The birdhouse is estimated to be ready for harvesting, but completion is not guaranteed.";

    @Test
    public void exactDefinitionIsInAdvanceWarningConfigurationDescription() throws Exception
    {
        ConfigItem item = BirdhouseTrackerConfig.class.getMethod("warningLeadTime").getAnnotation(ConfigItem.class);
        assertNotNull(item);
        assertTrue(item.description().contains(READY_DEFINITION));
    }

    @Test
    public void exactDefinitionIsInPluginDescriptor()
    {
        PluginDescriptor descriptor = BirdhouseTrackerPlugin.class.getAnnotation(PluginDescriptor.class);
        assertNotNull(descriptor);
        assertTrue(descriptor.description().contains(READY_DEFINITION));
    }
}
