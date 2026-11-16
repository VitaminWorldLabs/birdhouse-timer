package com.birdhouse.tracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Reconciles persisted pending groups with currently valid seeded Time Tracking records. */
final class BirdhouseGroupReconciler
{
    private BirdhouseGroupReconciler()
    {
    }

    static Result reconcile(List<BirdhousePlantingGroup> storedGroups,
        List<BirdhouseTimer> currentTimers, long nowSeconds)
    {
        List<BirdhousePlantingGroup> groups = new ArrayList<>();
        List<BirdhousePlantingGroup> terminal = new ArrayList<>();
        Set<String> knownCycles = new HashSet<>();

        for (BirdhousePlantingGroup stored : safe(storedGroups))
        {
            if (stored.status != BirdhousePlantingGroup.Status.PENDING)
            {
                terminal.add(stored);
                knownCycles.addAll(stored.cycleKeys);
                continue;
            }

            List<String> retained = new ArrayList<>();
            boolean hasCurrentMember = false;
            for (String cycle : stored.cycleKeys)
            {
                long plantedAt = BirdhousePlantingGroup.plantedAt(cycle);
                int varp = BirdhousePlantingGroup.siteVarp(cycle);
                String currentForSite = currentCycleForSite(currentTimers, varp);
                // A matured cycle remains part of its original group while at least
                // one member of that group is still represented by the current saved
                // Time Tracking record. This preserves the group's latest deadline
                // after an earlier mature member is harvested. If every member has
                // been removed/replaced, the old group is finished locally and must
                // not emit a stale READY reminder after the player already serviced it.
                if (cycle.equals(currentForSite))
                {
                    retained.add(cycle);
                    hasCurrentMember = true;
                }
                else if (plantedAt != Long.MIN_VALUE
                    && plantedAt + BirdhouseTimer.MATURITY_SECONDS <= nowSeconds)
                {
                    retained.add(cycle);
                }
            }

            if (!hasCurrentMember && stored.deadline() > nowSeconds)
            {
                // No currently visible member is not sufficient evidence that every
                // cycle was harvested or replaced. In particular, Time Tracking can
                // provide an empty or incomplete snapshot temporarily. Preserve the
                // original pending group and its cycle identities until the latest
                // member's estimated maturity deadline; if the records return before
                // then, they can reconcile to this same group. Once the group deadline
                // is due, the existing no-catch-up policy below marks it SKIPPED.
                groups.add(stored);
                knownCycles.addAll(stored.cycleKeys);
            }
            else if (retained.isEmpty() || !hasCurrentMember)
            {
                terminal.add(stored.terminal(BirdhousePlantingGroup.Status.SKIPPED));
                knownCycles.addAll(stored.cycleKeys);
            }
            else
            {
                BirdhousePlantingGroup normalized = retained.size() == stored.cycleKeys.size()
                    ? stored : stored.withMembers(retained);
                groups.add(normalized);
                knownCycles.addAll(normalized.cycleKeys);
            }
        }

        List<BirdhouseTimer> newTimers = new ArrayList<>();
        for (BirdhouseTimer timer : safe(currentTimers))
        {
            if (!knownCycles.contains(timer.cycleKey))
            {
                newTimers.add(timer);
            }
        }

        // A graph node is one persisted pending group or one newly observed cycle.
        // Nodes join when any member pair is within the consecutive five-minute rule.
        List<Node> nodes = new ArrayList<>();
        for (BirdhousePlantingGroup group : groups)
        {
            nodes.add(Node.forGroup(group));
        }
        for (BirdhouseTimer timer : newTimers)
        {
            nodes.add(Node.forTimer(timer));
        }

        UnionFind union = new UnionFind(nodes.size());
        for (int left = 0; left < nodes.size(); left++)
        {
            for (int right = left + 1; right < nodes.size(); right++)
            {
                if (nodes.get(left).isAdjacent(nodes.get(right)))
                {
                    union.join(left, right);
                }
            }
        }

        List<BirdhousePlantingGroup> reconciled = new ArrayList<>(terminal);
        Set<String> newlyObservedGroupIds = new LinkedHashSet<>();
        boolean[] processed = new boolean[nodes.size()];
        for (int i = 0; i < nodes.size(); i++)
        {
            if (processed[i])
            {
                continue;
            }
            int root = union.find(i);
            List<BirdhousePlantingGroup> componentGroups = new ArrayList<>();
            List<BirdhouseTimer> componentTimers = new ArrayList<>();
            for (int j = i; j < nodes.size(); j++)
            {
                if (union.find(j) != root)
                {
                    continue;
                }
                processed[j] = true;
                Node node = nodes.get(j);
                if (node.group != null)
                {
                    componentGroups.add(node.group);
                }
                else
                {
                    componentTimers.add(node.timer);
                }
            }

            if (componentGroups.isEmpty())
            {
                BirdhousePlantingGroup created = BirdhousePlantingGroup.create(componentTimers, nowSeconds);
                reconciled.add(created);
                newlyObservedGroupIds.add(created.id);
                continue;
            }

            componentGroups.sort(Comparator.comparingLong(BirdhousePlantingGroup::firstPlantedAt)
                .thenComparing(group -> group.id));
            BirdhousePlantingGroup canonical = componentGroups.get(0);
            Set<String> members = new LinkedHashSet<>();
            for (BirdhousePlantingGroup group : componentGroups)
            {
                members.addAll(group.cycleKeys);
            }
            for (BirdhouseTimer timer : componentTimers)
            {
                members.add(timer.cycleKey);
            }
            reconciled.add(canonical.withMembers(members));
            if (!componentTimers.isEmpty())
            {
                // A newly observed cycle can extend an existing pending group.
                // If that group's revised deadline is already overdue, it must be
                // skipped rather than dispatched as a catch-up notification.
                newlyObservedGroupIds.add(canonical.id);
            }
        }

        reconciled.sort(Comparator.comparingLong(BirdhousePlantingGroup::firstPlantedAt)
            .thenComparing(group -> group.id));
        return new Result(reconciled, newlyObservedGroupIds);
    }

    /** Mark estimated deadlines missed at startup/outage boundaries without replay. */
    static List<BirdhousePlantingGroup> skipOverdue(List<BirdhousePlantingGroup> groups, long nowSeconds)
    {
        List<BirdhousePlantingGroup> result = new ArrayList<>();
        for (BirdhousePlantingGroup group : safe(groups))
        {
            if (group.isPending() && group.deadline() <= nowSeconds)
            {
                result.add(group.terminal(BirdhousePlantingGroup.Status.SKIPPED));
            }
            else
            {
                result.add(group);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private static String currentCycleForSite(List<BirdhouseTimer> timers, int varp)
    {
        for (BirdhouseTimer timer : safe(timers))
        {
            if (timer.site.varp == varp)
            {
                return timer.cycleKey;
            }
        }
        return null;
    }

    private static <T> List<T> safe(List<T> values)
    {
        return values == null ? Collections.<T>emptyList() : values;
    }

    static final class Result
    {
        final List<BirdhousePlantingGroup> groups;
        final Set<String> newlyObservedGroupIds;

        private Result(List<BirdhousePlantingGroup> groups, Set<String> newlyObservedGroupIds)
        {
            this.groups = Collections.unmodifiableList(new ArrayList<>(groups));
            this.newlyObservedGroupIds = Collections.unmodifiableSet(new HashSet<>(newlyObservedGroupIds));
        }
    }

    private static final class Node
    {
        final BirdhousePlantingGroup group;
        final BirdhouseTimer timer;
        final List<String> cycles;

        private Node(BirdhousePlantingGroup group, BirdhouseTimer timer, List<String> cycles)
        {
            this.group = group;
            this.timer = timer;
            this.cycles = cycles;
        }

        static Node forGroup(BirdhousePlantingGroup group)
        {
            return new Node(group, null, group.cycleKeys);
        }

        static Node forTimer(BirdhouseTimer timer)
        {
            return new Node(null, timer, Collections.singletonList(timer.cycleKey));
        }

        boolean isAdjacent(Node other)
        {
            for (String left : cycles)
            {
                long leftTime = BirdhousePlantingGroup.plantedAt(left);
                for (String right : other.cycles)
                {
                    long rightTime = BirdhousePlantingGroup.plantedAt(right);
                    if (leftTime != Long.MIN_VALUE && rightTime != Long.MIN_VALUE
                        && BirdhousePlantingGroup.cyclesAreAdjacent(leftTime, rightTime))
                    {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private static final class UnionFind
    {
        private final int[] parent;
        private final byte[] rank;

        private UnionFind(int size)
        {
            parent = new int[size];
            rank = new byte[size];
            for (int i = 0; i < size; i++)
            {
                parent[i] = i;
            }
        }

        int find(int value)
        {
            if (parent[value] != value)
            {
                parent[value] = find(parent[value]);
            }
            return parent[value];
        }

        void join(int left, int right)
        {
            int leftRoot = find(left);
            int rightRoot = find(right);
            if (leftRoot == rightRoot)
            {
                return;
            }
            if (rank[leftRoot] < rank[rightRoot])
            {
                parent[leftRoot] = rightRoot;
            }
            else if (rank[leftRoot] > rank[rightRoot])
            {
                parent[rightRoot] = leftRoot;
            }
            else
            {
                parent[rightRoot] = leftRoot;
                rank[leftRoot]++;
            }
        }
    }
}
