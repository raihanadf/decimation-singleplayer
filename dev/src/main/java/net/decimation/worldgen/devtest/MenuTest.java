package net.decimation.worldgen.devtest;

import java.io.File;

import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;

/**
 * Decimation's home menu never asks for an update (tools/patches/PatchNoUpdate.java, bug.md): run it
 * with mods/DecimationVoiceChat.jar moved aside to see what a player without the voice chat jar gets.
 * Records whether the jar is there and what the update check answers, and photographs the home menu
 * (menu_home: "Play", not "Update").
 */
public class MenuTest extends DevTestMode
{
    public String name() { return "menu"; }

    private int frames;
    private boolean done;

    public boolean client(Minecraft mc)
    {
        if (mc.thePlayer == null)
        {
            return true;
        }
        if (frames == 0)
        {
            boolean voice = new File("mods/DecimationVoiceChat.jar").exists();
            DevTestResults.value(name(), "mods/DecimationVoiceChat.jar present", voice);
            DevTestResults.check(name(), "update required", Deci.updateRequired(), !Deci.updateRequired(), "false");
            // the jar an update replaces (fixes/Updater.target): the running jar in a shipped game, none in a dev folder
            boolean dev = Boolean.TRUE.equals(net.minecraft.launchwrapper.Launch.blackboard.get("fml.deobfuscatedEnvironment"));
            File target = net.decimation.fixes.Updater.target();
            DevTestResults.check(name(), "update target", target == null ? "none" : target.getPath(),
                                 dev ? target == null : target != null && target.isFile() && target.getName().endsWith(".jar"),
                                 dev ? "none (dev folder)" : "the running jar (mods/Decimation.jar)");
            net.decimation.worldgen.DevAutoTest.keepScreen = true;
            mc.displayGuiScreen(Deci.homeMenu());
        }
        if (++frames == 60)
        {
            DevTestUtil.screenshot(mc, name(), "menu_home");
            net.decimation.worldgen.DevAutoTest.keepScreen = false;
            mc.displayGuiScreen(null);
            done = true;
        }
        return !done;
    }
}
