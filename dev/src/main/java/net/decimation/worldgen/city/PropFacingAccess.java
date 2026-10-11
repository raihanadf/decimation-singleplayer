package net.decimation.worldgen.city;

import net.minecraft.block.Block;

/** Read access to the verified prop facing table (city/PropFacing) for the dev tests. */
public final class PropFacingAccess
{
    private PropFacingAccess()
    {
    }

    /** Direction 0 E, 1 S, 2 W, 3 N the front of this prop points at this metadata; -1 none / unknown. */
    public static int front(Block b, int meta)
    {
        return PropFacing.front(b, meta);
    }
}
