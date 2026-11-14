package com.birdhouse.tracker;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/** RuneLite development runner used by gradlew run. */
public class BirdhouseTrackerPluginTest
{
    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(BirdhouseTrackerPlugin.class);
        RuneLite.main(args);
    }
}
