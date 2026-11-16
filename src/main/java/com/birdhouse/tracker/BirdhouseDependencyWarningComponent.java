package com.birdhouse.tracker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;

/** Compact inline amber warning triangle and label for the existing RuneLite overlay. */
final class BirdhouseDependencyWarningComponent implements LayoutableRenderableEntity
{
    static final String LABEL = "Time Tracking Disabled";
    static final Color ICON_COLOR = new Color(255, 190, 75);
    private static final Color ICON_MARK_COLOR = new Color(55, 43, 22);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final int ICON_SIZE = 11;
    private static final int GAP = 5;

    private final Rectangle bounds = new Rectangle();
    private Point preferredLocation = new Point();

    @Override
    public Dimension render(Graphics2D graphics)
    {
        FontMetrics metrics = graphics.getFontMetrics();
        int height = Math.max(metrics.getHeight(), ICON_SIZE);
        int iconX = preferredLocation.x;
        int iconY = preferredLocation.y + (height - ICON_SIZE) / 2;
        int centerX = iconX + ICON_SIZE / 2;

        Polygon triangle = new Polygon(
            new int[] {iconX, centerX, iconX + ICON_SIZE},
            new int[] {iconY + ICON_SIZE, iconY, iconY + ICON_SIZE}, 3);

        Color oldColor = graphics.getColor();
        graphics.setColor(ICON_COLOR);
        graphics.fillPolygon(triangle);
        graphics.setColor(ICON_MARK_COLOR);
        graphics.drawLine(centerX, iconY + 3, centerX, iconY + 7);
        graphics.fillOval(centerX - 1, iconY + 8, 2, 2);
        graphics.setColor(TEXT_COLOR);
        graphics.drawString(LABEL, iconX + ICON_SIZE + GAP,
            preferredLocation.y + metrics.getAscent());
        graphics.setColor(oldColor);

        int width = ICON_SIZE + GAP + metrics.stringWidth(LABEL);
        bounds.setBounds(preferredLocation.x, preferredLocation.y, width, height);
        return new Dimension(width, height);
    }

    @Override
    public Rectangle getBounds()
    {
        return bounds;
    }

    @Override
    public void setPreferredLocation(Point position)
    {
        preferredLocation = position == null ? new Point() : new Point(position);
    }

    @Override
    public void setPreferredSize(Dimension dimension)
    {
        // Width is determined by the RuneLite font metrics and compact icon.
    }
}
