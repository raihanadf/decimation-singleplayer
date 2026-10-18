package net.decimation.worldgen.devtest;

import net.decimation.fixes.Deci;
import net.decimation.fixes.hosting.CommunityList;
import net.decimation.fixes.hosting.HostLan;
import net.decimation.fixes.hosting.HostScreen;
import net.decimation.fixes.hosting.HostService;
import net.decimation.fixes.hosting.ModSet;
import net.decimation.worldgen.DevAutoTest;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.WorldSettings;

/**
 * Website pictures of player hosting (docs/website.md, multiplayer section): this game hosts its world through the
 * deployed relay, then shoots the Host Online page while hosting and the Community page, which lists this game's own
 * world (one game, no joiner). Best with --window=1440x810. Stops hosting at the end. World name: worldname=NAME.
 */
public class HostShotsTest extends DevTestMode
{
    public String name() { return "hostshots"; }

    private int step, wait, seen = -1;

    public boolean client(Minecraft mc)
    {
        wait++;
        DevAutoTest.keepScreen = true;
        IntegratedServer s = (IntegratedServer) MinecraftServer.getServer();
        switch (step)
        {
            case 0:
                if (s == null || mc.thePlayer == null)
                {
                    return true;
                }
                String port = s.getPublic() ? HostLan.port() : s.shareToLAN(WorldSettings.GameType.SURVIVAL, false);
                HostLan.remember(port);
                HostService.Settings st = new HostService.Settings();
                st.name = System.getProperty("deciworldgen.autotest.worldname", "Survivors");
                st.online = true;
                st.max = 4;
                st.player = mc.getSession().getUsername();
                st.version = ModSet.ourVersion();
                HostLan.limit(s, st.max);
                final IntegratedServer srv = s;
                HostService.INSTANCE.start(st, Integer.parseInt(port), () -> Math.max(0, srv.getCurrentPlayerCount() - 1));
                next(1);
                return true;
            case 1:
                if (HostService.INSTANCE.state() != HostService.State.ONLINE && wait < 20 * 20)
                {
                    return true;
                }
                DevTestResults.check(name(), "hosting", HostService.INSTANCE.state(),
                                     HostService.INSTANCE.state() == HostService.State.ONLINE, "ONLINE");
                mc.gameSettings.guiScale = 3; // the website's pictures: GUI scale 3 at 1440 x 810, like guideshots
                mc.displayGuiScreen(new HostScreen(null));
                next(2);
                return true;
            case 2:
                if (wait < 40)
                {
                    return true;
                }
                DevTestUtil.screenshot(mc, name(), "hostshots_hostpage");
                CommunityList.open(mc);
                next(3);
                return true;
            case 3:
                if ((!Deci.isCommunityScreen(mc.currentScreen) || Deci.communityRowTexts(mc.currentScreen).isEmpty()) && wait < 20 * 20)
                {
                    return true;
                }
                if (seen < 0)
                {
                    seen = wait;
                }
                if (wait - seen < 80) // the row pings this world through the relay
                {
                    return true;
                }
                DevTestResults.check(name(), "own world listed", Deci.communityRowTexts(mc.currentScreen),
                                     !Deci.communityRowTexts(mc.currentScreen).isEmpty(), "a row");
                DevTestUtil.screenshot(mc, name(), "hostshots_community");
                HostService.INSTANCE.stop();
                net.decimation.fixes.hosting.HostOps.revoke(s);
                mc.gameSettings.guiScale = 2; // the dev client's own setting
                mc.displayGuiScreen(null);
                DevAutoTest.keepScreen = false;
                return false;
            default:
                return false;
        }
    }

    private void next(int s)
    {
        step = s;
        wait = 0;
    }
}
