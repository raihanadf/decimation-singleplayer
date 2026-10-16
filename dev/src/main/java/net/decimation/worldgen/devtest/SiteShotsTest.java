package net.decimation.worldgen.devtest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.decimation.worldgen.DecimationWorldGen;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;

/**
 * Screenshots for the project website (docs/website.md section 6): one shot per row of tools/site/siteshots.tsv
 * (name, x, y, z, yaw, pitch, time; y "~N" = N above the ground). The server holds the player there flying, no rain,
 * the row's time of day; rows with "~" wait 100 ticks high above first so the ground is generated. The client waits
 * until the chunks are drawn (no chunk updates for 3 s, at least 10 s, at most 60 s) and saves
 * screenshots/site_<name>.png with the GUI hidden. Row "gun" holds our MAC-10: the hand stays, the HUD is cancelled.
 * -Pshots=a,b shoots only those rows; -Pwindow=1920x1080 sets the window size.
 */
public class SiteShotsTest extends DevTestMode
{
    public String name() { return "siteshots"; }

    /** Shots need the current generation everywhere: a fresh seed 1 world. */
    public boolean freshWorld() { return true; }

    private static final int MIN_WAIT = 200, QUIET = 60, MAX_WAIT = 1200, GROUND_WAIT = 100;

    /** One camera row of the TSV. */
    static final class Row
    {
        final String name, y;
        final double x, z;
        final float yaw, pitch;
        final long time;

        Row(String[] c)
        {
            name = c[0];
            x = Double.parseDouble(c[1]);
            y = c[2];
            z = Double.parseDouble(c[3]);
            yaw = Float.parseFloat(c[4]);
            pitch = Float.parseFloat(c[5]);
            time = Long.parseLong(c[6]);
        }

        boolean relative()
        {
            return y.startsWith("~");
        }
    }

    static List<Row> read(File f, String only) throws IOException
    {
        List<String> keep = only == null || only.isEmpty() ? null : Arrays.asList(only.split(","));
        List<Row> rows = new ArrayList<Row>();
        for (String line : Files.readAllLines(f.toPath(), StandardCharsets.UTF_8))
        {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
            {
                continue;
            }
            String[] c = line.split("\t");
            if (c.length < 7)
            {
                throw new IOException("siteshots.tsv needs 7 columns: " + line);
            }
            Row r = new Row(c);
            if (keep == null || keep.contains(r.name))
            {
                rows.add(r);
            }
        }
        return rows;
    }

    /** Hides the HUD (not the hand) while a "gun" row is shot. */
    public static final class HudHider
    {
        volatile boolean active;

        @SubscribeEvent
        public void overlay(RenderGameOverlayEvent.Pre e)
        {
            if (active && e.type == RenderGameOverlayEvent.ElementType.ALL)
            {
                e.setCanceled(true);
            }
        }
    }

    private static HudHider hud;
    private volatile List<Row> rows;
    private volatile int index;
    private volatile int placed = -1;
    private int serverRow = -1, serverT, waited, quiet;
    private double groundY;
    private float savedFov, savedGamma;
    private int savedDistance;
    private boolean savedClouds, saved;

    public boolean client(Minecraft mc)
    {
        if (rows == null)
        {
            String only = System.getProperty("deciworldgen.autotest.shots");
            File tsv = new File(mc.mcDataDir, "../../../tools/site/siteshots.tsv");
            try
            {
                rows = read(tsv.getCanonicalFile(), only);
            }
            catch (IOException e)
            {
                DevTestResults.check(name(), "siteshots.tsv", e.getMessage(), false, "a readable tools/site/siteshots.tsv");
                return false;
            }
            if (hud == null)
            {
                hud = new HudHider();
                MinecraftForge.EVENT_BUS.register(hud);
            }
        }
        if (!saved)
        {
            saved = true;
            savedFov = mc.gameSettings.fovSetting;
            savedGamma = mc.gameSettings.gammaSetting;
            savedDistance = mc.gameSettings.renderDistanceChunks;
            savedClouds = mc.gameSettings.clouds;
        }
        int i = index;
        if (i >= rows.size())
        {
            // the dev client is also used for play: leave its options as they were
            mc.gameSettings.fovSetting = savedFov;
            mc.gameSettings.gammaSetting = savedGamma;
            mc.gameSettings.renderDistanceChunks = savedDistance;
            mc.gameSettings.clouds = savedClouds;
            mc.gameSettings.hideGUI = false;
            hud.active = false;
            return false;
        }
        Row r = rows.get(i);
        boolean gun = "gun".equals(r.name);
        mc.gameSettings.fovSetting = 70.0F;
        mc.gameSettings.gammaSetting = 0.6F;
        mc.gameSettings.renderDistanceChunks = 16;
        mc.gameSettings.clouds = true;
        mc.gameSettings.hideGUI = !gun;
        hud.active = gun;
        if (mc.thePlayer == null || placed < i)
        {
            waited = 0;
            quiet = 0;
            return true;
        }
        if (gun)
        {
            mc.thePlayer.inventory.currentItem = 0;
        }
        // hold the camera: a touched mouse or touchpad must not turn the shot
        mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = r.yaw;
        mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = r.pitch;
        waited++;
        quiet = chunkUpdates(mc) == 0 ? quiet + 1 : 0;
        if (waited < MIN_WAIT || quiet < QUIET && waited < MAX_WAIT)
        {
            return true;
        }
        String file = "site_" + r.name + ".png";
        net.minecraft.util.ScreenShotHelper.saveScreenshot(mc.mcDataDir, file, mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
        File out = new File(mc.mcDataDir, "screenshots/" + file);
        DevTestResults.shot(name(), file);
        DevTestResults.value(name(), "shot " + r.name, mc.displayWidth + " x " + mc.displayHeight + ", waited " + waited
                             + " ticks" + (quiet < QUIET ? " (chunks still drawing)" : ""));
        DevTestResults.check(name(), "saved " + r.name, out.isFile(), out.isFile(), "a png in screenshots/");
        FMLLog.info("[%s] siteshots %s at %.1f %s %.1f yaw %.0f pitch %.0f", DecimationWorldGen.MODID, r.name, r.x, r.y, r.z, r.yaw, r.pitch);
        index = i + 1;
        return true;
    }

    /** "60 fps, 12 chunk updates": the chunk updates of the last second. */
    private static int chunkUpdates(Minecraft mc)
    {
        String d = mc.debug;
        int a = d.indexOf(", "), b = d.indexOf(" chunk");
        return a < 0 || b < a ? 0 : Integer.parseInt(d.substring(a + 2, b).trim());
    }

    public void server()
    {
        EntityPlayerMP p = DevTestUtil.player();
        List<Row> list = rows;
        int i = index;
        if (p == null || list == null || i >= list.size())
        {
            return;
        }
        Row r = list.get(i);
        if (serverRow != i)
        {
            serverRow = i;
            serverT = 0;
            p.capabilities.allowFlying = true;
            p.capabilities.isFlying = true;
            p.sendPlayerAbilities();
            p.worldObj.setWorldTime(r.time);
            p.worldObj.getWorldInfo().setRaining(false);
            p.worldObj.getWorldInfo().setThundering(false);
            if ("gun".equals(r.name))
            {
                giveMac10(p);
            }
        }
        serverT++;
        p.worldObj.setWorldTime(r.time); // hold the light while the client waits
        double y;
        if (r.relative())
        {
            if (serverT < GROUND_WAIT)
            {
                y = 200; // above everything while the chunks around generate
            }
            else
            {
                if (serverT == GROUND_WAIT)
                {
                    groundY = p.worldObj.getHeightValue((int) Math.floor(r.x), (int) Math.floor(r.z));
                }
                y = groundY + Double.parseDouble(r.y.substring(1));
            }
        }
        else
        {
            y = Double.parseDouble(r.y);
        }
        p.playerNetServerHandler.setPlayerLocation(r.x, y, r.z, r.yaw, r.pitch);
        if (!r.relative() || serverT > GROUND_WAIT)
        {
            placed = i;
        }
    }

    private static void giveMac10(EntityPlayerMP p)
    {
        Object o = Item.itemRegistry.getObject("deci:mac10");
        if (o == null)
        {
            o = Item.itemRegistry.getObject("deciworldgen:mac10");
        }
        if (o == null)
        {
            DevTestResults.check("siteshots", "MAC-10 item", "missing", false, "deci:mac10 or deciworldgen:mac10");
            return;
        }
        ItemStack s = new ItemStack((Item) o);
        s.stackTagCompound = new NBTTagCompound(); // gun data ready before the first frame
        s.stackTagCompound.setInteger("ammo", 0);
        p.inventory.currentItem = 0;
        p.inventory.mainInventory[0] = s;
        p.inventoryContainer.detectAndSendChanges();
    }
}
