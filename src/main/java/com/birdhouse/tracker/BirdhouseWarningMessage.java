package com.birdhouse.tracker;

import java.util.List;

/** Player-facing text for consolidated READY delivery. Advance warnings are visual only. */
final class BirdhouseWarningMessage
{
    static final String READY_TEXT =
        "Birdhouse Timer: Your birdhouse traps may now be ready to reset.";

    private BirdhouseWarningMessage()
    {
    }

    /** One approved estimated READY message for one completed planting group. */
    static String formatReady(List<BirdhouseTimer> newlyReady)
    {
        if (newlyReady == null || newlyReady.isEmpty())
        {
            throw new IllegalArgumentException("At least one ready birdhouse is required");
        }
        // Approved single message for one or more newly READY sites.
        // Site grouping and per-cycle markers are handled by the plugin.
        return READY_TEXT;
    }
}
