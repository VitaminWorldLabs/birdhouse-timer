package com.birdhouse.tracker;

import org.junit.Test;
import static org.junit.Assert.*;

public class BirdhouseClockGuardTest
{
    @Test
    public void normalWallClockAndMonotonicProgressIsAccepted()
    {
        BirdhouseClockGuard guard = new BirdhouseClockGuard();
        assertFalse(guard.observe(1_800_000_000L, 10_000_000_000L));
        assertFalse(guard.observe(1_800_000_030L, 40_000_000_000L));
    }

    @Test
    public void wallClockForwardJumpIsDetected()
    {
        BirdhouseClockGuard guard = new BirdhouseClockGuard();
        guard.observe(1_800_000_000L, 10_000_000_000L);
        assertTrue(guard.observe(1_800_001_000L, 40_000_000_000L));
    }

    @Test
    public void wallClockRollbackIsDetected()
    {
        BirdhouseClockGuard guard = new BirdhouseClockGuard();
        guard.observe(1_800_000_000L, 10_000_000_000L);
        assertTrue(guard.observe(1_799_999_900L, 40_000_000_000L));
    }
}
