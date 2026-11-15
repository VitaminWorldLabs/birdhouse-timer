# Birdhouse Timer

Birdhouse Timer is a RuneLite plugin for Old School RuneScape that displays independent estimated countdowns for the four Fossil Island birdhouse traps.

## Features

- A compact **190px overlay** showing Mushroom Meadow South, Mushroom Meadow North, Verdant Valley SW and Verdant Valley NE in that order.
- A separate estimated timer for each seeded trap, based on the built-in RuneLite Time Tracking records.
- Red (growing), yellow (inside the selected advance-warning window), and bright-green **READY** (the standard 50-minute estimate has elapsed). READY is an estimate, not confirmation that a trap is full or harvestable.
- Configurable advance warnings: **Off / 5 / 10 / 15 minutes**.
- Grouped, per-cycle READY alerts through red in-game chat and desktop notification, using: **"Birdhouse Timer: Your birdhouse traps may now be ready to reset."**
- Independent READY delivery preferences for red in-game chat and desktop notifications. Both default to On. Turning both off does not affect the overlay or advance warnings.
- Optional visual-only Demo Mode, available in the plugin configuration. It previews four simulated countdowns and does not change real tracking data or send notifications.

## Required RuneLite plugin: Time Tracking

Birdhouse Timer reads the saved birdhouse records maintained by RuneLite's built-in **Time Tracking** plugin. Enable Time Tracking in RuneLite for current planting and reset changes to be recorded.

If Time Tracking is disabled or fails to start, Birdhouse Timer keeps displaying the last saved records where available, but those estimates may become out of date. The overlay displays an amber warning triangle, **Time Tracking Disabled**, and three setup instructions. Birdhouse Timer also sends one actionable red in-game warning per inactive period. The warning marker persists across client restarts and resets when Time Tracking becomes active again. Re-enabling Time Tracking removes the overlay warning and resumes normal observation. Birdhouse Timer does not fabricate timestamps or modify Time Tracking's records.

While Time Tracking is inactive, saved timers and their advance/READY notifications continue to be calculated from the last saved records and may be stale. Treat those countdowns and alerts as uncertain until Time Tracking is active and has observed the relevant game state. The warning does not guarantee recovery of missed planting or reset events. RuneLite may reconcile birdhouse varp changes only in supported Fossil Island areas; a missed same-tier reset may remain stale until the built-in tracker observes a detectable change.

## Notifications

READY preferences are independent:

- **READY: Red in-game chat** (default On)
- **READY: Windows desktop** (default On)

The advance-warning setting is separate. If both READY channels are disabled, the overlay and advance warnings continue normally. READY markers are tracked per location and planted cycle, so enabling a channel again during an already-notified cycle does not replay that cycle.

Desktop READY notifications follow RuneLite's global **Send notifications when focused** setting at the moment they are sent. When the setting is Off and RuneLite is focused, desktop delivery is suppressed while red chat remains independent. Windows notification permissions and Do Not Disturb can also affect desktop presentation.

Traps first observed READY within the same fixed 10-second coalescing window are consolidated into one chat/desktop dispatch. This can delay the first alert by up to 10 seconds. Events separated by more than that window are delivered in separate batches. A blocked notification after dispatch is not automatically retried during the same cycle.

## Demo Mode

Demo Mode is a visual-only preview of four simulated countdown states. The overlay title is **Birdhouse Timers** and is marked **Birdhouse Timers [DM]** in Demo Mode (`DM` indicates the visual Demo Mode preview). Demo Mode does not require Time Tracking, does not read or modify real birdhouse records or notification markers, and does not send advance, READY, or dependency-warning messages.

## How tracking works

Birdhouse Timer reads RuneLite's saved per-account birdhouse Time Tracking values. It does not interact with the game or plant/reset traps. It uses the standard **50-minute estimate**. Actual birdhouse completion can vary, so READY does not guarantee that the trap is harvestable. Notification markers are stored separately per location and planted cycle to suppress duplicates.

## Installation

Install **Birdhouse Timer** from RuneLite's Plugin Hub using the normal RuneLite client. Enable the built-in Time Tracking plugin, then configure advance warnings, READY channels, and Demo Mode as desired.

## Source and support

https://github.com/VitaminWorldLabs/birdhouse-timer

## Disclaimer

Third-party RuneLite plugin, not affiliated with or endorsed by Jagex or RuneLite.
