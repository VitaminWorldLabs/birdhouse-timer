package com.birdhouse.tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Persistent notification group for planting cycles connected by consecutive
 * planting gaps of at most five minutes. Group membership is distinct from the
 * current set of visible timers and is never inferred from event arrival order.
 */
final class BirdhousePlantingGroup
{
    static final long MAX_CONSECUTIVE_GAP_SECONDS = 5 * 60L;
    static final String SERIAL_VERSION = "1";

    enum Status
    {
        PENDING,
        DISPATCHED,
        SKIPPED
    }

    enum ChannelOutcome
    {
        NOT_ATTEMPTED,
        ATTEMPTING,
        ATTEMPTED,
        DISABLED,
        FAILED
    }

    final String id;
    final List<String> cycleKeys;
    final long createdAt;
    final Status status;
    final ChannelOutcome chatOutcome;
    final ChannelOutcome desktopOutcome;

    private BirdhousePlantingGroup(String id, List<String> cycleKeys, long createdAt,
        Status status, ChannelOutcome chatOutcome, ChannelOutcome desktopOutcome)
    {
        if (id == null || id.trim().isEmpty())
        {
            throw new IllegalArgumentException("Group ID is required");
        }
        if (cycleKeys == null || cycleKeys.isEmpty())
        {
            throw new IllegalArgumentException("A group must contain at least one planting cycle");
        }
        this.id = id;
        List<String> sorted = new ArrayList<>(new LinkedHashSet<>(cycleKeys));
        sorted.sort(Comparator.comparingLong(BirdhousePlantingGroup::plantedAt)
            .thenComparingInt(BirdhousePlantingGroup::siteVarp));
        this.cycleKeys = Collections.unmodifiableList(sorted);
        this.createdAt = createdAt;
        this.status = status;
        this.chatOutcome = chatOutcome;
        this.desktopOutcome = desktopOutcome;
    }

    static BirdhousePlantingGroup create(List<BirdhouseTimer> timers, long createdAt)
    {
        if (timers == null || timers.isEmpty())
        {
            throw new IllegalArgumentException("At least one timer is required");
        }
        List<BirdhouseTimer> sorted = new ArrayList<>(timers);
        sorted.sort(Comparator.comparingLong((BirdhouseTimer timer) -> timer.plantedAt)
            .thenComparingInt(timer -> timer.site.varp));
        String id = sorted.get(0).cycleKey;
        List<String> keys = new ArrayList<>();
        for (BirdhouseTimer timer : sorted)
        {
            keys.add(timer.cycleKey);
        }
        return new BirdhousePlantingGroup(id, keys, createdAt, Status.PENDING,
            ChannelOutcome.NOT_ATTEMPTED, ChannelOutcome.NOT_ATTEMPTED);
    }

    static BirdhousePlantingGroup restore(String id, String value)
    {
        if (id == null || value == null)
        {
            return null;
        }
        String[] parts = value.split("\\|", -1);
        if (parts.length != 6 || !SERIAL_VERSION.equals(parts[0]))
        {
            return null;
        }
        try
        {
            long createdAt = Long.parseLong(parts[1]);
            Status status = Status.valueOf(parts[2]);
            ChannelOutcome chat = ChannelOutcome.valueOf(parts[3]);
            ChannelOutcome desktop = ChannelOutcome.valueOf(parts[4]);
            List<String> members = new ArrayList<>();
            if (!parts[5].isEmpty())
            {
                Collections.addAll(members, parts[5].split(",", -1));
            }
            return new BirdhousePlantingGroup(id, members, createdAt, status, chat, desktop);
        }
        catch (IllegalArgumentException ex)
        {
            return null;
        }
    }

    String serialize()
    {
        return SERIAL_VERSION + "|" + createdAt + "|" + status.name() + "|"
            + chatOutcome.name() + "|" + desktopOutcome.name() + "|"
            + String.join(",", cycleKeys);
    }

    long firstPlantedAt()
    {
        return plantedAt(cycleKeys.get(0));
    }

    long latestPlantedAt()
    {
        return plantedAt(cycleKeys.get(cycleKeys.size() - 1));
    }

    long deadline()
    {
        return latestPlantedAt() + BirdhouseTimer.MATURITY_SECONDS;
    }

    boolean contains(String cycleKey)
    {
        return cycleKeys.contains(cycleKey);
    }

    boolean isPending()
    {
        return status == Status.PENDING;
    }

    BirdhousePlantingGroup withMembers(Set<String> members)
    {
        if (!isPending())
        {
            throw new IllegalStateException("Terminal group membership is immutable");
        }
        return new BirdhousePlantingGroup(id, new ArrayList<>(members), createdAt, status,
            chatOutcome, desktopOutcome);
    }

    BirdhousePlantingGroup withMembers(List<String> members)
    {
        if (!isPending())
        {
            throw new IllegalStateException("Terminal group membership is immutable");
        }
        return new BirdhousePlantingGroup(id, members, createdAt, status,
            chatOutcome, desktopOutcome);
    }

    BirdhousePlantingGroup withId(String newId)
    {
        if (!isPending())
        {
            throw new IllegalStateException("Terminal group identity is immutable");
        }
        return new BirdhousePlantingGroup(newId, cycleKeys, createdAt, status,
            chatOutcome, desktopOutcome);
    }

    BirdhousePlantingGroup terminal(Status newStatus)
    {
        if (newStatus == Status.PENDING)
        {
            throw new IllegalArgumentException("Terminal state is required");
        }
        if (!isPending())
        {
            return this;
        }
        return new BirdhousePlantingGroup(id, cycleKeys, createdAt, newStatus,
            chatOutcome, desktopOutcome);
    }

    BirdhousePlantingGroup withChatOutcome(ChannelOutcome outcome)
    {
        return new BirdhousePlantingGroup(id, cycleKeys, createdAt, status,
            outcome, desktopOutcome);
    }

    BirdhousePlantingGroup withDesktopOutcome(ChannelOutcome outcome)
    {
        return new BirdhousePlantingGroup(id, cycleKeys, createdAt, status,
            chatOutcome, outcome);
    }

    static String encodeConfigKey(String id)
    {
        return java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString(id.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    static String decodeConfigKey(String encoded)
    {
        try
        {
            return new String(java.util.Base64.getUrlDecoder().decode(encoded),
                java.nio.charset.StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException ex)
        {
            return null;
        }
    }

    static int siteVarp(String cycleKey)
    {
        if (cycleKey == null)
        {
            return -1;
        }
        int separator = cycleKey.indexOf(':');
        if (separator <= 0)
        {
            return -1;
        }
        try
        {
            return Integer.parseInt(cycleKey.substring(0, separator));
        }
        catch (NumberFormatException ex)
        {
            return -1;
        }
    }

    static long plantedAt(String cycleKey)
    {
        if (cycleKey == null)
        {
            return Long.MIN_VALUE;
        }
        int first = cycleKey.indexOf(':');
        int last = cycleKey.lastIndexOf(':');
        if (first < 0 || last <= first || last == cycleKey.length() - 1)
        {
            return Long.MIN_VALUE;
        }
        try
        {
            return Long.parseLong(cycleKey.substring(last + 1));
        }
        catch (NumberFormatException ex)
        {
            return Long.MIN_VALUE;
        }
    }

    static boolean cyclesAreAdjacent(long left, long right)
    {
        return Math.abs(left - right) <= MAX_CONSECUTIVE_GAP_SECONDS;
    }
}
