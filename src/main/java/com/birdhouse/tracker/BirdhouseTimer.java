package com.birdhouse.tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A read-only interpretation of RuneLite's per-site Time Tracking record. */
final class BirdhouseTimer
{
    static final long MATURITY_SECONDS = 50 * 60L;
    private static final long EARLIEST_REASONABLE_TIMESTAMP = 1_500_000_000L;
    private static final long MAX_FUTURE_CLOCK_SKEW_SECONDS = 60 * 60L;

    final BirdhouseSite site;
    final int varpValue;
    final long plantedAt;
    final String cycleKey;

    private BirdhouseTimer(BirdhouseSite site, int varpValue, long plantedAt)
    {
        this.site = site;
        this.varpValue = varpValue;
        this.plantedAt = plantedAt;
        this.cycleKey = site.varp + ":" + varpValue + ":" + plantedAt;
    }

    /** Return null for empty, built-but-unseeded, missing, or invalid states. */
    static BirdhouseTimer parse(BirdhouseSite site, String stored, long nowSeconds)
    {
        if (stored == null)
        {
            return null;
        }

        String[] parts = stored.split(":", -1);
        if (parts.length != 2)
        {
            return null;
        }

        try
        {
            int varp = Integer.parseInt(parts[0]);
            long timestamp = Long.parseLong(parts[1]);
            // RuneLite: positive, every third birdhouse value means it is seeded.
            // Nine birdhouse tiers, three states per tier.
            if (varp <= 0 || varp > 27 || varp % 3 != 0
                || timestamp < EARLIEST_REASONABLE_TIMESTAMP
                || timestamp > nowSeconds + MAX_FUTURE_CLOCK_SKEW_SECONDS)
            {
                return null;
            }
            return new BirdhouseTimer(site, varp, timestamp);
        }
        catch (NumberFormatException ex)
        {
            return null;
        }
    }

    long secondsRemaining(long nowSeconds)
    {
        return plantedAt + MATURITY_SECONDS - nowSeconds;
    }

    String display(long nowSeconds)
    {
        long remaining = secondsRemaining(nowSeconds);
        if (remaining <= 0)
        {
            return "READY";
        }
        long minutes = remaining / 60;
        long seconds = remaining % 60;
        return String.format(java.util.Locale.ROOT, "%02d:%02d", minutes, seconds);
    }
}
