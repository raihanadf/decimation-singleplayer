package net.decimation.worldgen.military;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.decimation.worldgen.Residents;
import net.decimation.worldgen.SafezoneSites;
import net.decimation.worldgen.ZoneKind;

/**
 * A {@link SurvivorCamp} as a slice placed plan (docs/safezone.md): turned, graded, a SAFEZONE
 * zone around it, its residents handed to SafezoneSites with the zone.
 */
public final class SurvivorCampPlan extends CanvasPlan implements Residents
{
    private static final Map<String, SurvivorCamp> CACHE = new LinkedHashMap<String, SurvivorCamp>(16, 0.75f, true)
    {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, SurvivorCamp> e)
        {
            return size() > 6;
        }
    };

    private final long seed;

    public SurvivorCampPlan(String id, long seed, int minX, int minZ, int turns)
    {
        super(id, minX, minZ, turns);
        this.seed = seed;
    }

    /** Footprint (x, z): the camp is square. */
    public static int[] footprint()
    {
        return new int[] {SurvivorCamp.W, SurvivorCamp.L};
    }

    private SurvivorCamp camp()
    {
        synchronized (CACHE)
        {
            SurvivorCamp m = CACHE.get(id);
            if (m == null)
            {
                m = new SurvivorCamp(seed);
                m.build();
                CACHE.put(id, m);
            }
            return m;
        }
    }

    /** Clash / refusal log (dev test). */
    public List<String> debugLog()
    {
        return camp().log;
    }

    Canvas canvas() { return camp().c; }
    Map<String, float[]> canvasPoints() { return camp().poi; }
    int canvasWidth() { return SurvivorCamp.W; }
    int canvasLength() { return SurvivorCamp.L; }
    int canvasHeight() { return SurvivorCamp.HEIGHT; }

    public int maxSpread() { return 9; }
    public ZoneKind zone() { return ZoneKind.SAFEZONE; }

    public String describe()
    {
        return "survivor camp (safezone) rot " + turns * 90;
    }

    /**
     * Registers the camp as Slices does for a generated one: the SAFEZONE box 6 around and the
     * residents (the /deciworldgen camp command, the camp dev test).
     */
    public void register(int baseY)
    {
        int x0 = minX() - 6, z0 = minZ() - 6, x1 = maxX() + 6, z1 = maxZ() + 6;
        net.decimation.worldgen.ZoneStore.add(ZoneKind.SAFEZONE, x0, baseY - 4, z0, x1, baseY + height() + 12, z1);
        SafezoneSites.add(id, x0, baseY - 4, z0, x1, baseY + height() + 12, z1, residents(baseY));
    }

    public List<SafezoneSites.Post> residents(int baseY)
    {
        List<SafezoneSites.Post> out = new ArrayList<SafezoneSites.Post>();
        for (SurvivorCamp.Post p : camp().posts)
        {
            double[] w = world(p.x, p.z);
            out.add(new SafezoneSites.Post(p.kind, w[0], baseY + p.y, w[1], p.yaw + 90 * turns));
        }
        return out;
    }
}
