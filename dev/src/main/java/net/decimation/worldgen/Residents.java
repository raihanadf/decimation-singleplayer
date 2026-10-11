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
}
