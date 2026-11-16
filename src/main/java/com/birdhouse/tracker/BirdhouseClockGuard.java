package com.birdhouse.tracker;

/** Detects material wall-clock discontinuities while the client is running. */
final class BirdhouseClockGuard
{
    static final long DISCONTINUITY_TOLERANCE_SECONDS = 120L;

    private long previousWallSeconds = Long.MIN_VALUE;
    private long previousMonotonicNanos = Long.MIN_VALUE;

    boolean observe(long wallSeconds, long monotonicNanos)
    {
        if (previousWallSeconds == Long.MIN_VALUE || previousMonotonicNanos == Long.MIN_VALUE)
        {
            previousWallSeconds = wallSeconds;
            previousMonotonicNanos = monotonicNanos;
            return false;
        }

        long wallElapsed = wallSeconds - previousWallSeconds;
        long monotonicElapsed = Math.max(0L, (monotonicNanos - previousMonotonicNanos) / 1_000_000_000L);
        long difference = Math.abs(wallElapsed - monotonicElapsed);
        previousWallSeconds = wallSeconds;
        previousMonotonicNanos = monotonicNanos;
        return difference > DISCONTINUITY_TOLERANCE_SECONDS;
    }

    void reset()
    {
        previousWallSeconds = Long.MIN_VALUE;
        previousMonotonicNanos = Long.MIN_VALUE;
    }
}
