package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.List;

import net.decimation.worldgen.city.CityNames;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

/**
 * Place names (worldgen/PlaceNames, docs/zones.md "Place names"): finds two neighbouring city
 * districts and a spot outside every city near the world's middle, walks the test player into each
 * (3 s apart), checks the client's turf list holds the district names, and takes a shot each time
 * (Decimation's "ENTERING LOCATION" banner and the "Now entering ..." chat line). Shots
 * places_<n>.png.
 */
public class PlacesTest extends DevTestMode
{
    public String name() { return "places"; }

    private static final int START = 20, STEP = 100, SHOT = 70;
    private final List<double[]> spots = new ArrayList<double[]>();
    private final List<String> expect = new ArrayList<String>();
    private volatile int ticks;
    private volatile boolean ready;
    private int taken = -1;

    public boolean client(Minecraft mc)
    {
        int t = ticks;
        if (mc.thePlayer == null || !ready)
        {
            return true;
        }
        int i = (t - START) / STEP, u = (t - START) % STEP;
        if (i >= spots.size())
        {
            return false;
        }
        if (i >= 0 && u >= SHOT && taken != i)
        {
            taken = i;
            String want = expect.get(i);
            boolean has = false;
            for (String e : net.decimation.fixes.Deci.clientTurfs())
            {
                has |= e.endsWith("=" + want.replace(' ', '_'));
            }
            if (!want.isEmpty())
            {
                DevTestResults.check(name(), "client turf list has " + want, has, has, "true");
            }
            DevTestUtil.screenshot(mc, name(), "places_" + i);
        }
        return true;
    }

    public void server()
    {
        int t = ++ticks;
        EntityPlayerMP p = DevTestUtil.player();
        if (p == null)
        {
            return;
        }
        World w = p.worldObj;
        if (t == 2)
        {
            find(w);
            ready = true;
        }
        int i = (t - START) / STEP;
        if (ready && t >= START && i < spots.size() && (t - START) % STEP == 0)
        {
            double[] s = spots.get(i);
            w.getChunkProvider().loadChunk((int) s[0] >> 4, (int) s[2] >> 4);
            int y = w.getTopSolidOrLiquidBlock((int) s[0], (int) s[2]) + 1;
            p.capabilities.isFlying = true;
            p.playerNetServerHandler.setPlayerLocation(s[0], y + 6, s[2], 0, 20);
        }
    }

    private void find(World w)
    {
        long seed = w.getSeed();
        for (int r = 0; r < 40 && spots.isEmpty(); r++)
        {
            for (int dx = -r; dx <= r && spots.isEmpty(); dx++)
            {
                for (int dz = -r; dz <= r && spots.isEmpty(); dz++)
                {
                    String[] a = CityNames.district(seed, dx, dz), b = CityNames.district(seed, dx + 1, dz);
                    if (a != null && b != null)
                    {
                        spots.add(new double[] {dx * 128 + 40, 0, dz * 128 + 40});
                        expect.add(a[0]);
                        spots.add(new double[] {dx * 128 + 168, 0, dz * 128 + 40});
                        expect.add(b[0]);
                        DevTestResults.value(name(), "district 1", a[0] + " (" + a[1] + ") at " + (dx * 128) + "," + (dz * 128));
                        DevTestResults.value(name(), "district 2", b[0] + " (" + b[1] + ")");
                    }
                }
            }
        }
        // outside every city: walk away from the last district until no district
        if (!spots.isEmpty())
        {
            int bx = (int) spots.get(0)[0] / 128, bz = (int) spots.get(0)[2] / 128;
            for (int k = 1; k < 40; k++)
            {
                if (CityNames.district(seed, bx - k, bz) == null)
                {
                    spots.add(new double[] {(bx - k) * 128 + 40, 0, bz * 128 + 40});
                    expect.add("");
                    DevTestResults.value(name(), "outside", ((bx - k) * 128) + "," + (bz * 128));
                    break;
                }
            }
        }
        DevTestResults.check(name(), "found districts", spots.size(), spots.size() >= 2, ">= 2");
    }
}
