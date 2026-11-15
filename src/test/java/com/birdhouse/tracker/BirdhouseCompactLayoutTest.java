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
    public void overlayUsesTheApprovedTitleAndFitsTheCompactWidth()
    {
        assertEquals("Birdhouse Timers", BirdhouseOverlay.TITLE);
        assertEquals("Birdhouse Timers [DM]", BirdhouseOverlay.DEMO_TITLE);
        assertEquals(190, BirdhouseOverlay.COMPACT_WIDTH_PX);

        java.awt.Graphics2D graphics = new java.awt.image.BufferedImage(
            1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB).createGraphics();
        try
        {
            graphics.setFont(new java.awt.Font("Dialog", java.awt.Font.PLAIN, 16));
            int contentWidth = BirdhouseOverlay.COMPACT_WIDTH_PX - 8; // RuneLite's 4px border on each side.
            int titleWidth = graphics.getFontMetrics().stringWidth(BirdhouseOverlay.TITLE);
            int demoTitleWidth = graphics.getFontMetrics().stringWidth(BirdhouseOverlay.DEMO_TITLE);
            assertTrue(titleWidth <= contentWidth);
            assertTrue(demoTitleWidth <= contentWidth);
        }
        finally
        {
            graphics.dispose();
        }
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
