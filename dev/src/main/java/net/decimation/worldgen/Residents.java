package net.decimation.worldgen;

import java.util.List;

/**
 * A plan with people living in it (a safezone site): Slices hands its posts to SafezoneSites
 * together with the zone box when the plan's first slice is written.
 */
public interface Residents extends Plan
{
    /** The resident posts in world coordinates, the floor at baseY (standing at baseY + 1). */
    List<SafezoneSites.Post> residents(int baseY);

    /**
     * Cells from the site box edge to the wall line: a civilian or survivor found on or past it is
     * put back (SafezoneSites.reseat). Survivor camp: zone margin 6 + wall at 2.
     */
    default int wallInset()
    {
        return 8;
    }
}
