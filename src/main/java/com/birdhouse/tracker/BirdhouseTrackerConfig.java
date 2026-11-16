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
        description = "Show a yellow visual warning inside the selected threshold. No advance notification is sent. "
            + "READY: The standard 50-minute estimate has elapsed; actual harvestability is not guaranteed.",
        position = 0
    )
    default WarningLeadTime warningLeadTime()
    {
        return WarningLeadTime.TEN;
    }

    @ConfigItem(
        keyName = "readyRedChatEnabled",
        name = "READY: Red in-game chat",
        description = "Send one red in-game chat notification when a planting group reaches its estimated READY time.",
        position = 1
    )
    default boolean readyRedChatEnabled()
    {
        return true;
    }

    @ConfigItem(
        keyName = "readyDesktopEnabled",
        name = "READY: Windows desktop",
        description = "Send one Windows desktop notification when a planting group reaches its estimated READY time.",
        position = 2
    )
    default boolean readyDesktopEnabled()
    {
        return true;
    }

}
