package net.decimation.worldgen.devtest;

import net.decimation.fixes.Deci;
import net.decimation.fixes.hosting.HostMenu;
import net.decimation.fixes.hosting.HostScreen;
import net.decimation.worldgen.DevAutoTest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

/**
 * Player hosting part 1 (docs/superpowers/plans/2026-10-18-player-hosting-part1.md Task 9): the pause menu shows Host
 * Online in Server Status' place; it and the LAN globe open the Host Online page.
 */
public class HostMenuTest extends DevTestMode
{
    public String name() { return "hostmenu"; }

    private int step, wait;

    private static String screen(Minecraft mc)
    {
        return mc.currentScreen == null ? "none" : mc.currentScreen.getClass().getSimpleName();
    }

    public boolean client(Minecraft mc)
    {
        DevAutoTest.keepScreen = true;
        wait++;
        switch (step)
        {
            case 0:
                mc.displayGuiScreen(Deci.homeMenu());
                step = 1;
                wait = 0;
                return true;
            case 1:
            {
                if (wait < 10) return true;
                GuiButton host = DevTestUtil.byId(mc.currentScreen, HostMenu.ID_HOST);
                DevTestResults.check(name(), "Host Online button in the pause menu", host == null ? "none" : DevTestUtil.label(host),
                                     host != null && DevTestUtil.label(host).contains("Host Online"), "Host Online >>");
                DevTestResults.check(name(), "Server Status gone from the pause menu", DevTestUtil.byId(mc.currentScreen, 11) == null,
                                     DevTestUtil.byId(mc.currentScreen, 11) == null, "true");
                if (host != null) DevTestUtil.click(mc.currentScreen, host);
                step = 2;
                wait = 0;
                return true;
            }
            case 2:
                if (wait < 10) return true;
                DevTestResults.check(name(), "Host Online opens its page", screen(mc), mc.currentScreen instanceof HostScreen, "HostScreen");
                GuiButton mojang = DevTestUtil.byId(mc.currentScreen, 4); // HostScreen ONLINE
                String chosen = mojang == null ? "none" : mojang.displayString;
                DevTestResults.check(name(), "new hosts default to Mojang accounts only", chosen,
                                     chosen.startsWith("\u00a7a") && chosen.contains("Mojang"), "Mojang accounts only, chosen (user, 18 Oktober 2026)");
                GuiButton cmd = DevTestUtil.byId(mc.currentScreen, 8); // HostScreen COMMANDS
                String cmdText = cmd == null ? "none" : cmd.displayString;
                DevTestResults.check(name(), "new hosts default to commands for the host only", cmdText,
                                     cmdText.contains("only me"), "Commands: only me (user, 18 Oktober 2026)");
                GuiButton route = DevTestUtil.byId(mc.currentScreen, 9); // HostScreen ROUTE
                String routeText = route == null ? "none" : route.displayString;
                DevTestResults.check(name(), "new hosts default to direct + relay", routeText, routeText.contains("Direct"),
                                     "Direct + relay (user, 18 Oktober 2026)");
                if (route != null) // the switch flips to relay only and back
                {
                    DevTestUtil.click(mc.currentScreen, route);
                    GuiButton flipped = DevTestUtil.byId(mc.currentScreen, 9);
                    String flippedText = flipped == null ? "none" : flipped.displayString;
                    DevTestResults.check(name(), "the connection switch offers relay only", flippedText,
                                         flippedText.contains("Relay only"), "Relay only");
                    if (flipped != null)
                    {
                        DevTestUtil.click(mc.currentScreen, flipped);
                    }
                }
                DevTestUtil.screenshot(mc, name(), "host_page");
                mc.displayGuiScreen(Deci.homeMenu());
                step = 3;
                wait = 0;
                return true;
            case 3:
            {
                if (wait < 10) return true;
                GuiButton lan = DevTestUtil.byId(mc.currentScreen, 25);
                if (lan != null) DevTestUtil.click(mc.currentScreen, lan);
                step = 4;
                wait = 0;
                return true;
            }
            case 4:
                if (wait < 10) return true;
                DevTestResults.check(name(), "the LAN globe opens the Host Online page", screen(mc), mc.currentScreen instanceof HostScreen,
                                     "HostScreen");
                mc.displayGuiScreen(null);
                DevAutoTest.keepScreen = false;
                return false;
        }
        return false;
    }
}
