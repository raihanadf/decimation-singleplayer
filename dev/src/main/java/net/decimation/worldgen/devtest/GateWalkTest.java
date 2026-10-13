package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.List;

import net.decimation.fixes.Deci;
import net.decimation.worldgen.military.CityGate;
import net.decimation.worldgen.military.CityGatePlan;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;

/**
 * Do the wasteland gates stop bodies? The toll gate and the compact toll in every state, built in the sky at
 * -20000, 230, -18600 (lines along z, the street in front to the west), and one infected per lane (x 3..12 of
 * the canvas) pushed east from 7 blocks before the line for 6 s with a step height of 1.1 (a jump), its own
 * walking cancelled, at night (infected burn by day); the fence on the line's sidewalks and 12 blocks on each
 * side is built beside it as in a city. A body counts as through when it gets 3 blocks past the line. Checks: shut gates let no
 * lane through, open gates at least one. -Pgates=toll/wire,... picks some.
 */
public class GateWalkTest extends DevTestMode
{
    public String name() { return "gatewalk"; }

    private static final int Y = 230, X0 = -20000, Z0 = -18600, STEP = 44, BUILD = 10, SPAWN = 20, PUSH = 120;

    private final String pick = System.getProperty("deciworldgen.autotest.gates", "");
    private final List<CityGatePlan> plans = new ArrayList<CityGatePlan>();
    private final List<List<EntityLiving>> bodies = new ArrayList<List<EntityLiving>>();
    /** Farthest x each body reached (it may walk off the far end of the sky pad and fall). */
    private final java.util.Map<EntityLiving, Double> far = new java.util.IdentityHashMap<EntityLiving, Double>();
    private volatile int ticks;
    private volatile boolean finished;

    public boolean client(Minecraft mc)
    {
        return !finished;
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
            p.capabilities.isFlying = true;
            p.capabilities.disableDamage = true;
            w.difficultySetting = net.minecraft.world.EnumDifficulty.NORMAL;
            w.setWorldTime(18000);                              // night: infected burn in daylight
            p.playerNetServerHandler.setPlayerLocation(X0 + 15, Y + 20, Z0 + 2 * STEP, 0, 90);
        }
        if (t == BUILD)
        {
            int i = 0;
            for (int look = -1; look <= 0; look++)
            {
                for (int state = 0; state < CityGate.STATES.length; state++)
                {
                    String n = CityGate.name(look, state);
                    if (!pick.isEmpty() && !("," + pick + ",").contains("," + n.replace(' ', '_') + ","))
                    {
                        continue;
                    }
                    // looks side by side along x, states along z: every gate within the player's loaded chunks
                    CityGatePlan plan = new CityGatePlan("TW_" + i, X0 + (look + 1) * 30, Z0 + state * STEP, 1, look, state);
                    GateTest.build(w, plan);
                    // the fence on the line's sidewalks (StreetPlan / WastelandFence.inCity in a real city)
                    int line = plan.maxX() - 2;
                    for (int a = -12; a <= 27; a++)                // and on along the line past the gate, as in a city
                    {
                        if (a >= 3 && a <= 12)
                        {
                            continue;
                        }
                        boolean post = a == 2 || a == 13;
                        for (int k = 1; k <= (post ? 4 : 3); k++)
                        {
                            w.setBlock(line, Y + k, plan.minZ() + a, post ? Block.getBlockFromName("deci:BlockMetal_2")
                                : net.minecraft.init.Blocks.iron_bars, 0, 2);
                        }
                    }
                    plans.add(plan);
                    i++;
                }
            }
        }
        if (t == SPAWN)
        {
            for (CityGatePlan plan : plans)
            {
                List<EntityLiving> lane = new ArrayList<EntityLiving>();
                int line = plan.maxX() - 2;                      // turns 1: the line is 2 in from the east end
                for (int a = 3; a <= 12; a++)
                {
                    EntityLiving e = null;
                    for (int tries = 0; tries < 20 && e == null; tries++)
                    {
                        EntityLiving c = Deci.newInfected(w);
                        c.setLocationAndAngles(line - 7 + 0.5, Y + 1, plan.minZ() + a + 0.5, -90, 0);
                        if (w.spawnEntityInWorld(c))
                        {
                            e = c;
                        }
                    }
                    lane.add(e);
                }
                bodies.add(lane);
            }
        }
        if (t > SPAWN && t <= SPAWN + PUSH)
        {
            for (List<EntityLiving> lane : bodies)
            {
                for (EntityLiving e : lane)
                {
                    if (e == null || e.isDead)
                    {
                        continue;
                    }
                    e.stepHeight = 1.1F;
                    e.motionX = e.motionZ = 0;
                    e.moveEntity(0.3, -0.2, 0);
                    Double m = far.get(e);
                    far.put(e, m == null ? e.posX : Math.max(m, e.posX));
                }
            }
        }
        if (t == SPAWN + PUSH + 1)
        {
            for (int g = 0; g < plans.size(); g++)
            {
                CityGatePlan plan = plans.get(g);
                int line = plan.maxX() - 2, through = 0, alive = 0;
                StringBuilder reached = new StringBuilder();
                for (EntityLiving e : bodies.get(g))
                {
                    if (e == null || !far.containsKey(e))
                    {
                        reached.append(" -");
                        continue;
                    }
                    alive++;
                    double x = far.get(e);
                    reached.append(String.format(" %.1f", x - line));
                    through += x > line + 3 ? 1 : 0;
                    e.setDead();
                }
                // the line and the crossing's rows after the push: open cells (h 1 and 2 both passable) by lane
                StringBuilder gaps = new StringBuilder();
                for (int dx = 0; dx <= 2; dx++)
                {
                    for (int a = 0; a <= 15; a++)
                    {
                        int x = line + dx, z = plan.minZ() + a;
                        if (w.getBlock(x, Y + 1, z).getCollisionBoundingBoxFromPool(w, x, Y + 1, z) == null
                            && w.getBlock(x, Y + 2, z).getCollisionBoundingBoxFromPool(w, x, Y + 2, z) == null)
                        {
                            gaps.append(" ").append(dx).append(":").append(a);
                        }
                    }
                }
                String n = CityGate.name(plan.look, plan.state);
                DevTestResults.value(name(), n + " open cells (rows past the line : lane)", gaps.toString().trim());
                DevTestResults.value(name(), n + " reached (farthest x past the line, lanes 3..12)", reached.toString().trim());
                boolean open = plan.state == CityGate.OPEN;
                DevTestResults.check(name(), n + (open ? " lanes through (open)" : " lanes through (shut)"), through,
                                     open ? through > 0 : through == 0 && alive >= 8, open ? "> 0" : "0 of 10");
            }
            finished = true;
        }
    }
}
