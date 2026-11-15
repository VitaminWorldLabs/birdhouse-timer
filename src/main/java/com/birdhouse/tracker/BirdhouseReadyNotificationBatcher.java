package com.birdhouse.tracker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Coalesces READY transitions first observed within a short fixed window. */
final class BirdhouseReadyNotificationBatcher
{
    static final long WINDOW_SECONDS = 10L;

    private final Map<String, BirdhouseTimer> pending = new LinkedHashMap<>();
    private long deadlineSeconds = Long.MIN_VALUE;

    void add(BirdhouseTimer timer, long observedAtSeconds)
    {
        if (timer == null)
        {
            return;
        }
        String key = timer.site.varp + ":" + timer.cycleKey;
        if (pending.isEmpty())
        {
            deadlineSeconds = observedAtSeconds + WINDOW_SECONDS;
        }
        pending.putIfAbsent(key, timer);
    }

    boolean isDue(long nowSeconds)
    {
        return !pending.isEmpty() && nowSeconds >= deadlineSeconds;
    }

    List<BirdhouseTimer> drain()
    {
        if (pending.isEmpty())
        {
            return java.util.Collections.emptyList();
        }
        List<BirdhouseTimer> result = new ArrayList<>(pending.values());
        pending.clear();
        deadlineSeconds = Long.MIN_VALUE;
        return result;
    }

    void clear()
    {
        pending.clear();
        deadlineSeconds = Long.MIN_VALUE;
    }

    int pendingCount()
    {
        return pending.size();
    }
}
