package net.decimation.fixes.hosting;

import java.util.Collections;
import java.util.List;
import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldSettings;

/**
 * The Host Online page (docs/superpowers/specs/2026-10-18-player-hosting-part1-design.md 3.1, mockup section 3):
 * name on the list, who can join, max players, the mods others need, the connection, Start / Stop hosting, and while
 * hosting the players with their ping. Decimation's own buttons (Deci.menuButton) on the dimmed world, the panel where
 * Decimation's pages put their server list.
 */
public final class HostScreen extends GuiScreen
{
    private static final int START = 1, BACK = 2, OFFLINE = 3, ONLINE = 4, MINUS = 5, PLUS = 6, RESUME = 7, COMMANDS = 8;
    private final GuiScreen parent;
    private GuiTextField name;
    private String typed;
    private boolean online = true; // Mojang accounts only by default (user, 18 Oktober 2026)
    private int max = 4;
    private boolean commands = true; // "Commands: only me" by default (user, 18 Oktober 2026)
    private HostService.State shown;

    public HostScreen(GuiScreen parent)
    {
        this.parent = parent;
        HostService.Settings s = HostService.INSTANCE.settings();
        if (s != null)
        {
            online = s.online;
            max = s.max;
            commands = s.commands;
            typed = s.name;
        }
        shown = HostService.INSTANCE.state();
    }

    private int x0()
    {
        return width / 2 - 173 + 105;
    }

    private boolean hosting()
    {
        HostService.State s = HostService.INSTANCE.state();
        return s == HostService.State.ONLINE || s == HostService.State.STARTING;
    }

    @SuppressWarnings("unchecked")
    public void initGui()
    {
        int x = x0();
        if (name != null)
        {
            typed = name.getText();
        }
        name = new GuiTextField(fontRendererObj, x + 6, 70, 234, 14);
        name.setMaxStringLength(32);
        IntegratedServer s = mc.getIntegratedServer();
        name.setText(typed != null ? typed : s == null ? "" : s.getWorldName());
        buttonList.clear();
        boolean on = hosting();
        if (!on)
        {
            buttonList.add(Deci.menuButton(OFFLINE, x + 6, 100, 234, 12, (online ? "" : "\u00a7a") + I18n.format("deciworldgen.host.offline"), ""));
            buttonList.add(Deci.menuButton(ONLINE, x + 6, 114, 234, 12, (online ? "\u00a7a" : "") + I18n.format("deciworldgen.host.online"), ""));
            buttonList.add(Deci.menuButton(MINUS, x + 6, 142, 14, 12, "-", ""));
            buttonList.add(Deci.menuButton(PLUS, x + 40, 142, 14, 12, "+", ""));
            buttonList.add(Deci.menuButton(COMMANDS, x + 100, 142, 140, 12,
                                           commands ? "\u00a7a" + I18n.format("deciworldgen.host.commands.me") : I18n.format("deciworldgen.host.commands.off"),
                                           I18n.format("deciworldgen.host.commands.tip")));
        }
        buttonList.add(Deci.menuButton(START, x + 63, 212, 120, 20,
                                       I18n.format(on ? "deciworldgen.host.stop" : "deciworldgen.host.start"), ""));
        buttonList.add(Deci.menuButton(RESUME, width / 2 - 172, 86, 100, 20, I18n.format("menu.button.backtogame"), ""));
        buttonList.add(Deci.menuButton(BACK, width / 2 - 172, 107, 100, 20, I18n.format("deciworldgen.host.back"), ""));
        name.setEnabled(!on);
    }

    protected void actionPerformed(GuiButton b)
    {
        switch (b.id)
        {
            case OFFLINE: online = false; break;
            case ONLINE: online = true; break;
            case MINUS: max = Math.max(2, max - 1); break;
            case PLUS: max = Math.min(8, max + 1); break;
            case COMMANDS: commands = !commands; break;
            case BACK: mc.displayGuiScreen(parent); return;
            case RESUME: mc.displayGuiScreen(null); return;
            case START: startOrStop(); break;
            default: break;
        }
        initGui();
    }

    private void startOrStop()
    {
        if (hosting())
        {
            HostService.INSTANCE.stop();
            HostOps.revoke(mc.getIntegratedServer());
            return;
        }
        IntegratedServer s = mc.getIntegratedServer();
        String clean = ListEntry.cleanName(name.getText());
        if (s == null || clean == null)
        {
            return;
        }
        s.setOnlineMode(online);
        WorldSettings.GameType mode = mc.theWorld == null ? WorldSettings.GameType.SURVIVAL : mc.theWorld.getWorldInfo().getGameType();
        String port = s.getPublic() ? HostLan.port() : s.shareToLAN(mode, false); // joiners play the world's own game mode (spec 4.1)
        HostLan.remember(port);
        if (port == null || port.equals("-1"))
        {
            Deci.toast(I18n.format("deciworldgen.host.failed", "no LAN port"));
            return;
        }
        HostService.Settings st = new HostService.Settings();
        st.name = clean;
        st.online = online;
        st.max = max;
        st.commands = commands;
        st.player = mc.getSession().getUsername();
        st.version = ModSet.ourVersion();
        HostLan.limit(s, max); // the status ping shows the host's own limit
        if (commands)
        {
            HostOps.grant(s, st.player); // the world is shared with cheats off: commands for the host only
        }
        HostService.INSTANCE.start(st, Integer.parseInt(port), new HostService.Players()
        {
            public int count()
            {
                IntegratedServer srv = Minecraft.getMinecraft().getIntegratedServer();
                return srv == null ? 0 : Math.max(0, srv.getCurrentPlayerCount() - 1);
            }
        });
    }

    /** Refreshes the page when the state changes (the toasts come from HostService's listener, on any screen). */
    public void updateScreen()
    {
        name.updateCursorCounter();
        HostService.State now = HostService.INSTANCE.state();
        if (now != shown)
        {
            shown = now;
            initGui();
        }
    }

    protected void keyTyped(char c, int key)
    {
        name.textboxKeyTyped(c, key);
        super.keyTyped(c, key);
    }

    protected void mouseClicked(int x, int y, int b)
    {
        name.mouseClicked(x, y, b);
        super.mouseClicked(x, y, b);
    }

    public void drawScreen(int mx, int my, float t)
    {
        drawRect(0, 0, width, height, 0xC8000000); // over Decimation's HUD, like its own menu pages
        int x = x0();
        drawRect(x, 48, x + 246, 236, 0xD0000000);
        drawString(fontRendererObj, I18n.format("deciworldgen.host.title"), x + 6, 52, 0xFFFFFF);
        drawString(fontRendererObj, "\u00a77" + I18n.format("deciworldgen.host.name"), x + 6, 61, 0xFFFFFF);
        name.drawTextBox();
        HostService.State state = HostService.INSTANCE.state();
        if (state == HostService.State.ONLINE || state == HostService.State.STARTING)
        {
            // the client's own player list: drawing must never walk the server thread's list (review: a crash risk)
            List<?> players = mc.getNetHandler() == null ? Collections.emptyList() : mc.getNetHandler().playerInfoList;
            String line = state == HostService.State.ONLINE ? "\u00a7a\u25cf \u00a7f" + I18n.format("deciworldgen.host.listed")
                                                            : "\u00a7e" + I18n.format("deciworldgen.host.starting");
            drawString(fontRendererObj, line + "   " + players.size() + "/" + max, x + 6, 92, 0xFFFFFF);
            drawString(fontRendererObj, "\u00a77" + I18n.format(online ? "deciworldgen.host.online" : "deciworldgen.host.offline")
                       + (commands ? ", " + I18n.format("deciworldgen.host.commands.me") : ""), x + 6, 103, 0xFFFFFF);
            int y = 118;
            for (Object o : players) // the players with their ping
            {
                net.minecraft.client.gui.GuiPlayerInfo p = (net.minecraft.client.gui.GuiPlayerInfo) o;
                drawString(fontRendererObj, p.name + " \u00a77" + p.responseTime + " ms", x + 6, y, 0xFFFFFF);
                y += 10;
            }
        }
        else
        {
            drawString(fontRendererObj, "\u00a77" + I18n.format("deciworldgen.host.who"), x + 6, 90, 0xFFFFFF);
            drawString(fontRendererObj, "\u00a77" + I18n.format("deciworldgen.host.max"), x + 6, 131, 0xFFFFFF);
            drawString(fontRendererObj, String.valueOf(max), x + 26, 144, 0xFFFFFF);
            drawString(fontRendererObj, "\u00a77" + I18n.format("deciworldgen.host.commands"), x + 100, 131, 0xFFFFFF);
        }
        drawString(fontRendererObj, "\u00a77" + I18n.format("deciworldgen.host.mods"), x + 6, 162, 0xFFFFFF);
        drawString(fontRendererObj, fontRendererObj.trimStringToWidth(mods(), 234), x + 6, 172, 0xFFFFFF);
        drawString(fontRendererObj, "\u00a77" + I18n.format("deciworldgen.host.route"), x + 6, 186, 0xFFFFFF);
        drawString(fontRendererObj, I18n.format("deciworldgen.host.relay"), x + 6, 196, 0xFFFFFF);
        super.drawScreen(mx, my, t);
    }

    private static String mods()
    {
        StringBuilder b = new StringBuilder();
        for (String[] m : ModSet.current().pairs())
        {
            b.append(b.length() == 0 ? "" : ", ").append(m[0]).append(' ').append(m[1]);
        }
        return b.toString();
    }

    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
