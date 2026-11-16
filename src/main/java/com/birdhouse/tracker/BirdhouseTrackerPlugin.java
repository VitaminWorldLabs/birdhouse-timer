package com.birdhouse.tracker;

import com.google.inject.Provides;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PluginChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.timetracking.TimeTrackingPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read-only display of RuneLite's built-in Time Tracking birdhouse records.
 * The plugin writes only its own warning, migration and notification metadata.
 */
@PluginDescriptor(
    name = "Birdhouse Timer",
    description = "Four independent estimated birdhouse countdowns with one READY reminder per five-minute planting group. "
        + "READY: The standard 50-minute estimate has elapsed; actual harvestability is not guaranteed.",
    tags = {"birdhouse", "hunter", "fossilisland", "timer"}
)
public class BirdhouseTrackerPlugin extends Plugin
{
    private static final Logger log = LoggerFactory.getLogger(BirdhouseTrackerPlugin.class);
    private static final String SOURCE_GROUP = "timetracking";
    private static final String OWN_GROUP = "birdhousetracker";
    private static final String TIME_TRACKING_WARNING_SENT_KEY = "timeTrackingDisabledWarningSent";
    private static final String GROUP_CONFIG_PREFIX = "run.";
    private static final String LAST_OBSERVED_EPOCH_KEY = "runLastObservedEpoch";
    private static final long CLOCK_PERSIST_INTERVAL_SECONDS = 60L;

    @Inject private Client client;
    @Inject private ConfigManager configManager;
    @Inject private PluginManager pluginManager;
    @Inject private BirdhouseTrackerConfig config;
    @Inject private OverlayManager overlayManager;
    @Inject private BirdhouseOverlay overlay;
    @Inject private BirdhouseNotificationDelivery notificationDelivery;

    private final TimeTrackingDependencyStatus timeTrackingStatus = new TimeTrackingDependencyStatus();
    private final BirdhouseClockGuard clockGuard = new BirdhouseClockGuard();
    private TimeTrackingPlugin timeTrackingPlugin;
    private volatile boolean loggedIn;
    private boolean deferDependencyWarningUntilFirstGameTick;
    private boolean startupReconciliationPending = true;
    private boolean trackingWasActive;
    private long lastPersistedObservedEpoch = Long.MIN_VALUE;

    private volatile List<BirdhouseTimer> timers = Collections.emptyList();

    @Provides
    BirdhouseTrackerConfig provideConfig(ConfigManager manager)
    {
        return manager.getConfig(BirdhouseTrackerConfig.class);
    }

    @Override
    protected void startUp()
    {
        // BH-DEC-001: remove only the obsolete public preference. The plugin no longer
        // reads this value, so a previous demoMode=true cannot enable simulation.
        configManager.unsetConfiguration(OWN_GROUP, "demoMode");

        timeTrackingPlugin = pluginManager.getPlugins().stream()
            .filter(TimeTrackingPlugin.class::isInstance)
            .map(TimeTrackingPlugin.class::cast)
            .findFirst()
            .orElse(null);
        loggedIn = client.getGameState() == GameState.LOGGED_IN;
        deferDependencyWarningUntilFirstGameTick = true;
        startupReconciliationPending = true;
        trackingWasActive = false;
        clockGuard.reset();
        lastPersistedObservedEpoch = parseEpoch(configManager.getRSProfileConfiguration(
            OWN_GROUP, LAST_OBSERVED_EPOCH_KEY));
        overlayManager.add(overlay);
        refresh();
    }

    @Override
    protected void shutDown()
    {
        overlayManager.remove(overlay);
        timers = Collections.emptyList();
        if (loggedIn)
        {
            persistObservedEpoch(Math.max(Instant.now().getEpochSecond(), lastPersistedObservedEpoch));
        }
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        updateTimeTrackingStatus(isTimeTrackingActive(), !deferDependencyWarningUntilFirstGameTick);
        deferDependencyWarningUntilFirstGameTick = false;
        refresh();
    }

    @Subscribe
    public void onPluginChanged(PluginChanged event)
    {
        if (event.getPlugin() == timeTrackingPlugin && !deferDependencyWarningUntilFirstGameTick)
        {
            boolean active = event.isLoaded() && isTimeTrackingActive();
            updateTimeTrackingStatus(active, true);
            // Record the transition immediately. A disable/enable pair can occur
            // between GameTicks; relying only on polling could otherwise miss the
            // outage and dispatch an overdue group as though tracking stayed active.
            trackingWasActive = false;
            startupReconciliationPending = true;
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        loggedIn = event.getGameState() == GameState.LOGGED_IN;
        if (!loggedIn)
        {
            timers = Collections.emptyList();
            startupReconciliationPending = true;
            trackingWasActive = false;
            clockGuard.reset();
        }
        else
        {
            if (!deferDependencyWarningUntilFirstGameTick)
            {
                updateTimeTrackingStatus(isTimeTrackingActive(), true);
            }
            refresh();
        }
    }

    private boolean isTimeTrackingActive()
    {
        return timeTrackingPlugin != null && pluginManager.isPluginActive(timeTrackingPlugin);
    }

    private void updateTimeTrackingStatus(boolean active, boolean allowInitialWarning)
    {
        boolean wasInitialized = timeTrackingStatus.isInitialized();
        boolean wasActive = timeTrackingStatus.isActive();
        if (active && (!wasInitialized || !wasActive))
        {
            // Reset only the dependency-warning latch for a new active episode.
            configManager.setConfiguration(OWN_GROUP, TIME_TRACKING_WARNING_SENT_KEY, false);
        }

        boolean mayWarn = allowInitialWarning && loggedIn;
        String warningSentValue = configManager.getConfiguration(OWN_GROUP, TIME_TRACKING_WARNING_SENT_KEY);
        boolean warningAlreadySent = Boolean.parseBoolean(warningSentValue);
        if (timeTrackingStatus.update(active, mayWarn, warningAlreadySent))
        {
            configManager.setConfiguration(OWN_GROUP, TIME_TRACKING_WARNING_SENT_KEY, true);
            notificationDelivery.sendTimeTrackingDisabledWarning();
        }
    }

    boolean isTimeTrackingStatusKnownForOverlay()
    {
        return timeTrackingStatus.isInitialized();
    }

    boolean isTimeTrackingActiveForOverlay()
    {
        return timeTrackingStatus.isActive();
    }

    List<BirdhouseTimer> getTimers()
    {
        return timers;
    }

    private void refresh()
    {
        if (!loggedIn || client.getGameState() != GameState.LOGGED_IN)
        {
            timers = Collections.emptyList();
            return;
        }

        long wallNow = Instant.now().getEpochSecond();
        long monotonicNow = System.nanoTime();
        boolean clockDiscontinuity = clockGuard.observe(wallNow, monotonicNow);
        long effectiveNow = Math.max(wallNow, lastPersistedObservedEpoch);
        // Use the monotonic effective time floor when validating future timestamps.
        // A backward system-clock adjustment must not make a previously valid
        // Time Tracking record disappear from the overlay/group reconciliation.
        List<BirdhouseTimer> currentTimers = readSavedTimers(effectiveNow);
        timers = Collections.unmodifiableList(currentTimers);

        List<BirdhousePlantingGroup> previousGroups = loadGroups();
        boolean trackingActive = isTimeTrackingActive();
        if (!trackingActive)
        {
            // Saved timers remain visible, but no new READY dispatch is allowed.
            // Persist SKIPPED for any group whose estimated deadline passes during
            // the inactive period so reactivation cannot produce a catch-up alert.
            List<BirdhousePlantingGroup> skipped = BirdhouseGroupReconciler.skipOverdue(previousGroups, effectiveNow);
            persistGroups(previousGroups, skipped);
            trackingWasActive = false;
            persistObservedEpoch(effectiveNow);
            return;
        }

        boolean reconcileOnly = startupReconciliationPending || !trackingWasActive;
        BirdhouseGroupReconciler.Result result = BirdhouseGroupReconciler.reconcile(
            previousGroups, currentTimers, effectiveNow);
        List<BirdhousePlantingGroup> nextGroups = new ArrayList<>(result.groups);

        for (int i = 0; i < nextGroups.size(); i++)
        {
            BirdhousePlantingGroup group = nextGroups.get(i);
            BirdhouseGroupDeadlinePolicy.Action action = BirdhouseGroupDeadlinePolicy.evaluate(
                group, effectiveNow, result.newlyObservedGroupIds.contains(group.id),
                reconcileOnly, clockDiscontinuity, wallNow);
            if (action == BirdhouseGroupDeadlinePolicy.Action.WAIT)
            {
                continue;
            }
            if (action == BirdhouseGroupDeadlinePolicy.Action.SKIP)
            {
                nextGroups.set(i, group.terminal(BirdhousePlantingGroup.Status.SKIPPED));
            }
            else
            {
                nextGroups.set(i, group.terminal(BirdhousePlantingGroup.Status.DISPATCHED));
                // Persist terminal state before dispatch. A process interruption
                // must not replay an attempted group on the next tick or restart.
                persistGroups(previousGroups, nextGroups);
                nextGroups = attemptGroupChannels(nextGroups, i);
                previousGroups = new ArrayList<>(nextGroups);
            }
        }

        persistGroups(previousGroups, nextGroups);
        startupReconciliationPending = false;
        trackingWasActive = true;
        persistObservedEpoch(effectiveNow);
    }

    private List<BirdhouseTimer> readSavedTimers(long nowSeconds)
    {
        List<BirdhouseTimer> active = new ArrayList<>(4);
        for (BirdhouseSite site : BirdhouseSite.displayOrder())
        {
            String saved = configManager.getRSProfileConfiguration(
                SOURCE_GROUP, "birdhouse." + site.varp);
            BirdhouseTimer timer = BirdhouseTimer.parse(site, saved, nowSeconds);
            if (timer != null)
            {
                active.add(timer);
            }
        }
        return active;
    }

    private List<BirdhousePlantingGroup> loadGroups()
    {
        String profile = configManager.getRSProfileKey();
        if (profile == null)
        {
            return new ArrayList<>();
        }

        List<BirdhousePlantingGroup> groups = new ArrayList<>();
        List<String> keys = configManager.getRSProfileConfigurationKeys(
            OWN_GROUP, profile, GROUP_CONFIG_PREFIX);
        for (String key : keys)
        {
            String encodedId = key.substring(GROUP_CONFIG_PREFIX.length());
            String id = BirdhousePlantingGroup.decodeConfigKey(encodedId);
            String value = configManager.getRSProfileConfiguration(OWN_GROUP, key);
            BirdhousePlantingGroup group = BirdhousePlantingGroup.restore(id, value);
            if (group != null)
            {
                groups.add(group);
            }
        }
        return groups;
    }

    private void persistGroups(List<BirdhousePlantingGroup> previous,
        List<BirdhousePlantingGroup> current)
    {
        Set<String> currentIds = new HashSet<>();
        for (BirdhousePlantingGroup group : current)
        {
            currentIds.add(group.id);
            String key = GROUP_CONFIG_PREFIX + BirdhousePlantingGroup.encodeConfigKey(group.id);
            configManager.setRSProfileConfiguration(OWN_GROUP, key, group.serialize());
        }

        for (BirdhousePlantingGroup group : previous)
        {
            if (!currentIds.contains(group.id) && group.isPending())
            {
                configManager.unsetRSProfileConfiguration(OWN_GROUP,
                    GROUP_CONFIG_PREFIX + BirdhousePlantingGroup.encodeConfigKey(group.id));
            }
        }
    }

    private List<BirdhousePlantingGroup> attemptGroupChannels(List<BirdhousePlantingGroup> groups, int index)
    {
        List<BirdhousePlantingGroup> updated = new ArrayList<>(groups);
        BirdhousePlantingGroup group = updated.get(index);
        boolean chatEnabled = config.readyRedChatEnabled();
        boolean desktopEnabled = config.readyDesktopEnabled();

        // Mark enabled channels ATTEMPTING before invoking either delivery path.
        // DISABLED is terminal too: changing a preference later does not replay a group.
        group = group.withChatOutcome(chatEnabled
            ? BirdhousePlantingGroup.ChannelOutcome.ATTEMPTING
            : BirdhousePlantingGroup.ChannelOutcome.DISABLED);
        group = group.withDesktopOutcome(desktopEnabled
            ? BirdhousePlantingGroup.ChannelOutcome.ATTEMPTING
            : BirdhousePlantingGroup.ChannelOutcome.DISABLED);
        updated.set(index, group);
        persistGroups(updated, updated);

        // Use the same independent channel gate covered by regression tests. Each
        // callback records its own outcome; a failed channel cannot block the other.
        BirdhouseNotificationDelivery.dispatchReady(chatEnabled, desktopEnabled,
            () -> attemptChatChannel(updated, index),
            () -> attemptDesktopChannel(updated, index));
        return updated;
    }

    private void attemptChatChannel(List<BirdhousePlantingGroup> groups, int index)
    {
        BirdhousePlantingGroup group = groups.get(index);
        try
        {
            notificationDelivery.sendRedChat();
            group = group.withChatOutcome(BirdhousePlantingGroup.ChannelOutcome.ATTEMPTED);
        }
        catch (RuntimeException ex)
        {
            group = group.withChatOutcome(BirdhousePlantingGroup.ChannelOutcome.FAILED);
        }
        groups.set(index, group);
        persistGroups(groups, groups);
    }

    private void attemptDesktopChannel(List<BirdhousePlantingGroup> groups, int index)
    {
        BirdhousePlantingGroup group = groups.get(index);
        try
        {
            notificationDelivery.sendDesktop();
            group = group.withDesktopOutcome(BirdhousePlantingGroup.ChannelOutcome.ATTEMPTED);
        }
        catch (RuntimeException ex)
        {
            group = group.withDesktopOutcome(BirdhousePlantingGroup.ChannelOutcome.FAILED);
        }
        groups.set(index, group);
        persistGroups(groups, groups);
    }

    private void persistObservedEpoch(long epochSeconds)
    {
        if (epochSeconds <= lastPersistedObservedEpoch
            || lastPersistedObservedEpoch != Long.MIN_VALUE
            && epochSeconds - lastPersistedObservedEpoch < CLOCK_PERSIST_INTERVAL_SECONDS)
        {
            return;
        }
        configManager.setRSProfileConfiguration(OWN_GROUP, LAST_OBSERVED_EPOCH_KEY, epochSeconds);
        lastPersistedObservedEpoch = epochSeconds;
    }

    private static long parseEpoch(String value)
    {
        if (value == null)
        {
            return Long.MIN_VALUE;
        }
        try
        {
            return Long.parseLong(value);
        }
        catch (NumberFormatException ex)
        {
            return Long.MIN_VALUE;
        }
    }
}
