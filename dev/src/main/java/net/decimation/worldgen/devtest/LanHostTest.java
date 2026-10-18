package net.decimation.worldgen.devtest;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

/**
 * Player hosting spike (docs/player_hosting.md section 6): this game opens its world to LAN (online mode from
 * `online=true|false`, default false) and writes the port to devtest/lan_port.txt for the relay's host agent. A
 * second game joins through the relay; then, with the joiner in survival on the arena and the host in creative far
 * off: an infected next to the joiner (does it chase the joiner, does the joiner lose health), a bandit (does it shoot
 * the joiner), the joiner kills an infected (Decimation's humanity for that player), then two bandwidth phases for the
 * relay log: standing still, and travelling 200 blocks every 5 s (fresh chunks). Phase times go to the results so the
 * relay's per second byte log can be cut by them. `wait=TICKS` (default 2400) for the joiner.
 */
public class LanHostTest extends DevTestMode
{
    public String name() { return "lanhost"; }

    private final boolean online = "true".equals(System.getProperty("deciworldgen.autotest.online"));
    /** real=true: host through HostService and the relay at relay=ADDRESS (player hosting part 1), not tools/hostspike. */
    private final boolean real = "true".equals(System.getProperty("deciworldgen.autotest.real"));
    private int hostStarted;
    private final int wait = Integer.parseInt(System.getProperty("deciworldgen.autotest.wait", "2400"));
    /** scene=true: no timed steps; the joiner's test (LanJoinTest) asks for each scene through LanBus. */
    private final boolean scene = "true".equals(System.getProperty("deciworldgen.autotest.scene"));
    private String lastReq = "";
    private final java.util.List<Entity> made = new java.util.ArrayList<Entity>();
    private int crateX, crateY, crateZ;
    private EntityLiving target;
    private float sceneMin = 20;
    private boolean watchHealth;
    private volatile int ticks, joinedAt = -1;
    private volatile boolean done;
    private EntityPlayerMP joiner;
    private int deaths;
    private EntityLiving mob;
    private float minHealth;
    private int humanityBefore;
    private double travelX;

    /** Promo shots asked by the joiner (promo=true): 1 the world view, 2 the Host Online page; run on the client thread. */
    private volatile int shoot, shootAt;

    public boolean client(Minecraft mc)
    {
        if (shoot == 1 && ++shootAt > 200) // chunks around the street drawn (they generate on the first visit)
        {
            mc.displayGuiScreen(null);
            mc.gameSettings.hideGUI = false;
            DevTestUtil.screenshot(mc, name(), "promo_host");
            mc.displayGuiScreen(new net.decimation.fixes.hosting.HostScreen(null));
            shoot = 2;
            shootAt = 0;
        }
        else if (shoot == 2 && ++shootAt > 20)
        {
            DevTestUtil.screenshot(mc, name(), "promo_hostpage");
            mc.displayGuiScreen(null);
            shoot = 3;
        }
        return !done || shoot == 1 || shoot == 2; // the host's server may lag: finish the shots after "end"
    }

    public void server()
    {
        int t = ++ticks;
        EntityPlayerMP host = DevTestUtil.player();
        if (host == null || done)
        {
            return;
        }
        IntegratedServer s = (IntegratedServer) MinecraftServer.getServer();
        List<?> players = s.getConfigurationManager().playerEntityList;
        if (t == 20)
        {
            s.setOnlineMode(online);
            // a second run in the same live game finds the world open already (1.7.10 cannot close it again)
            String port = s.getPublic() ? String.valueOf(lastPort) : s.shareToLAN(WorldSettings.GameType.SURVIVAL, false);
            if (!s.getPublic() || port == null || port.equals("-1"))
            {
                DevTestResults.check(name(), "world open to LAN", port, false, "a port");
                done = true;
                return;
            }
            lastPort = Integer.parseInt(port);
            write(port);
            DevTestResults.value(name(), "LAN port", port);
            DevTestResults.value(name(), "online mode", s.isServerInOnlineMode());
            if (real) // host through the mod's own hosting (plan Task 10), against relay=ADDRESS
            {
                String relay = System.getProperty("deciworldgen.autotest.relay");
                if (relay != null)
                {
                    System.setProperty("deciworldgen.relay", relay);
                }
                net.decimation.fixes.hosting.HostService.Settings st = new net.decimation.fixes.hosting.HostService.Settings();
                st.name = System.getProperty("deciworldgen.autotest.worldname", "Spike world");
                st.online = online;
                st.max = 4;
                st.player = host.getCommandSenderName();
                st.version = net.decimation.fixes.hosting.ModSet.ourVersion();
                final IntegratedServer srv = s;
                net.decimation.fixes.hosting.HostService.INSTANCE.start(st, lastPort, () -> Math.max(0, srv.getCurrentPlayerCount() - 1));
                net.decimation.fixes.hosting.HostLan.limit(srv, st.max); // what HostScreen does
                DevTestResults.check(name(), "server max players follows the host's choice", srv.getMaxPlayers(),
                                     srv.getMaxPlayers() == st.max, String.valueOf(st.max));
                hostStarted = t;
            }
            return;
        }
        if (real && hostStarted > 0 && t == hostStarted + 100) // the start runs on a background thread
        {
            net.decimation.fixes.hosting.HostService.State hs = net.decimation.fixes.hosting.HostService.INSTANCE.state();
            DevTestResults.check(name(), "host state", hs + " " + net.decimation.fixes.hosting.HostService.INSTANCE.error(),
                                 hs == net.decimation.fixes.hosting.HostService.State.ONLINE, "ONLINE");
        }
        if (joinedAt < 0)
        {
            for (Object o : players)
            {
                if (o != host)
                {
                    joiner = (EntityPlayerMP) o;
                    joinedAt = t;
                    DevTestResults.check(name(), "second player joined", joiner.getCommandSenderName() + " after "
                                         + (t - 20) / 20 + " s", true, "within " + wait / 20 + " s");
                    stamp("joined");
                }
            }
            if (joinedAt < 0 && t > 20 + wait)
            {
                DevTestResults.check(name(), "second player joined", "nobody", false, "within " + wait / 20 + " s");
                done = true;
            }
            return;
        }
        EntityPlayerMP now = s.getConfigurationManager().func_152612_a(joiner.getCommandSenderName());
        if (now != null && now != joiner)
        {
            deaths++; // a respawn makes a new player object
            joiner = now;
        }
        if (!players.contains(joiner) && scene)
        {
            joinedAt = -1; // the joiner's test leaves and joins again first (the rejoin case): wait for the new join
            return;
        }
        if (!players.contains(joiner))
        {
            DevTestResults.check(name(), "joiner stays connected", "left at step " + (t - joinedAt), false, "to the end");
            done = true;
            return;
        }
        int j = t - joinedAt;
        World w = joiner.worldObj;
        if (scene)
        {
            scene(host, w, j);
            return;
        }
        if (j == 80) // spawn protection is over after 60 ticks
        {
            Deci.Player d = Deci.player(joiner);
            DevTestResults.value(name(), "joiner Decimation data", "bottlecaps " + d.bottlecaps() + ", humanity "
                                 + d.humanity() + ", water " + d.water());
            w.getWorldInfo().setRaining(false);
            w.getWorldInfo().setThundering(false);
            w.setWorldTime(6000);
            DevTestArena.build(w, host);
            host.setGameType(WorldSettings.GameType.CREATIVE);
            host.setPositionAndUpdate(DevTestArena.X - 40, DevTestArena.Y, DevTestArena.Z);
            joiner.setGameType(WorldSettings.GameType.SURVIVAL);
            joiner.setPositionAndUpdate(DevTestArena.X + 0.5, DevTestArena.Y, DevTestArena.Z + 0.5);
            Deci.player(joiner).healFully();
            joiner.setHealth(joiner.getMaxHealth());
        }
        if (j == 120)
        {
            mob = spawn(Deci.newInfected(w), w, 6);
            minHealth = joiner.getHealth();
            stamp("infected");
        }
        if (j > 120 && j < 420)
        {
            minHealth = Math.min(minHealth, joiner.getHealth());
            joiner.setHealth(Math.max(joiner.getHealth(), 6)); // keep the joiner alive: a respawn ends the chase
        }
        if (j == 420)
        {
            DevTestResults.check(name(), "infected chases the joiner", target(mob), joiner == targetEntity(mob),
                                 "the joiner");
            DevTestResults.check(name(), "infected hurts the joiner", joiner.getMaxHealth() + " -> " + minHealth,
                                 minHealth < joiner.getMaxHealth(), "health drops");
            mob.setDead();
            Deci.player(joiner).healFully();
            joiner.setHealth(joiner.getMaxHealth());
        }
        if (j == 440)
        {
            mob = spawn(Deci.newBandit(w), w, 12);
            minHealth = joiner.getHealth();
            stamp("bandit");
        }
        if (j > 440 && j < 840)
        {
            minHealth = Math.min(minHealth, joiner.getHealth());
            joiner.setHealth(Math.max(joiner.getHealth(), 6)); // keep the joiner alive through the bandit's fire
        }
        if (j == 840)
        {
            DevTestResults.check(name(), "bandit targets the joiner", target(mob), joiner == targetEntity(mob),
                                 "the joiner");
            DevTestResults.check(name(), "bandit hurts the joiner", joiner.getMaxHealth() + " -> " + minHealth,
                                 minHealth < joiner.getMaxHealth(), "health drops");
            mob.setDead();
            Deci.player(joiner).healFully();
            joiner.setHealth(joiner.getMaxHealth());
            humanityBefore = Deci.player(joiner).humanity();
            mob = spawn(Deci.newInfected(w), w, 4);
        }
        if (j == 860)
        {
            mob.attackEntityFrom(DamageSource.causePlayerDamage(joiner), 1000);
        }
        if (j == 900)
        {
            int after = Deci.player(joiner).humanity();
            DevTestResults.check(name(), "joiner's infected kill counts", humanityBefore + " -> " + after,
                                 after != humanityBefore, "humanity changes");
            stamp("idle");
        }
        if (j == 1500)
        {
            stamp("travel");
            travelX = DevTestArena.X;
        }
        if (j > 1500 && j <= 2100 && (j - 1500) % 100 == 0)
        {
            travelX += 200;
            joiner.setGameType(WorldSettings.GameType.CREATIVE);
            joiner.setPositionAndUpdate(travelX, 120, DevTestArena.Z);
        }
        if (j == 2100)
        {
            stamp("end");
            joiner.setPositionAndUpdate(DevTestArena.X + 0.5, DevTestArena.Y, DevTestArena.Z + 0.5);
            DevTestResults.value(name(), "joiner ping (keep alive, ms)", joiner.ping);
            DevTestResults.value(name(), "joiner respawns", deaths);
            done = true;
        }
    }

    private static int lastPort = -1;

    private static int size(List<?> l)
    {
        return l == null ? -1 : l.size();
    }

    /** One request of the joiner's test per tick: set the scene up here on the server, answer, the joiner checks. */
    private void scene(EntityPlayerMP host, World w, int j)
    {
        if (j == 1)
        {
            LanBus.clear();
        }
        if (watchHealth)
        {
            sceneMin = Math.min(sceneMin, joiner.getHealth());
            joiner.setHealth(Math.max(joiner.getHealth(), 6)); // alive: a respawn would end the scene
        }
        String[] req = LanBus.read("req");
        if (req == null || req.length < 2 || String.join(" ", req).equals(lastReq))
        {
            return;
        }
        lastReq = String.join(" ", req);
        String n = req[0], verb = req[1], answer = "ok";
        int x = DevTestArena.X, y = DevTestArena.Y, z = DevTestArena.Z;
        if (verb.equals("arena"))
        {
            w.getWorldInfo().setRaining(false);
            w.getWorldInfo().setThundering(false);
            w.setWorldTime(6000);
            DevTestArena.build(w, host);
            host.setGameType(WorldSettings.GameType.CREATIVE);
            host.setPositionAndUpdate(x - 40, y, z);
            joiner.setGameType(WorldSettings.GameType.SURVIVAL);
            joiner.inventory.clearInventory(null, -1);
            joiner.playerNetServerHandler.setPlayerLocation(x + 0.5, y, z + 0.5, 180, 0);
            Deci.player(joiner).healFully();
            joiner.setHealth(joiner.getMaxHealth());
            answer = "ok " + size(Deci.clientTurfs()) + " " + size(Deci.clientSafezones()); // the host's own lists
        }
        else if (verb.equals("crate"))
        {
            String pick = null;
            for (String b : Deci.lootTable().keySet())
            {
                if (b.startsWith("deci:") && (pick == null || b.toLowerCase().contains("crate") && !pick.toLowerCase().contains("crate")))
                {
                    pick = b;
                }
            }
            net.minecraft.block.Block b = pick == null ? null : net.minecraft.block.Block.getBlockFromName(pick);
            crateX = x;
            crateY = y;
            crateZ = z - 2;
            if (b != null)
            {
                w.setBlock(crateX, crateY, crateZ, b, 0, 3);
                Deci.clearLootCooldowns();
            }
            answer = (b == null ? "none" : "ok") + " " + crateX + " " + crateY + " " + crateZ + " " + pick;
        }
        else if (verb.equals("hummer"))
        {
            w.setBlockToAir(crateX, crateY, crateZ);
            Entity h = Deci.newHummer(w, x + 0.5, y, z + 5.5);
            if (h != null && !w.spawnEntityInWorld(h))
            {
                h = null;
            }
            if (h != null)
            {
                made.add(h);
            }
            Entity seat = h == null ? null : Deci.vehicleDriverSeat(h);
            answer = (h == null ? "none" : "ok") + " " + (h == null ? -1 : h.getEntityId()) + " " + (seat == null ? -1 : seat.getEntityId());
        }
        else if (verb.equals("lists"))
        {
            answer = "ok " + net.decimation.worldgen.PlaceNames.sentCount(joiner.getUniqueID()) + " " + size(Deci.clientSafezones());
        }
        else if (verb.equals("riding"))
        {
            answer = "ok " + (joiner.ridingEntity == null ? "none" : joiner.ridingEntity.getClass().getSimpleName());
            if (joiner.ridingEntity != null)
            {
                joiner.mountEntity(null);
            }
            for (Entity e : made)
            {
                e.setDead();
            }
            made.clear();
            joiner.playerNetServerHandler.setPlayerLocation(x + 0.5, y, z + 0.5, 180, 0);
        }
        else if (verb.equals("trader"))
        {
            Deci.player(joiner).addBottlecaps(1000);
            Entity tr = net.minecraft.entity.EntityList.createEntityByName("TraderFood", w);
            boolean ok = false;
            for (int i = 0; tr != null && i < 5 && !ok; i++)
            {
                tr.setLocationAndAngles(x + 0.5, y, z - 2.5, 0, 0);
                ok = w.spawnEntityInWorld(tr);
            }
            if (ok)
            {
                made.add(tr);
            }
            answer = (ok ? "ok" : "none") + " " + (ok ? tr.getEntityId() : -1) + " " + Deci.player(joiner).bottlecaps();
        }
        else if (verb.equals("caps"))
        {
            answer = "ok " + Deci.player(joiner).bottlecaps();
            for (Entity e : made)
            {
                e.setDead();
            }
            made.clear();
        }
        else if (verb.equals("gun"))
        {
            net.minecraft.item.ItemStack g = new net.minecraft.item.ItemStack(cpw.mods.fml.common.registry.GameRegistry.findItem("deci", "akm"));
            g.stackTagCompound = new net.minecraft.nbt.NBTTagCompound();
            g.stackTagCompound.setInteger("ammo", 30);
            joiner.inventory.setInventorySlotContents(joiner.inventory.currentItem, g);
            joiner.inventoryContainer.detectAndSendChanges();
            joiner.capabilities.disableDamage = true;
            target = null;
            for (int i = 0; i < 10 && target == null; i++)
            {
                EntityLiving e = Deci.newInfected(w);
                e.setLocationAndAngles(x + 0.5, y, z - 4.5, 0, 0);
                if (w.spawnEntityInWorld(e))
                {
                    target = e;
                    made.add(e);
                }
            }
            answer = (target == null ? "none -1 0" : "ok " + target.getEntityId() + " " + target.getHealth());
        }
        else if (verb.equals("target"))
        {
            answer = "ok " + (target == null ? "none" : target.isDead + " " + target.getHealth()) + " " + Deci.gunAmmo(joiner.getHeldItem());
            for (Entity e : made)
            {
                e.setDead();
            }
            made.clear();
            joiner.capabilities.disableDamage = false;
        }
        else if (verb.equals("bandit"))
        {
            Deci.player(joiner).healFully();
            joiner.setHealth(joiner.getMaxHealth());
            sceneMin = joiner.getMaxHealth();
            watchHealth = true;
            EntityLiving bd = null;
            for (int i = 0; i < 10 && bd == null; i++)
            {
                EntityLiving e = Deci.newBandit(w);
                e.setLocationAndAngles(x + 0.5, y, z - 8.5, 0, 0);
                if (w.spawnEntityInWorld(e))
                {
                    bd = e;
                    made.add(e);
                }
            }
            answer = (bd == null ? "none -1" : "ok " + bd.getEntityId());
        }
        else if (verb.equals("health"))
        {
            watchHealth = false;
            answer = "ok " + sceneMin + " " + joiner.getMaxHealth() + " " + deaths;
            for (Entity e : made)
            {
                e.setDead();
            }
            made.clear();
        }
        else if (verb.equals("promo")) // both players with a gun in a city street of seed 1 (tools/site/siteshots.tsv "city")
        {
            w.getWorldInfo().setRaining(false);
            w.getWorldInfo().setThundering(false);
            w.setWorldTime(Long.getLong("deciworldgen.autotest.promotime", 6000L));
            int px = Integer.getInteger("deciworldgen.autotest.promox", 1400), pz = Integer.getInteger("deciworldgen.autotest.promoz", 520);
            int gap = 5;
            for (EntityPlayerMP p : new EntityPlayerMP[] {host, joiner})
            {
                p.setGameType(WorldSettings.GameType.SURVIVAL);
                p.capabilities.disableDamage = true;
                net.minecraft.item.ItemStack g = new net.minecraft.item.ItemStack(cpw.mods.fml.common.registry.GameRegistry.findItem("deci", "akm"));
                g.stackTagCompound = new net.minecraft.nbt.NBTTagCompound();
                g.stackTagCompound.setInteger("ammo", 30);
                p.inventory.setInventorySlotContents(p.inventory.currentItem, g);
                p.inventoryContainer.detectAndSendChanges();
            }
            int hy = w.getTopSolidOrLiquidBlock(px, pz), jy = w.getTopSolidOrLiquidBlock(px - gap, pz);
            host.playerNetServerHandler.setPlayerLocation(px + 0.5, hy, pz + 0.5, 90, 0);       // facing west, toward the joiner
            joiner.playerNetServerHandler.setPlayerLocation(px - gap + 0.5, jy, pz + 0.5, 270, 0); // facing east, toward the host
            answer = "ok " + hy + " " + jy;
        }
        else if (verb.equals("promohost"))
        {
            shoot = 1;
            shootAt = 0;
        }
        else if (verb.equals("end"))
        {
            DevTestResults.value(name(), "scenes done", "joiner respawns " + deaths);
            if (real)
            {
                net.decimation.fixes.hosting.HostService.INSTANCE.stop();
            }
            done = true;
        }
        LanBus.write("res", n + " " + verb + " " + answer);
        DevTestResults.value(name(), "scene " + verb, answer);
    }

    private EntityLiving spawn(EntityLiving e, World w, int dx)
    {
        for (int i = 0; i < 10; i++)
        {
            e.setPosition(joiner.posX + dx, DevTestArena.Y, joiner.posZ);
            if (w.spawnEntityInWorld(e))
            {
                return e;
            }
            e = Deci.isInfected(e) ? Deci.newInfected(w) : Deci.newBandit(w);
        }
        return e;
    }

    private static Entity targetEntity(EntityLiving e)
    {
        if (e.getAttackTarget() != null)
        {
            return e.getAttackTarget();
        }
        return e instanceof EntityCreature ? ((EntityCreature) e).getEntityToAttack() : null;
    }

    private static String target(EntityLiving e)
    {
        Entity t = targetEntity(e);
        return t == null ? "none" : t.getCommandSenderName();
    }

    private void stamp(String phase)
    {
        DevTestResults.value(name(), "phase " + phase + " at", System.currentTimeMillis() / 10 / 100.0);
    }

    private static void write(String port)
    {
        try
        {
            File f = new File(Minecraft.getMinecraft().mcDataDir, "devtest/lan_port.txt");
            f.getParentFile().mkdirs();
            FileWriter out = new FileWriter(f);
            out.write(port);
            out.close();
        }
        catch (Exception e)
        {
            DevTestResults.value("lanhost", "port file", e.toString());
        }
    }
}
