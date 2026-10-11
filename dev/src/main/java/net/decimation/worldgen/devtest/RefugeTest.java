package net.decimation.worldgen.devtest;

import net.decimation.worldgen.military.CanvasPlan;
import net.decimation.worldgen.military.CityRefugePlan;

/**
 * City refuge (docs/safezone.md) built in the sky at -20000, 230, -19400 with its SAFEZONE and its
 * residents registered as the city generator does; the same checks and shots as the camp test
 * ({@link CampTest}): no clash or refusal, every resident spawned by kind, a shot trader unhurt, the
 * player inside counted as in a safezone, 30 s on post. Writes run/client/devtest/refuge.tsv for
 * tools/props/propclash.py and walkcheck.py. -Pturns=0..3, -Pseed=N, -Ppoints=a,b.
 */
public class RefugeTest extends CampTest
{
    public String name() { return "refuge"; }

    protected int origin()
    {
        return -19400;
    }

    protected CanvasPlan makePlan(String id, long seed, int x, int z, int turns)
    {
        return new CityRefugePlan(id, seed, x, z, turns, 230);
    }

    protected String prefix()
    {
        return "TR_";
    }
}
