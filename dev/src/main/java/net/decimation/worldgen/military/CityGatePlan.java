package net.decimation.worldgen.military;

import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;

/**
 * One wasteland gate (CityGate style) at its place in the city: the canvas turned so its line row lies on
 * the fence line and its street runs out into the street chunk before it. Not placed as a plan of its own:
 * StreetPlan asks blockAt for the cells it owns (city/WastelandFence.gate), y 0 = the street surface. Turns 1: line along z, the street west of it; turns 2: line along x, the street north of it.
 */
public final class CityGatePlan extends CanvasPlan
{
    public final int look, state;

    public CityGatePlan(String id, int minX, int minZ, int turns, int look, int state)
    {
        super(id, minX, minZ, turns);
        this.look = look;
        this.state = state;
    }

    Canvas canvas()
    {
        return CityGate.canvas(look, state);
    }

    /** A compact toll gate: owns only the line and the crossing's two road rows (no street before it). */
    public boolean compact()
    {
        return look < 0;
    }

    int canvasWidth() { return CityGate.W; }
    int canvasLength() { return CityGate.L; }
    int canvasHeight() { return CityGate.H; }

    /** Height of the gate's cells (StreetPlan clears its own blocks below it). */
    public int canvasHeightPublic() { return CityGate.H; }

    public int maxSpread() { return 0; }

    Map<String, float[]> canvasPoints()
    {
        return CityGate.points();
    }

    @Override
    public List<String> debugLog()
    {
        return CityGate.log(look, state);
    }

    @Override
    public String describe()
    {
        return "wasteland gate " + CityGate.name(look, state);
    }

    @Override
    public net.decimation.worldgen.ZoneKind zone()
    {
        return null;
    }

    @Override
    public Block blockAt(int rx, int ly, int rz, int[] meta)
    {
        Block b = super.blockAt(rx, ly, rz, meta);
        if (b != null && b == BlockBoomArm.instance && meta[0] < 2 && (turns & 1) == 1)
        {
            meta[0] ^= 1;                                      // an arm lying along x lies along z
        }
        return b;
    }
}
