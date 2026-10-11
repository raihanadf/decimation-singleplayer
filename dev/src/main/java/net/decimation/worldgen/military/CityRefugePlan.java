package net.decimation.worldgen.military;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.decimation.worldgen.FixedBase;
import net.decimation.worldgen.Residents;
import net.decimation.worldgen.SafezoneSites;
import net.decimation.worldgen.ZoneKind;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * A {@link CityRefuge} on a city block (docs/safezone.md): the 3 x 3 building chunks of one cell,
 * ground floor at the city ground (a fixed base like the city's own buildings, no grading: the
 * streets around it stay as they are), turned so the gate faces the chosen street. Slices registers
 * the SAFEZONE box (2 around) and the residents when its first slice is written.
 */
public final class CityRefugePlan extends CanvasPlan implements FixedBase, Residents
{
    private static final Map<String, CityRefuge> CACHE = new LinkedHashMap<String, CityRefuge>(16, 0.75f, true)
    {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CityRefuge> e)
        {
            return size() > 6;
        }
    };

    /** Site box margin (Slices: 2) + wall line (2): civilians found past it are put back. */
    public static final int WALL_INSET = 4;

    private final long seed;
    private final int ground;

    public CityRefugePlan(String id, long seed, int minX, int minZ, int turns, int ground)
    {
        super(id, minX, minZ, turns);
        this.seed = seed;
        this.ground = ground;
    }

    public static int size()
    {
        return CityRefuge.W;
    }

    private CityRefuge refuge()
    {
        synchronized (CACHE)
        {
            CityRefuge m = CACHE.get(id);
            if (m == null)
            {
                m = new CityRefuge(seed);
                m.build();
                CACHE.put(id, m);
            }
            return m;
        }
    }

    /** Clash / refusal log (dev test). */
    public List<String> debugLog()
    {
        return refuge().log;
    }

    Canvas canvas() { return refuge().c; }
    Map<String, float[]> canvasPoints() { return refuge().poi; }
    int canvasWidth() { return CityRefuge.W; }
    int canvasLength() { return CityRefuge.L; }
    int canvasHeight() { return CityRefuge.HEIGHT; }

    public int fixedBaseY() { return ground; }
    public int maxSpread() { return 255; }
    public int clearAbove() { return 12; }
    public Block foundation() { return Blocks.stone; }
    public ZoneKind zone() { return ZoneKind.SAFEZONE; }
    public int wallInset() { return WALL_INSET; }

    // no ramp: the lot is the footprint (the city streets around it are at the same ground)
    public int lotMinX() { return minX(); }
    public int lotMinZ() { return minZ(); }
    public int lotMaxX() { return maxX(); }
    public int lotMaxZ() { return maxZ(); }

    @Override
    public void grade(World world, int x, int z, int baseY)
    {
    }

    public String describe()
    {
        return "city refuge (safezone) rot " + turns * 90;
    }

    /** Registers zone and residents as Slices does (dev test). */
    public void register(int baseY)
    {
        int x0 = minX() - 2, z0 = minZ() - 2, x1 = maxX() + 2, z1 = maxZ() + 2;
        net.decimation.worldgen.ZoneStore.add(ZoneKind.SAFEZONE, x0, baseY - 4, z0, x1, baseY + height() + 8, z1);
        SafezoneSites.add(id, x0, baseY - 4, z0, x1, baseY + height() + 8, z1, residents(baseY), WALL_INSET);
    }

    public List<SafezoneSites.Post> residents(int baseY)
    {
        List<SafezoneSites.Post> out = new ArrayList<SafezoneSites.Post>();
        for (SurvivorCamp.Post p : refuge().posts)
        {
            double[] w = world(p.x, p.z);
            out.add(new SafezoneSites.Post(p.kind, w[0], baseY + p.y, w[1], p.yaw + 90 * turns));
        }
        return out;
    }
}
