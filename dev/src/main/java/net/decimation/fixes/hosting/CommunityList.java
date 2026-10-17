package net.decimation.fixes.hosting;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.util.ArrayList;
import java.util.List;
import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.GuiScreenEvent;

/**
 * Decimation's Community page (GuiMenuCommunityServers) filled from our list (docs/superpowers/specs/2026-10-18-player-
 * hosting-part1-design.md 3.1, mockup section 4): one row per world on a local port of JoinService (Decimation pings and
 * joins it like any server, so its players and ping come through the relay); the title carries the name, another
 * DeciRemnant version greys it; the hovered row's tooltip lists the mods, a missing one red; the page counts the players
 * and worlds; a join that Forge's handshake drops within 20 s is tried once more by itself.
 */
public final class CommunityList
{
    private static final int COMMUNITY_TAB = 13;
    private static volatile List<ListEntry> shown = new ArrayList<ListEntry>();
    private static volatile long joinedAt;
    private static volatile boolean retried;
    private static volatile ServerData lastServer;

    /** Fetches the list on its own thread, then shows the page on the client thread. */
    public static void open(final Minecraft mc)
    {
        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                List<ListEntry> list = new ArrayList<ListEntry>();
                String error = null;
                try
                {
                    list = new RelayClient(RelayClient.base()).list();
                }
                catch (RelayClient.RelayFull e)
                {
                    error = I18n.format("deciworldgen.host.full");
                }
                catch (Exception e)
                {
                    error = I18n.format("deciworldgen.list.unreachable");
                }
                final List<ListEntry> got = list;
                final String err = error;
                mc.func_152344_a(new Runnable()
                {
                    public void run()
                    {
                        show(mc, got, err);
                    }
                });
            }
        }, "DeciRemnant list");
        t.setDaemon(true);
        t.start();
    }

    static void show(Minecraft mc, List<ListEntry> list, String error)
    {
        JoinService.INSTANCE.closeAll();
        JoinService.INSTANCE.setToasts(new JoinService.Toasts()
        {
            public void show(final String line)
            {
                Minecraft.getMinecraft().func_152344_a(new Runnable()
                {
                    public void run()
                    {
                        Deci.toast(line);
                    }
                });
            }
        });
        JoinService.INSTANCE.setJoinHook(new JoinService.JoinHook()
        {
            public void joining(String name, int port)
            {
                lastServer = new ServerData(name, "127.0.0.1:" + port);
                joinedAt = System.currentTimeMillis();
            }
        });
        ModSet mine = ModSet.current();
        String version = ModSet.ourVersion();
        List<Object> rows = new ArrayList<Object>();
        List<String> titles = new ArrayList<String>();
        List<ListEntry> kept = new ArrayList<ListEntry>();
        for (ListEntry e : list)
        {
            try
            {
                int port = JoinService.INSTANCE.listen(e, mine);
                boolean same = version.equals(e.version);
                rows.add(Deci.communityRow(e.name, port, e.online ? "Mojang accounts" : "Any account"));
                titles.add((same ? "" : "§7") + e.name + (same ? "" : " §c(needs " + e.version + ")"));
                kept.add(e);
            }
            catch (java.io.IOException ex)
            {
                // no free local port: leave the row out
            }
        }
        shown = kept;
        retried = false;
        // a row's Join goes through FMLClientHandler.connectToServer, which reads FML's server list cache; only Minecraft's
        // own Multiplayer screen creates it: without this a Join crashed the game (NullPointerException, found 18 Oktober 2026)
        cpw.mods.fml.client.FMLClientHandler.instance().setupServerList();
        GuiScreen page = Deci.communityScreen(rows);
        mc.displayGuiScreen(page);
        Deci.setCommunityRows(page, rows, titles);
        if (error != null)
        {
            Deci.toast(error);
        }
    }

    @SubscribeEvent
    public void onClick(GuiScreenEvent.ActionPerformedEvent.Pre e)
    {
        if (e.button.id == COMMUNITY_TAB && (Deci.isCommunityScreen(e.gui) || Deci.isOfficialScreen(e.gui)))
        {
            e.setCanceled(true);
            open(Minecraft.getMinecraft());
        }
    }

    @SubscribeEvent
    public void onOpen(GuiOpenEvent e)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (e.gui instanceof GuiDisconnected && !retried && lastServer != null && System.currentTimeMillis() - joinedAt < 20_000)
        {
            retried = true; // Forge's handshake timed out on a busy computer (seen in the spike): join once more by itself
            e.gui = new GuiConnecting(Deci.homeMenu(), mc, lastServer);
            return;
        }
        if (Deci.isCommunityScreen(mc.currentScreen) && !Deci.isCommunityScreen(e.gui) && !(e.gui instanceof GuiConnecting)
            && mc.theWorld == null)
        {
            JoinService.INSTANCE.closeAll(); // left the page without joining: free the ports
        }
        if (Deci.isHomeMenu(e.gui) && mc.theWorld == null && System.currentTimeMillis() - joinedAt > 20_000)
        {
            JoinService.INSTANCE.closeAll(); // back on the main menu after a join or a failed one (review: ports leaked)
        }
    }

    /** Leaving a joined world frees every local port the page opened (review: they stayed open until the next visit). */
    @SubscribeEvent
    public void onDisconnect(cpw.mods.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent e)
    {
        if (System.currentTimeMillis() - joinedAt > 20_000)
        {
            JoinService.INSTANCE.closeAll();
        }
    }

    /** The Community tab tells how many worlds are listed (Decimation's own tooltip said "Offline", then "coming soon"). */
    @SubscribeEvent
    public void onInit(GuiScreenEvent.InitGuiEvent.Post e)
    {
        if (!Deci.isOfficialScreen(e.gui) && !Deci.isCommunityScreen(e.gui))
        {
            return;
        }
        @SuppressWarnings("unchecked")
        List<GuiButton> b = e.buttonList;
        for (int k = 0; k < b.size(); k++)
        {
            GuiButton old = b.get(k);
            if (old.id == COMMUNITY_TAB && old.yPosition < 80) // the tab at y 48, not Refresh List at y 86
            {
                b.set(k, Deci.menuButton(COMMUNITY_TAB, old.xPosition, old.yPosition, old.width, old.height, "§7Community",
                                         shown.size() + " worlds"));
            }
        }
    }

    @SubscribeEvent
    public void onDraw(GuiScreenEvent.DrawScreenEvent.Post e)
    {
        if (!Deci.isCommunityScreen(e.gui))
        {
            return;
        }
        FontRenderer f = Minecraft.getMinecraft().fontRenderer;
        int players = 0;
        for (ListEntry w : shown)
        {
            players += w.players + 1; // the host plays too
        }
        f.drawStringWithShadow("§a● §fPlayers Online: §a" + players + " §fin " + shown.size() + " worlds",
                               e.gui.width / 2 - 173 + 119, 63, 0xFFFFFF);
        int i = Deci.hoveredCommunityRow(e.gui);
        if (i < 0 || i >= shown.size())
        {
            return;
        }
        ListEntry entry = shown.get(i);
        String missing = ModSet.current().firstMissing(entry.modSet());
        List<String> lines = new ArrayList<String>();
        lines.add(I18n.format("deciworldgen.list.mods"));
        for (String[] m : entry.modSet().pairs())
        {
            String s = m[0] + " " + m[1];
            lines.add((s.equals(missing) ? "§c" : "§f") + s);
        }
        lines.add("§7host " + entry.host + " · " + (entry.online ? "Mojang accounts" : "Any account"));
        int w = 0;
        for (String s : lines)
        {
            w = Math.max(w, f.getStringWidth(s));
        }
        int x0 = e.mouseX + 12, y0 = e.mouseY - 12;
        Gui.drawRect(x0 - 3, y0 - 3, x0 + w + 3, y0 + lines.size() * 10 + 1, 0xF0100010);
        for (int k = 0; k < lines.size(); k++)
        {
            f.drawStringWithShadow(lines.get(k), x0, y0 + k * 10, 0xFFFFFF);
        }
    }
}
