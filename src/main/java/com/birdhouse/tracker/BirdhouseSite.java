package com.birdhouse.tracker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.runelite.api.gameval.VarPlayerID;

/** The four Fossil Island birdhouse spaces as named by RuneLite Time Tracking. */
enum BirdhouseSite
{
    MEADOW_NORTH("Mushroom Meadow North", VarPlayerID.BIRDHOUSE_TRANSMIT_A),
    MEADOW_SOUTH("Mushroom Meadow South", VarPlayerID.BIRDHOUSE_TRANSMIT_B),
    VALLEY_NORTH("Verdant Valley NE", VarPlayerID.BIRDHOUSE_TRANSMIT_C),
    VALLEY_SOUTH("Verdant Valley SW", VarPlayerID.BIRDHOUSE_TRANSMIT_D);

    // Intentionally separate from enum declaration order so varp and persisted
    // notification identity remain stable. Never sort rows by maturity.
    private static final List<BirdhouseSite> DISPLAY_ORDER = Collections.unmodifiableList(
        Arrays.asList(MEADOW_SOUTH, MEADOW_NORTH, VALLEY_SOUTH, VALLEY_NORTH));

    final String label;
    final int varp;

    BirdhouseSite(String label, int varp)
    {
        this.label = label;
        this.varp = varp;
    }

    static List<BirdhouseSite> displayOrder()
    {
        return DISPLAY_ORDER;
    }

    /** Ceci's location label for the single compact overlay. */
    String displayLabel()
    {
        return label;
    }
}
