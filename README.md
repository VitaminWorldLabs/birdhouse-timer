# Birdhouse Timer

A RuneLite plugin for **Old School RuneScape** that presents independent countdowns for the four Fossil Island birdhouse traps and alerts when the standard **50-minute estimate** has elapsed.

## Features

* One compact **190px overlay**, showing Mushroom Meadow South, Mushroom Meadow North, Verdant Valley SW and Verdant Valley NE in that order.
* A distinct timer per trap, based on **RuneLite Time Tracking** stored birdhouse records. Empty and unseeded traps are excluded.
* Estimated countdown state: growing (red), approaching READY (yellow), and READY (green). A READY estimate does **not** prove the trap is actually ready or full.
* Advance warnings configurable as **Off / 5 / 10 / 15 minutes**. Off disables advance warnings, not READY alerts.
* A grouped, per-cycle READY notification via red RuneLite chat and desktop notification with approved text: **"Birdhouse Timer: Your birdhouse traps may now be ready to reset."**
* **Demo mode** previews four independent timers without modifying gameplay tracking or sending notifications.

## How it works

Birdhouse Timer reads RuneLite's existing per-account birdhouse tracking values. It does **not** interact with the game or plant/reset traps. Actual timer availability depends on RuneLite Time Tracking recording a seeded trap. Timer data can be temporarily absent, stale, or delayed after login or world changes.

The READY message uses the standard elapsed **50-minute estimate**. It is not a confirmed status signal from the game. Notification history is tracked per planted cycle, and duplicates are suppressed. The plugin stores only its own warning/READY deduplication markers, not gameplay timer records.

## Notifications

The current public candidate always requests both READY channels: a red in-game RuneLite chat notification and a desktop notification. Visibility can also depend on RuneLite notification preferences, focus settings, Windows notification permissions and Do Not Disturb. The plugin's optional Demo mode is visual only and does not fire real notifications.

If an alert is blocked or cannot be displayed, the current implementation does not automatically retry that cycle. Windows desktop behavior requires acceptance testing on a Windows client.

## Building

Java 11 and Gradle 8.10 are recommended. From the source project directory, run:

```sh
gradle clean test build
```

If you already have a verified Gradle wrapper in another local development project, you can invoke that wrapper using `-p` to target this project; see the separate `WINDOWS\_ACCEPTANCE.md` in the delivery package. The standard Plugin Hub build replaces local Gradle configuration, so no custom build steps or external dependencies are required.

## Plugin Hub

The project contains `runelite-plugin.properties` with `build=standard`. Public author attribution: VitaminWorld. Licensed under BSD 2-Clause. This is a **private production-preparation candidate** and is not yet published or approved for distribution.

Source repository and support links: to be added after founder approval.

## Disclaimer

Third-party RuneLite plugin, not affiliated with or endorsed by Jagex or RuneLite.
