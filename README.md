# Birdhouse Timer

**Version:** 1.2.0

Birdhouse Timer displays four independent estimated countdowns for the Fossil Island birdhouse traps in a compact, movable RuneLite overlay.

## Features

- Four independent countdowns using RuneLite's built-in **Time Tracking** birdhouse records.
- Compact overlay (approximately 190px wide) with the locations in this order: Mushroom Meadow South, Mushroom Meadow North, Verdant Valley SW, Verdant Valley NE.
- Red growing state, configurable yellow visual warning, and bright-green **READY** (`#00FF00`) for each individual timer.
- Visual advance-warning choices: **Off / 5 / 10 / 15 minutes**. The selected threshold changes the timer color only; no advance chat or desktop notifications are sent.
- One consolidated READY notification per planting group. Planting cycles are ordered by planting time; consecutive gaps of five minutes or less remain in one group, while a gap greater than five minutes starts another group. The notification is attempted when the latest planting cycle in the group reaches its estimated 50-minute completion time.
- Independent READY preferences for red in-game chat and desktop notifications. Both default to On.
- A Time Tracking dependency warning with setup guidance.

## Estimated readiness

READY means the standard **50-minute estimate** has elapsed. It does not prove that a birdhouse is full or immediately harvestable. A READY timer remains visible until RuneLite Time Tracking observes a relevant change. The plugin does not impose an arbitrary expiration.

Planting groups use consecutive planting-time gaps. For example, plantings at 10:00, 10:03 and 10:07 form one group with an estimated completion at 10:57. A planting at 10:15 starts another group with an estimated completion at 11:05.

## Notifications and missed events

- Earlier birdhouses in a group turn READY individually but do not send individual READY notifications.
- One notification attempt is made per enabled channel when the latest timer in that group reaches its estimated READY time.
- Chat and desktop delivery are independent. A disabled or failed channel does not trigger a duplicate through the other channel.
- Desktop delivery respects RuneLite's focused-notification preference. Windows notification permissions and Do Not Disturb settings may still suppress a visible desktop banner.
- If RuneLite is closed when a group becomes overdue, that notification is skipped. No catch-up notification is sent after restart.
- When Time Tracking is disabled, new READY dispatch is suppressed. Groups that become overdue during the interruption are skipped during reconciliation; no delayed catch-up notification is sent.
- Notification attempts are tracked independently from whether Windows visibly displays a banner. There are no automatic retries or repeated reminders.

## Required RuneLite plugin: Time Tracking

Birdhouse Timer reads per-account birdhouse records maintained by RuneLite's built-in **Time Tracking** plugin. It does not plant, harvest or reset traps, and it does not overwrite Time Tracking's underlying records.

Keep Time Tracking enabled so planting and reset changes can be recorded. RuneLite may reconcile birdhouse state changes only in supported Fossil Island areas. Saved records can be temporarily absent, stale or delayed after login, world changes, restarts or dependency interruptions. The plugin detects whether Time Tracking is active; it cannot independently detect every internal tracking failure while RuneLite still reports the plugin as active.

The plugin excludes records it can identify as empty or unseeded. It cannot guarantee that every saved seeded record corresponds to a currently constructed physical birdhouse. Treat saved countdowns and maturity estimates as estimates, not authoritative proof of harvestability.

## Configuration migration

The previous player-facing Demo Mode option is no longer available. On startup, the plugin clears only the obsolete `demoMode` preference. Existing birdhouse records, timer data and the separate READY chat and desktop preferences are preserved.

## Support

Report issues at: https://github.com/VitaminWorldLabs/birdhouse-timer/issues

## Disclaimer

Third-party RuneLite plugin. Not affiliated with or endorsed by Jagex or RuneLite.
