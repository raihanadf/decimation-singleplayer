package net.decimation.worldgen.devtest;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

/**
 * Facing check (docs/city_dressing.md, docs/references/prop_facing.tsv): every prop of -Pprops=
 * (names without deci:, default the ones whose verified facing looked doubtful) is placed four
 * times in a row on a sky platform at metadata 2, 3, 4, 5 (west to east), each on a 3 x 3 pad of
 * wool coloured by its metadata (2 magenta, 3 light blue, 4 yellow, 5 lime), one row per prop. The
 * camera looks at each row from the south (facing_<prop>_s.png: fronts pointing SOUTH show their face)
 * and from the north (facing_<prop>_n.png: fronts pointing NORTH show theirs); east / west fronts show
 * their side in both (tell them apart by the model).
 */
public class FacingTest extends DevTestMode
{
    public String name() { return "facing"; }

    private static final int X0 = -21000, Y = 200, Z0 = -21000, STEP = 4, ROW = 12;
    private static final int BUILD = 10, SETTLE = 40, SHOT = 60;
    private static final String DEFAULT = "BlockRoadsign20,BlockRoadsign30,BlockRoadsign60,BlockRoadsignOneway,"
        + "BlockRoadsignStop,BlockRoadsignParking,BlockBodyBagBlack,BlockBodyBagBlue,BlockHazardLight,BlockPowerGenerator,"
        + "BlockTrafficLights,BlockMailbox,BlockNewsStand1,BlockPhonebooth,BlockStreetBench,BlockElectricBox2,"
        + "BlockPowerPole,BlockCCTV,BlockExitLight,BlockFlatscreenTV_News,BlockSkeletonWall,BlockBicycle";

    private final List<String> names = new ArrayList<String>();
    private volatile int ticks;
    private volatile boolean built;
    private int taken = -1;
    private float gamma = -1;

    public FacingTest()
    {
        for (String n : System.getProperty("deciworldgen.autotest.props", DEFAULT).split(","))
        {
            if (!n.trim().isEmpty())
            {
                names.add(n.trim());
            }
        }
    }

    public boolean client(Minecraft mc)
    {
        int t = ticks;
        if (mc.thePlayer == null || !built)
        {
            return true;
        }
        int i = (t - BUILD - 20) / SHOT, u = (t - BUILD - 20) % SHOT;
        if (i >= 2 * names.size())
        {
            mc.gameSettings.hideGUI = false;
            if (gamma >= 0)
            {
                mc.gameSettings.gammaSetting = gamma;
            }
            return false;
        }
        if (i < 0)
        {
            return true;
        }
        if (gamma < 0)
        {
            gamma = mc.gameSettings.gammaSetting;
            mc.gameSettings.gammaSetting = 1.0F;
        }
        mc.gameSettings.hideGUI = true;
        mc.gameSettings.fovSetting = 70f;
        mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = i % 2 == 0 ? 180f : 0f;
        mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = 18f;
        if (u >= SETTLE && taken != i)
        {
            taken = i;
            DevTestUtil.screenshot(mc, name(), "facing_" + names.get(i / 2) + (i % 2 == 0 ? "_s" : "_n"));
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
            p.capabilities.isFlying = true;
            p.capabilities.disableDamage = true;
            w.setWorldTime(6000);
            w.getWorldInfo().setRaining(false);
        }
        if (t == BUILD)
        {
            build(w);
            built = true;
        }
        if (built)
        {
            int i = (t - BUILD - 20) / SHOT;
            if (i >= 0 && i < 2 * names.size())
            {
                double cx = X0 + 1.5 * STEP + 0.5, cz = Z0 + (i / 2) * ROW + 0.5;
                if (i % 2 == 0)
                {
                    p.playerNetServerHandler.setPlayerLocation(cx, Y + 2, cz + 10, 180f, 18f);   // south, looking north
                }
                else
                {
                    p.playerNetServerHandler.setPlayerLocation(cx, Y + 2, cz - 10, 0f, 18f);    // north, looking south
                }
                p.motionX = p.motionY = p.motionZ = 0;
            }
        }
    }

    private void build(World w)
    {
        for (int cx = (X0 - 16) >> 4; cx <= (X0 + 4 * STEP + 16) >> 4; cx++)
        {
            for (int cz = (Z0 - 16) >> 4; cz <= (Z0 + names.size() * ROW + 16) >> 4; cz++)
            {
                w.getChunkProvider().loadChunk(cx, cz);
            }
        }
        for (int x = X0 - 6; x <= X0 + 4 * STEP + 6; x++)
        {
            for (int z = Z0 - 6; z <= Z0 + names.size() * ROW + 6; z++)
            {
                w.setBlock(x, Y - 1, z, Blocks.stone, 0, 2);
                for (int y = Y; y < Y + 10; y++)
                {
                    w.setBlockToAir(x, y, z);
                }
            }
        }
        int[] wool = {2, 3, 4, 5};
        for (int i = 0; i < names.size(); i++)
        {
            Block b = Block.getBlockFromName("deci:" + names.get(i));
            if (b == null || b == Blocks.air)
            {
                DevTestResults.value(name(), names.get(i), "no such block");
                continue;
            }
            for (int m = 2; m <= 5; m++)
            {
                int x = X0 + (m - 2) * STEP, z = Z0 + i * ROW;
                for (int dx = -1; dx <= 1; dx++)
                {
                    for (int dz = -1; dz <= 1; dz++)
                    {
                        w.setBlock(x + dx, Y - 1, z + dz, Blocks.wool, wool[m - 2], 2);
                    }
                }
                w.setBlock(x, Y, z, b, m, 2);
                if (b.hasTileEntity(m))
                {
                    net.decimation.fixes.MultiblockRepairHandler.complete(w, x, Y, z);
                }
            }
            DevTestResults.value(name(), names.get(i), "row " + i);
        }
    }
}
