package com.birdhouse.tracker;

import java.util.List;
import java.util.stream.Collectors;

/** One readable notification for all locations entering a warning window together. */
final class BirdhouseWarningMessage
{
    static final String READY_TEXT =
        "Birdhouse Timer: Your birdhouse traps may now be ready to reset.";

    private BirdhouseWarningMessage()
    {
    }

    static String format(List<BirdhouseTimer> newlyWarned, int leadMinutes)
    {
        if (newlyWarned == null || newlyWarned.isEmpty())
        {
            throw new IllegalArgumentException("At least one birdhouse is required");
        }
        if (leadMinutes <= 0)
        {
            throw new IllegalArgumentException("Warning lead time must be positive");
        }
        String locations = newlyWarned.stream()
            .map(timer -> timer.site.displayLabel())
            .collect(Collectors.joining(", "));
        return (newlyWarned.size() == 1 ? "Birdhouse" : "Birdhouses")
            + " ready within " + leadMinutes + " minutes: " + locations + ".";
    }

    /** One approved estimated READY message for a single coalesced event batch. */
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
