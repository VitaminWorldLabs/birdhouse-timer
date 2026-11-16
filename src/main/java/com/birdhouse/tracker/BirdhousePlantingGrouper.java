package com.birdhouse.tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/** Builds deterministic rolling groups from chronologically sorted planting cycles. */
final class BirdhousePlantingGrouper
{
    private BirdhousePlantingGrouper()
    {
    }

    static List<List<BirdhouseTimer>> group(List<BirdhouseTimer> timers)
    {
        if (timers == null || timers.isEmpty())
        {
            return Collections.emptyList();
        }

        List<BirdhouseTimer> sorted = new ArrayList<>(timers);
        sorted.sort(Comparator.comparingLong((BirdhouseTimer timer) -> timer.plantedAt)
            .thenComparingInt(timer -> timer.site.varp));

        List<List<BirdhouseTimer>> groups = new ArrayList<>();
        List<BirdhouseTimer> current = new ArrayList<>();
        long previousPlanting = Long.MIN_VALUE;
        for (BirdhouseTimer timer : sorted)
        {
            if (!current.isEmpty()
                && !BirdhousePlantingGroup.cyclesAreAdjacent(previousPlanting, timer.plantedAt))
            {
                groups.add(Collections.unmodifiableList(current));
                current = new ArrayList<>();
            }
            current.add(timer);
            previousPlanting = timer.plantedAt;
        }
        if (!current.isEmpty())
        {
            groups.add(Collections.unmodifiableList(current));
        }
        return Collections.unmodifiableList(groups);
    }
}
