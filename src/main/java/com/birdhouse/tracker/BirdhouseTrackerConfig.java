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
        description = "Preview four simulated birdhouse countdowns. This is a visual-only preview; it does not change real tracking data or send notifications.",
        position = 1
    )
    default boolean demoMode()
    {
        return false;
    }

    @ConfigItem(
        keyName = "readyRedChatEnabled",
        name = "READY: Red in-game chat",
        description = "Send the red in-game chat notification when a birdhouse reaches its estimated READY time.",
        position = 2
    )
    default boolean readyRedChatEnabled()
    {
        return true;
    }

    @ConfigItem(
        keyName = "readyDesktopEnabled",
        name = "READY: Windows desktop",
        description = "Send the Windows desktop notification when a birdhouse reaches its estimated READY time.",
        position = 3
    )
    default boolean readyDesktopEnabled()
    {
        return true;
    }

}
