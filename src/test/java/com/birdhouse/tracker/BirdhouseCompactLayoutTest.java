package com.birdhouse.tracker;

import java.lang.reflect.Method;
import net.runelite.client.config.ConfigItem;
import org.junit.Test;
import static org.junit.Assert.*;

/** Prevents the removed standard layout and its setting from returning. */
public class BirdhouseCompactLayoutTest
{
    @Test
    public void overlayKeepsTheApprovedCompactWidth()
    {
        assertEquals(190, BirdhouseOverlay.COMPACT_WIDTH_PX);
    }

    @Test
    public void noCompactOrStandardSwitchIsExposed()
    {
        for (Method method : BirdhouseTrackerConfig.class.getDeclaredMethods())
        {
            if (method.getName().equals("compactOverlay"))
            {
                fail("Compact overlay setting must not be exposed");
            }
            ConfigItem item = method.getAnnotation(ConfigItem.class);
            if (item != null && item.keyName().equals("compactOverlay"))
            {
                fail("Old compact overlay key must not be exposed");
            }
        }
    }

    @Test
    public void allFourLocationsUseOneCompactName()
    {
        assertEquals(4, BirdhouseSite.values().length);
        assertEquals("Mushroom Meadow North", BirdhouseSite.MEADOW_NORTH.displayLabel());
        assertEquals("Mushroom Meadow South", BirdhouseSite.MEADOW_SOUTH.displayLabel());
        assertEquals("Verdant Valley NE", BirdhouseSite.VALLEY_NORTH.displayLabel());
        assertEquals("Verdant Valley SW", BirdhouseSite.VALLEY_SOUTH.displayLabel());
    }
}
