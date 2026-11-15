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
    static final String TITLE = "Birdhouse Timers";
    static final String DEMO_TITLE = TITLE + " [DM]";
    static final String TIME_TRACKING_INSTRUCTION_1 = "1. Open Configuration (wrench)";
    static final String TIME_TRACKING_INSTRUCTION_2 = "2. Search Time Tracking";
    static final String TIME_TRACKING_INSTRUCTION_3 = "3. Enable Time Tracking";
    static final Color READY_COLOR = new Color(0, 255, 0); // Bright green to match RuneLite Farming READY text.
    static final Color SOON_COLOR = new Color(255, 220, 50);
    static final Color GROWING_COLOR = new Color(255, 80, 80);
    static final Color DEPENDENCY_WARNING_COLOR = new Color(255, 190, 75);

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
            .text(config.demoMode() ? DEMO_TITLE : TITLE)
            .build());

        boolean dependencyWarning = TimeTrackingDependencyStatus.shouldShowOverlayWarning(
            config.demoMode(), plugin.isTimeTrackingStatusKnownForOverlay(),
            plugin.isTimeTrackingActiveForOverlay());
        if (dependencyWarning)
        {
            panelComponent.getChildren().add(new BirdhouseDependencyWarningComponent());
            panelComponent.getChildren().add(LineComponent.builder()
                .left("To enable:")
                .build());
            panelComponent.getChildren().add(LineComponent.builder()
                .left(TIME_TRACKING_INSTRUCTION_1)
                .build());
            panelComponent.getChildren().add(LineComponent.builder()
                .left(TIME_TRACKING_INSTRUCTION_2)
                .build());
            panelComponent.getChildren().add(LineComponent.builder()
                .left(TIME_TRACKING_INSTRUCTION_3)
                .build());
        }

        if (shouldShowEmptyState(timers.isEmpty(), dependencyWarning))
        {
            panelComponent.getChildren().add(LineComponent.builder()
                .left(EMPTY_STATE_MESSAGE)
                .build());
            return super.render(graphics);
        }
        if (timers.isEmpty())
        {
            // A dependency warning already explains why no current timers are shown.
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
                .leftColor(Color.WHITE)
                .right(timer.display(now))
                .rightColor(statusColor)
                .build());
        }
        return super.render(graphics);
    }

    static boolean shouldShowEmptyState(boolean timersEmpty, boolean dependencyWarning)
    {
        return timersEmpty && !dependencyWarning;
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
