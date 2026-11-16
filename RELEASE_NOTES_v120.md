# Birdhouse Timer 1.2.0

## Notification lifecycle
- Replaces individual READY notifications and the fixed 10-second batcher with chronological rolling five-minute planting groups.
- Consecutive planting gaps of 300 seconds or less remain in one group; gaps greater than 300 seconds start a new group.
- Each group uses the latest member's planting timestamp plus the standard 50-minute estimate as its completion deadline.
- Persists group membership, terminal status and independent chat/desktop notification outcomes.
- Skips groups that become overdue while RuneLite is closed or Time Tracking is disabled/unavailable. No catch-up notifications or automatic retries.
- Keeps individual countdowns and green READY states unchanged.

## Advance warnings
- Retains Off / 5 / 10 / 15-minute settings as visual-only yellow thresholds.
- Advance chat and desktop notifications are not sent.

## Configuration and dependency
- Removes the public Demo Mode option and clears the obsolete saved preference on startup.
- Preserves existing Time Tracking records and READY chat/desktop settings.
- Improves reconciliation when Time Tracking snapshots temporarily omit pending planting records.

## Readiness semantics
- READY remains a 50-minute estimate, not a guarantee of physical harvestability.
