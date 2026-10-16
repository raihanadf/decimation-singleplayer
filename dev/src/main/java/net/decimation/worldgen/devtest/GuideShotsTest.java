package net.decimation.worldgen.devtest;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import cpw.mods.fml.client.FMLClientHandler;
import net.decimation.fixes.Deci;
import net.decimation.worldgen.DevAutoTest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenAddServer;
import net.minecraft.client.gui.GuiSelectWorld;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

/**
 * Pictures and checks for DeciRemnant's install guide and server page (docs/website.md): walks the way a new
 * player goes from Decimation's menu (Play, Singleplayer, Create New World, More World Options, World Type until
 * it says Decimation) and the way to a server (Other servers opens Minecraft's Multiplayer screen, Add Server,
 * join). Every screen on the way is checked; only Minecraft's own screens are photographed (Decimation's menu
 * shows the dev session warning, which players never see). -Pserver=host[:port] names a running server for the
 * Multiplayer picture and the join test (default localhost). The server entry lives in memory only: the test
 * never saves servers.dat. Leaves the world first and ends at Decimation's home menu.
 *
 * The join counts only when the player is still in the server's world 10 s later: Decimation's anticheat kicks
 * a player whose mods folder holds a file the server lacks, or no file at all. The dev client hashes only its
 * empty mods/1.7.10 folder, so for a real join put one identical small file in the server's mods and the dev
 * client's mods/1.7.10 (docs/website.md, server guide facts).
 */
public class GuideShotsTest extends DevTestMode
{
    public String name() { return "guideshots"; }

    private static final String SERVER_NAME = "Decimation server";

    private final String host = System.getProperty("deciworldgen.autotest.server", "localhost");
    private int step;
    private int wait;
    private int ticks;
    private int savedScale = -1;
    private int inWorld;
    private IntegratedServer stopping;
    private GuiScreen serverPage;
    private GuiMultiplayer multiplayer;
    private ServerData entry;

    public boolean client(Minecraft mc)
    {
        if (wait > 0)
        {
            wait--;
            return true;
        }
        ticks++;
        switch (step)
        {
            case 0:
                DevAutoTest.keepScreen = true;
                savedScale = mc.gameSettings.guiScale;
                mc.gameSettings.guiScale = 0;              // auto, as a new player has it: 3 in a 1440 x 810 window
                if (mc.theWorld != null)
                {
                    stopping = mc.getIntegratedServer();
                    mc.theWorld.sendQuittingDisconnectingPacket();
                    mc.loadWorld((net.minecraft.client.multiplayer.WorldClient) null);
                }
                mc.displayGuiScreen(Deci.homeMenu());
                next(1, 40);
                return true;
            case 1:
                if (stopping != null && !stopping.isServerStopped() && ticks < 20 * 60)
                {
                    return true;                           // the test world is still being saved
                }
                GuiButton play = button(mc.currentScreen, "Play");
                DevTestUtil.screenshot(mc, name(), "guide_menu_home");   // kept local: may show dev only text
                DevTestResults.value(name(), "home menu play button", play == null ? "none" : label(play));
                DevTestResults.check(name(), "home menu has Play", play != null, play != null, "a Play button");
                if (play == null)
                {
                    return finish(mc);
                }
                press(mc.currentScreen, play);
                next(2, 20);
                return true;
            case 2:
                serverPage = mc.currentScreen;
                GuiButton single = button(serverPage, "Singleplayer");
                GuiButton other = button(serverPage, "Other servers");
                DevTestResults.value(name(), "screen after Play", serverPage == null ? "none" : serverPage.getClass().getName());
                DevTestResults.check(name(), "Play opens the server page with Singleplayer and Other servers",
                                     single != null && other != null, single != null && other != null, "both buttons");
                if (single == null)
                {
                    return finish(mc);
                }
                DevTestUtil.screenshot(mc, name(), "guide_menu_servers");
                press(serverPage, single);
                next(3, 20);
                return true;
            case 3:
                boolean select = mc.currentScreen instanceof GuiSelectWorld;
                DevTestResults.check(name(), "Singleplayer opens the world list", name(mc.currentScreen), select, "GuiSelectWorld");
                if (!select)
                {
                    return finish(mc);
                }
                press(mc.currentScreen, byId(mc.currentScreen, 3));   // Create New World
                next(4, 30);
                return true;
            case 4:
                boolean create = mc.currentScreen instanceof GuiCreateWorld;
                DevTestResults.check(name(), "Create New World opens", name(mc.currentScreen), create, "GuiCreateWorld");
                if (!create)
                {
                    return finish(mc);
                }
                DevTestUtil.screenshot(mc, name(), "guide_createworld");
                press(mc.currentScreen, byId(mc.currentScreen, 3));   // More World Options...
                next(5, 10);
                return true;
            case 5:
                GuiButton type = byId(mc.currentScreen, 5);           // World Type: ...
                int clicks = 0;
                while (type != null && !label(type).endsWith("Decimation") && clicks < 30)
                {
                    press(mc.currentScreen, type);
                    clicks++;
                }
                String shown = type == null ? "none" : label(type);
                DevTestResults.value(name(), "World Type clicks to Decimation", clicks);
                DevTestResults.check(name(), "World Type reaches Decimation", shown, shown.equals("World Type: Decimation"),
                                     "World Type: Decimation");
                next(6, 30);
                return true;
            case 6:
                DevTestUtil.screenshot(mc, name(), "guide_worldtype");
                mc.displayGuiScreen(serverPage);
                next(7, 20);
                return true;
            case 7:
                GuiButton others = button(mc.currentScreen, "Other servers");
                if (others == null)
                {
                    DevTestResults.check(name(), "server page again", name(mc.currentScreen), false, "Other servers button");
                    return finish(mc);
                }
                press(mc.currentScreen, others);
                next(8, 20);
                return true;
            case 8:
                boolean multi = mc.currentScreen instanceof GuiMultiplayer;
                DevTestResults.check(name(), "Other servers opens Multiplayer", name(mc.currentScreen), multi, "GuiMultiplayer");
                if (!multi)
                {
                    return finish(mc);
                }
                multiplayer = (GuiMultiplayer) mc.currentScreen;
                ServerList list = multiplayer.func_146795_p();
                while (list.countServers() > 0)
                {
                    list.removeServerData(0);                         // in memory only, never saved
                }
                entry = new ServerData(SERVER_NAME, host);
                list.addServerData(entry);
                refresh(multiplayer, list);
                multiplayer.func_146790_a(0);
                ticks = 0;
                next(9, 0);
                return true;
            case 9:
                if (entry.pingToServer <= 0 && ticks < 20 * 15)
                {
                    return true;                                      // the ping answers in a second or two
                }
                DevTestResults.value(name(), "server ping ms", entry.pingToServer);
                DevTestResults.value(name(), "server motd", EnumChatFormatting.getTextWithoutFormattingCodes(String.valueOf(entry.serverMOTD)));
                DevTestResults.value(name(), "server players", EnumChatFormatting.getTextWithoutFormattingCodes(String.valueOf(entry.populationInfo)));
                DevTestResults.check(name(), "server answers the ping", entry.pingToServer, entry.pingToServer > 0, "> 0 ms");
                next(10, 40);
                return true;
            case 10:
                DevTestUtil.screenshot(mc, name(), "guide_multiplayer");
                mc.displayGuiScreen(new GuiScreenAddServer(multiplayer, new ServerData(SERVER_NAME, host)));
                next(11, 30);
                return true;
            case 11:
                DevTestUtil.screenshot(mc, name(), "guide_addserver");
                FMLClientHandler.instance().connectToServer(multiplayer, entry);
                ticks = 0;
                next(12, 0);
                return true;
            case 12:
                // Decimation's anticheat kicks a moment after the login (it compares the mods folders),
                // so "joined" means still in the server's world 10 s later
                if (mc.currentScreen instanceof GuiDisconnected)
                {
                    DevTestResults.value(name(), "join", "kicked: " + disconnectText((GuiDisconnected) mc.currentScreen));
                    DevTestUtil.screenshot(mc, name(), "guide_kicked");
                    return finish(mc);
                }
                if (mc.theWorld != null && mc.thePlayer != null && ++inWorld >= 200)
                {
                    DevTestResults.value(name(), "join", "in the server's world for 10 s");
                    next(13, 0);
                    return true;
                }
                if (ticks > 20 * 90)
                {
                    DevTestResults.value(name(), "join", "no answer in 90 s, screen " + name(mc.currentScreen));
                    return finish(mc);
                }
                return true;
            case 13:
                DevTestUtil.screenshot(mc, name(), "guide_joined");
                if (mc.theWorld != null)
                {
                    mc.theWorld.sendQuittingDisconnectingPacket();
                    mc.loadWorld((net.minecraft.client.multiplayer.WorldClient) null);
                }
                return finish(mc);
            default:
                return false;
        }
    }

    private void next(int s, int w)
    {
        step = s;
        wait = w;
    }

    /** Back to Decimation's home menu, settings as before; the mode ends. */
    private boolean finish(Minecraft mc)
    {
        if (savedScale >= 0)
        {
            mc.gameSettings.guiScale = savedScale;
        }
        mc.displayGuiScreen(Deci.homeMenu());
        DevAutoTest.keepScreen = false;
        step = 99;
        return false;
    }

    private static String name(GuiScreen s)
    {
        return s == null ? "none" : s.getClass().getName();
    }

    private static String label(GuiButton b)
    {
        return EnumChatFormatting.getTextWithoutFormattingCodes(b.displayString).trim();
    }

    /** The screen's button whose label starts with the text (formatting codes ignored), or null. */
    private static GuiButton button(GuiScreen screen, String text)
    {
        for (GuiButton b : buttons(screen))
        {
            if (b.displayString != null && label(b).startsWith(text))
            {
                return b;
            }
        }
        return null;
    }

    private static GuiButton byId(GuiScreen screen, int id)
    {
        for (GuiButton b : buttons(screen))
        {
            if (b.id == id)
            {
                return b;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static List<GuiButton> buttons(GuiScreen screen)
    {
        try
        {
            Field f = GuiScreen.class.getDeclaredField("buttonList");
            f.setAccessible(true);
            return (List<GuiButton>) f.get(screen);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("buttonList", e);
        }
    }

    /** What a click on the button does: the screen's actionPerformed (Decimation's menu buttons too). */
    private static void press(GuiScreen screen, GuiButton b)
    {
        for (Class<?> c = screen.getClass(); c != null; c = c.getSuperclass())
        {
            try
            {
                Method m = c.getDeclaredMethod("actionPerformed", GuiButton.class);
                m.setAccessible(true);
                m.invoke(screen, b);
                return;
            }
            catch (NoSuchMethodException e)
            {
                // try the parent class
            }
            catch (Exception e)
            {
                throw new IllegalStateException("press " + b.displayString, e);
            }
        }
        throw new IllegalStateException("no actionPerformed on " + screen.getClass());
    }

    /** The Multiplayer list shows the server list's entries again (it reads them once, in initGui). */
    private static void refresh(GuiMultiplayer gui, ServerList list)
    {
        try
        {
            Field f = GuiMultiplayer.class.getDeclaredField("field_146803_h");
            f.setAccessible(true);
            Object selector = f.get(gui);
            Method m = selector.getClass().getDeclaredMethod("func_148195_a", ServerList.class);
            m.setAccessible(true);
            m.invoke(selector, list);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("server list refresh", e);
        }
    }

    private static String disconnectText(GuiDisconnected gui)
    {
        try
        {
            Field f = GuiDisconnected.class.getDeclaredField("field_146304_f");
            f.setAccessible(true);
            IChatComponent c = (IChatComponent) f.get(gui);
            return c == null ? "" : c.getUnformattedText();
        }
        catch (Exception e)
        {
            return "(no reason: " + e + ")";
        }
    }
}
