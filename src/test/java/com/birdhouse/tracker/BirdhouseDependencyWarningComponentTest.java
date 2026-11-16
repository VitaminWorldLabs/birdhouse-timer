package com.birdhouse.tracker;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import org.junit.Test;
import static org.junit.Assert.*;

public class BirdhouseDependencyWarningComponentTest
{
    @Test
    public void warningComponentRendersWithinCompactOverlayWidth()
    {
        BirdhouseDependencyWarningComponent component = new BirdhouseDependencyWarningComponent();
        BufferedImage image = new BufferedImage(190, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try
        {
            component.setPreferredLocation(new Point(0, 0));
            Dimension dimension = component.render(graphics);
            assertTrue(dimension.width < BirdhouseOverlay.COMPACT_WIDTH_PX);
            assertTrue(dimension.height > 0);
            assertEquals(dimension, component.getBounds().getSize());
        }
        finally
        {
            graphics.dispose();
        }
    }
}
