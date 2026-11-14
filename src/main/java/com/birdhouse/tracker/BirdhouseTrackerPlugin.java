package com.birdhouse.tracker;

import com.google.inject.Provides;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

/**
 * Read-only display of RuneLite's built-in Time Tracking birdhouse records.
 * The only saved records this plugin writes are its own notification markers.
 */
@PluginDescriptor(
    name = "Birdhouse Timer",
    description = "Visible planted birdhouse countdowns and advance warnings. "
        + "READY: The standard 50-minute birdhouse timer has elapsed. The birdhouse is estimated to be ready for harvesting, but completion is not guaranteed.",
    tags = {"birdhouse", "hunter", "fossilisland", "timer"}
)
public class BirdhouseTrackerPlugin extends Plugin
{
    private static final String SOURCE_GROUP = "timetracking";
    private static final String OWN_GROUP = "birdhousetracker";

    @Inject private Client client;
    @Inject private ConfigManager configManager;
    @Inject private BirdhouseTrackerConfig config;
    @Inject private OverlayManager overlayManager;
    @Inject private BirdhouseOverlay overlay;
    @Inject private Notifier notifier;
    @Inject private BirdhouseNotificationDelivery notificationDelivery;

    private volatile List<BirdhouseTimer> timers = Collections.emptyList();
    private volatile List<BirdhouseTimer> demoTimers = Collections.emptyList();

    @Provides
    BirdhouseTrackerConfig provideConfig(ConfigManager manager)
    {
        return manager.getConfig(BirdhouseTrackerConfig.class);
    }

    @Override
    protected void startUp()
    {
        demoTimers = Collections.emptyList();
        overlayManager.add(overlay);
        refresh();
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        timers = Collections.emptyList();
        demoTimers = Collections.emptyList();
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        refresh();
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() != GameState.LOGGED_IN)
        {
            timers = Collections.emptyList();
        }
        else
        {
            refresh();
        }
    }

    List<BirdhouseTimer> getTimers()
    {
        if (!config.demoMode())
        {
            // Clearing the preview makes a later re-enable start fresh.
            demoTimers = Collections.emptyList();
            return timers;
        }

        List<BirdhouseTimer> preview = demoTimers;
        if (preview.isEmpty())
        {
            preview = BirdhouseTimer.demoTimers(Instant.now().getEpochSecond());
            demoTimers = preview;
        }
        return preview;
    }

    private void refresh()
    {
        // Demo mode is strictly visual: do not read/write account timers or
        // issue real game notifications while the preview is enabled.
        if (config.demoMode())
        {
            timers = Collections.emptyList();
            return;
        }
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            timers = Collections.emptyList();
            return;
        }

        long now = Instant.now().getEpochSecond();
        List<BirdhouseTimer> active = new ArrayList<>(4);
        List<BirdhouseTimer> newlyWarned = new ArrayList<>(4);
        List<BirdhouseTimer> newlyReady = new ArrayList<>(4);
        int lead = config.warningLeadTime().minutes;
        for (BirdhouseSite site : BirdhouseSite.displayOrder())
        {
            String saved = configManager.getRSProfileConfiguration(
                SOURCE_GROUP, "birdhouse." + site.varp);
            BirdhouseTimer timer = BirdhouseTimer.parse(site, saved, now);
            if (timer == null)
            {
                continue;
            }
            active.add(timer);
            long remaining = timer.secondsRemaining(now);
            // A separate per-site 50-minute READY marker ensures that READY and
            // advance-warning notifications never suppress one another.
            // The cycle key changes after replanting.
            if (remaining <= 0)
            {
                String readyKey = "ready." + site.varp;
                String lastReadyCycle = configManager.getRSProfileConfiguration(OWN_GROUP, readyKey);
                if (!timer.cycleKey.equals(lastReadyCycle))
                {
                    // Persist first, matching the existing warning behavior.
                    configManager.setRSProfileConfiguration(OWN_GROUP, readyKey, timer.cycleKey);
                    newlyReady.add(timer);
                }
            }
            else if (lead > 0 && remaining <= lead * 60L)
            {
                String warningKey = "warned." + site.varp;
                String lastCycle = configManager.getRSProfileConfiguration(OWN_GROUP, warningKey);
                if (!timer.cycleKey.equals(lastCycle))
                {
                    // Persist before notifying to avoid spamming on relaunch.
                    configManager.setRSProfileConfiguration(OWN_GROUP, warningKey, timer.cycleKey);
                    newlyWarned.add(timer);
                }
            }
        }
        timers = Collections.unmodifiableList(active);
        if (!newlyWarned.isEmpty())
        {
            notifier.notify(BirdhouseWarningMessage.format(newlyWarned, lead));
        }
        if (!newlyReady.isEmpty())
        {
            notificationDelivery.sendReady();
        }
    }
}
