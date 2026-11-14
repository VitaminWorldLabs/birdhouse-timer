package com.birdhouse.tracker;

import net.runelite.api.gameval.VarPlayerID;
import org.junit.Test;
import static org.junit.Assert.*;

/** Regression: display names only, never alter the per-site RuneLite varp mapping. */
public class BirdhouseLocationLabelTest
{
    @Test
    public void allFourSitesHaveTheRequestedCompactLocationLabels()
    {
        assertEquals(4, BirdhouseSite.values().length);
        assertLabels(BirdhouseSite.VALLEY_NORTH, "Verdant Valley NE");
        assertLabels(BirdhouseSite.VALLEY_SOUTH, "Verdant Valley SW");
        assertLabels(BirdhouseSite.MEADOW_NORTH, "Mushroom Meadow North");
        assertLabels(BirdhouseSite.MEADOW_SOUTH, "Mushroom Meadow South");
    }

    private void assertLabels(BirdhouseSite site, String label)
    {
        assertEquals(label, site.displayLabel());
    }

    @Test
    public void ceciDisplayOrderIsFixedWithoutReorderingEnumIdentity()
    {
        assertEquals(java.util.Arrays.asList(
            BirdhouseSite.MEADOW_SOUTH, BirdhouseSite.MEADOW_NORTH,
            BirdhouseSite.VALLEY_SOUTH, BirdhouseSite.VALLEY_NORTH),
            BirdhouseSite.displayOrder());
        assertEquals(BirdhouseSite.MEADOW_NORTH, BirdhouseSite.values()[0]);
        try
        {
            BirdhouseSite.displayOrder().clear();
            fail("Order must not be mutable");
        }
        catch (UnsupportedOperationException expected)
        {
            // Pass.
        }
    }

    @Test
    public void siteVarpMappingsRemainUnchanged()
    {
        assertEquals(VarPlayerID.BIRDHOUSE_TRANSMIT_A, BirdhouseSite.MEADOW_NORTH.varp);
        assertEquals(VarPlayerID.BIRDHOUSE_TRANSMIT_B, BirdhouseSite.MEADOW_SOUTH.varp);
        assertEquals(VarPlayerID.BIRDHOUSE_TRANSMIT_C, BirdhouseSite.VALLEY_NORTH.varp);
        assertEquals(VarPlayerID.BIRDHOUSE_TRANSMIT_D, BirdhouseSite.VALLEY_SOUTH.varp);
    }
}
