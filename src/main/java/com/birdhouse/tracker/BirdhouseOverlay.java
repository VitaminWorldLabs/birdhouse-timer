package com.birdhouse.tracker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.time.Instant;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/** Movable game-screen overlay for active sites, with an explicit empty state. */
final class BirdhouseOverlay extends OverlayPanel
{
    static final int COMPACT_WIDTH_PX = 190;
    static final String EMPTY_STATE_MESSAGE = "No active birdhouses";
    static final Color READY_COLOR = new Color(110, 230, 130);
    static final Color SOON_COLOR = new Color(255, 220, 50);
    static final Color GROWING_COLOR = new Color(255, 80, 80);

    private final BirdhouseTrackerPlugin plugin;
    private final BirdhouseTrackerConfig config;

    @Inject
    BirdhouseOverlay(BirdhouseTrackerPlugin plugin, BirdhouseTrackerConfig config)
    {
        super(plugin);
        this.plugin = plugin;
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setPriority(PRIORITY_MED);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        List<BirdhouseTimer> timers = plugin.getTimers();
        panelComponent.setPreferredSize(new Dimension(COMPACT_WIDTH_PX, 0));
        panelComponent.getChildren().add(TitleComponent.builder()
            .text(config.demoMode() ? "Birdhouses [DEMO]" : "Birdhouses")
            .build());

        if (timers.isEmpty())
        {
            panelComponent.getChildren().add(LineComponent.builder()
                .left(EMPTY_STATE_MESSAGE)
                .build());
            return super.render(graphics);
        }

        long now = Instant.now().getEpochSecond();
        for (BirdhouseTimer timer : timers)
        {
            long remaining = timer.secondsRemaining(now);
            Color statusColor = statusColor(remaining, config.warningLeadTime());
            String label = timer.site.displayLabel();
            panelComponent.getChildren().add(LineComponent.builder()
                .left(label)
                .right(timer.display(now))
                .rightColor(statusColor)
                .build());
        }
        return super.render(graphics);
    }

    /** The existing advance-warning preference also controls the yellow visual cue. */
    static Color statusColor(long remainingSeconds, WarningLeadTime leadTime)
    {
        if (remainingSeconds <= 0)
        {
            // READY denotes the 50-minute estimate, not confirmed maturity.
            return READY_COLOR;
        }
        int warningMinutes = leadTime == null ? 0 : leadTime.minutes;
        return warningMinutes > 0 && remainingSeconds <= warningMinutes * 60L
            ? SOON_COLOR : GROWING_COLOR;
    }
}
