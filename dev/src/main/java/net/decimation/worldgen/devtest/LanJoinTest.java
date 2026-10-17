package net.decimation.worldgen.devtest;

import java.util.List;
import java.util.Map;

import net.decimation.fixes.Deci;
import net.decimation.worldgen.DevAutoTest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.integrated.IntegratedServer;

/**
 * The JOINER's side of the player hosting spike (docs/player_hosting.md section 9), run in a second game on its own live
 * port: leaves its own world for Decimation's menu (as a player would), turns fancy graphics on, joins the host through
 * the relay (`joinhost=NAME joinport=PORT`, default spike-world.relay.localhost:25600), then asks the host's `lanhost
 * scene=true` for one scene after another over LanBus and checks each on this client: the HUD survives fancy graphics,
 * the zone and place lists arrived, Decimation's crate opens its loot screen, the hummer seats us, the trader screen
 * opens and a buy costs bottlecaps, our AKM hits an infected, a bandit's fire hurts us and draws tracers.
 */
public class LanJoinTest extends DevTestMode
{
    public String name() { return "lanjoin"; }

    private final String joinHost = System.getProperty("deciworldgen.autotest.joinhost", "spike-world.relay.localhost");
    private final int joinPort = Integer.getInteger("deciworldgen.autotest.joinport", 25600);
    private int step, wait, req;
    private IntegratedServer stopping;
    private String[] res;
    private int hostTurfs = -1, hostSafe = -1, x, y, z, id = -1, seat = -1;
    private long caps;
    private int maxTracers;

    public boolean client(Minecraft mc)
    {
        wait++;
        DevAutoTest.keepScreen = true;
        switch (step)
        {
            case 0: // out of this game's own world, onto Decimation's menu (it makes the HUD's player preview)
                if (mc.theWorld != null)
                {
                    stopping = mc.getIntegratedServer();
                    mc.theWorld.sendQuittingDisconnectingPacket();
                    mc.loadWorld((net.minecraft.client.multiplayer.WorldClient) null);
                }
                mc.displayGuiScreen(Deci.homeMenu());
                next(1);
                break;
            case 1:
                if ((stopping != null && !stopping.isServerStopped() || !Deci.menuShown()) && wait < 20 * 60)
                {
                    break;
                }
                DevTestResults.value(name(), "menu shown before joining", Deci.menuShown());
                mc.gameSettings.fancyGraphics = true;
                mc.displayGuiScreen(new GuiConnecting(mc.currentScreen, mc, joinHost, joinPort));
                next(2);
                break;
            case 2:
                if (mc.theWorld != null && mc.thePlayer != null && !mc.isSingleplayer())
                {
                    DevTestResults.check(name(), "joined the host through the relay", wait / 20 + " s", true, "in the host's world");
                    next(3);
                }
                else if (wait > 20 * 120)
                {
                    DevTestResults.check(name(), "joined the host through the relay", screen(mc), false, "in the host's world");
                    return false;
                }
                break;
            case 3: // ten seconds of HUD with fancy graphics: the crash came after 4 to 5 s without the menu
                if (wait == 200)
                {
                    DevTestResults.check(name(), "HUD with fancy graphics after the menu", "connected " + (mc.theWorld != null),
                                         mc.theWorld != null, "no crash");
                    ask("arena");
                    next(4);
                }
                break;
            case 4:
                if (answered())
                {
                    next(5);
                }
                break;
            case 5:
                if (wait == 60)
                {
                    ask("crate");
                }
                if (wait > 60 && answered())
                {
                    x = intAt(3);
                    y = intAt(4);
                    z = intAt(5);
                    DevTestResults.value(name(), "crate", res.length > 6 ? res[6] : "?");
                    next(6);
                }
                break;
            case 6:
                if (wait == 20)
                {
                    mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), x, y, z, 1,
                                                           net.minecraft.util.Vec3.createVectorHelper(x + 0.5, y + 1.0, z + 0.5));
                }
                if (wait > 20 && Deci.LOOT_SCREEN.equals(screenClass(mc)) || wait > 140)
                {
                    DevTestResults.check(name(), "Decimation crate opens its loot screen", screen(mc),
                                         Deci.LOOT_SCREEN.equals(screenClass(mc)), Deci.LOOT_SCREEN);
                    mc.thePlayer.closeScreen();
                    ask("lists");
                    next(17);
                }
                break;
            case 17: // the lists go out every 2 s by position: compare with what the server last sent THIS player
                if (answered())
                {
                    hostTurfs = intAt(3);
                    hostSafe = intAt(4);
                    int turfs = size(Deci.clientTurfs()), safe = size(Deci.clientSafezones());
                    DevTestResults.check(name(), "place names list arrived", turfs + " (server sent " + hostTurfs + ")",
                                         turfs == hostTurfs, "what the server sent this player");
                    DevTestResults.check(name(), "safezone list arrived", safe + " (host " + hostSafe + ")", safe == hostSafe,
                                         "the host's count");
                    ask("hummer");
                    next(7);
                }
                break;
            case 7:
                if (answered())
                {
                    id = intAt(3);
                    seat = intAt(4);
                    next(8);
                }
                break;
            case 8:
                if (wait == 40 || wait == 80)
                {
                    Entity e = seatNear(mc, mc.theWorld.getEntityByID(id)); // a player clicks a seat (VehicleSeatEntity)
                    if (e != null)
                    {
                        mc.playerController.interactWithEntitySendPacket(mc.thePlayer, e);
                    }
                    DevTestResults.value(name(), "hummer seat clicked", e == null ? "no seat on the client" : e.getClass().getName());
                }
                if (wait > 40 && mc.thePlayer.ridingEntity != null || wait > 140)
                {
                    DevTestResults.check(name(), "right click seats us in the hummer",
                                         mc.thePlayer.ridingEntity == null ? "not riding" : mc.thePlayer.ridingEntity.getClass().getSimpleName(),
                                         mc.thePlayer.ridingEntity != null, "riding");
                    ask("riding");
                    next(9);
                }
                break;
            case 9:
                if (answered())
                {
                    DevTestResults.value(name(), "riding on the host's side", res.length > 3 ? res[3] : "?");
                    ask("trader");
                    next(10);
                }
                break;
            case 10:
                if (answered())
                {
                    id = intAt(3);
                    caps = intAt(4);
                    next(11);
                }
                break;
            case 11:
                if (wait == 40)
                {
                    Entity e = mc.theWorld.getEntityByID(id);
                    if (e != null)
                    {
                        mc.playerController.interactWithEntitySendPacket(mc.thePlayer, e);
                    }
                }
                if (wait > 40 && mc.currentScreen != null || wait > 160)
                {
                    DevTestResults.check(name(), "trader screen opens", screen(mc), mc.currentScreen != null, "a screen");
                    Entity e = mc.theWorld.getEntityByID(id);
                    List<Map.Entry<ItemStack, int[]>> offers = e == null ? null : Deci.traderOffers(e);
                    ItemStack offer = null;
                    int buy = 0;
                    for (Map.Entry<ItemStack, int[]> o : offers == null ? java.util.Collections.<Map.Entry<ItemStack, int[]>>emptyList() : offers)
                    {
                        if (o.getValue()[0] > 0 && o.getValue()[2] == 0)
                        {
                            offer = o.getKey();
                            buy = o.getValue()[0];
                            break;
                        }
                    }
                    DevTestResults.value(name(), "trader offers seen by the joiner", offers == null ? "none" : offers.size());
                    if (offer != null)
                    {
                        Deci.sendTrade(0, offer, buy, id, 0);
                    }
                    caps = offer == null ? -1 : caps - buy;
                    next(12);
                }
                break;
            case 12:
                if (wait == 40)
                {
                    mc.thePlayer.closeScreen();
                    ask("caps");
                }
                if (wait > 40 && answered())
                {
                    long now = intAt(3);
                    DevTestResults.check(name(), "a buy costs bottlecaps on the host", now + " (expected " + caps + ")",
                                         caps >= 0 && now == caps, "caps before minus the price");
                    ask("gun");
                    next(13);
                }
                break;
            case 13:
                if (answered())
                {
                    id = intAt(3);
                    next(14);
                }
                break;
            case 14:
                if (wait >= 40 && wait <= 80 && wait % 10 == 0)
                {
                    Entity e = mc.theWorld.getEntityByID(id);
                    if (e != null)
                    {
                        aim(mc, e.posX, e.posY + e.height * 0.6, e.posZ);
                        boolean fired = Deci.fireHeldGun(mc.thePlayer);
                        if (wait == 40)
                        {
                            DevTestResults.value(name(), "joiner fires the AKM", fired + ", target " + e.getClass().getSimpleName());
                        }
                    }
                }
                if (wait == 120)
                {
                    ask("target");
                }
                if (wait > 120 && answered())
                {
                    String r = String.join(" ", res);
                    boolean hit = r.contains(" true ") || res.length > 4 && parse(res[4]) < 20;
                    DevTestResults.check(name(), "joiner's shots hurt the infected on the host", r, hit, "dead or health below 20");
                    ask("bandit");
                    next(15);
                }
                break;
            case 15:
                maxTracers = Math.max(maxTracers, size(Deci.tracers()));
                if (wait == 600)
                {
                    ask("health");
                }
                if (wait > 600 && answered())
                {
                    float min = parse(res[3]), max = parse(res[4]);
                    DevTestResults.check(name(), "bandit fire hurts the joiner", max + " -> " + min, min < max, "health drops");
                    DevTestResults.check(name(), "bandit tracers drawn on the joiner", maxTracers, maxTracers > 0, "1 or more");
                    ask("end");
                    next(16);
                }
                break;
            case 16:
                if (answered() || wait > 100)
                {
                    DevAutoTest.keepScreen = false;
                    return false;
                }
                break;
        }
        return true;
    }

    private void next(int s)
    {
        step = s;
        wait = 0;
    }

    private void ask(String verb)
    {
        req++;
        res = null;
        LanBus.write("req", req + " " + verb);
    }

    /** True once the host answered the last request. */
    private boolean answered()
    {
        if (res == null)
        {
            String[] r = LanBus.read("res");
            if (r != null && r.length >= 3 && r[0].equals(String.valueOf(req)))
            {
                res = r;
            }
        }
        return res != null;
    }

    private int intAt(int i)
    {
        return res != null && res.length > i ? (int) parse(res[i]) : -1;
    }

    private static float parse(String s)
    {
        try
        {
            return Float.parseFloat(s);
        }
        catch (Exception e)
        {
            return -1;
        }
    }

    private static int size(List<?> l)
    {
        return l == null ? -1 : l.size();
    }

    private static String screenClass(Minecraft mc)
    {
        return mc.currentScreen == null ? "none" : mc.currentScreen.getClass().getName();
    }

    private static String screen(Minecraft mc)
    {
        return screenClass(mc);
    }

    /** The client's vehicle seat (deci.ad.c, VehicleSeatEntity) nearest to the player within 8 blocks of the vehicle. */
    private static Entity seatNear(Minecraft mc, Entity vehicle)
    {
        Entity best = null;
        for (Object o : mc.theWorld.loadedEntityList)
        {
            Entity e = (Entity) o;
            if (vehicle != null && e.getClass().getName().equals("deci.ad.c") && e.getDistanceToEntity(vehicle) < 8
                && (best == null || e.getDistanceToEntity(mc.thePlayer) < best.getDistanceToEntity(mc.thePlayer)))
            {
                best = e;
            }
        }
        return best;
    }

    /** Turns the client player to look at a point (PlayerFireTest.aim). */
    private static void aim(Minecraft mc, double x, double y, double z)
    {
        double dx = x - mc.thePlayer.posX, dz = z - mc.thePlayer.posZ;
        double dy = y - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        mc.thePlayer.rotationYaw = mc.thePlayer.prevRotationYaw = yaw;
        mc.thePlayer.rotationPitch = mc.thePlayer.prevRotationPitch = pitch;
    }
}
