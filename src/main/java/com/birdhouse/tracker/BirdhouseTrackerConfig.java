package com.birdhouse.tracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("birdhousetracker")
public interface BirdhouseTrackerConfig extends Config
{
    @ConfigItem(
        keyName = "warningLeadTime",
        name = "Advance warning",
        description = "Notify once when any planted birdhouse is approaching readiness. "
            + "READY: The standard 50-minute birdhouse timer has elapsed. The birdhouse is estimated to be ready for harvesting, but completion is not guaranteed.",
        position = 0
    )
    default WarningLeadTime warningLeadTime()
    {
        return WarningLeadTime.TEN;
    }

    @ConfigItem(
        keyName = "demoMode",
        name = "Demo mode",
        description = "Preview simulated birdhouse timers without planted birdhouses. No real tracking data or notifications are changed.",
        position = 1
    )
    default boolean demoMode()
    {
        return false;
    }
}
