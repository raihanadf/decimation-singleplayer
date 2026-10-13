package net.decimation.worldgen.devtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.decimation.fixes.Deci;
import net.decimation.fixes.UpdateScreen;
import net.decimation.fixes.Updater;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/**
 * Auto update end to end (fixes/Updater, UpdateScreen, UpdateSwap, PatchNoUpdate v2) against
 * tools/fake_release_server.py (start it first; port from -Pport / port=, default 8765): the check finds
 * the fake release, Decimation's update check answers true, the home menu shows its Update button
 * (update_menu), the button opens our screen, the download runs with a progress bar (update_progress),
 * is checked and ready (update_ready), and the swap helper replaces a stand in jar
 * (run/client/devtest/update_target/Decimation.jar) with the download.
 */
public class UpdateTest extends DevTestMode
{
    public String name() { return "update"; }

    private int frames, step, at;
    private boolean progressShot, done;
    private File target;

    public boolean client(Minecraft mc)
    {
        if (mc.thePlayer == null)
        {
            return true;
        }
        frames++;
        Updater.State s = Updater.state();
        switch (step)
        {
            case 0:
                String port = System.getProperty("deciworldgen.autotest.port", "8765");
                target = new File(mc.mcDataDir, "devtest/update_target/Decimation.jar");
                target.getParentFile().mkdirs();
                write(target, "OLDJAR".getBytes());
                new File(target.getPath() + ".download").delete();
                new File(target.getParentFile(), "update.log").delete();
                System.setProperty("deciworldgen.update.target", target.getPath());
                Updater.reset();
                Updater.setApi("http://127.0.0.1:" + port + "/api/releases");
                Updater.check();
                next(1);
                break;
            case 1:
                if (s == Updater.State.AVAILABLE || s == Updater.State.NONE || frames - at > 400)
                {
                    DevTestResults.check(name(), "check finds the release", s + " " + Updater.latest() + " (" + Updater.message() + ")",
                                         s == Updater.State.AVAILABLE && "9.9.9".equals(Updater.latest()), "AVAILABLE 9.9.9");
                    DevTestResults.check(name(), "Decimation's update check", Deci.updateRequired(), Deci.updateRequired(), "true");
                    net.decimation.worldgen.DevAutoTest.keepScreen = true;
                    mc.displayGuiScreen(Deci.homeMenu());
                    next(s == Updater.State.AVAILABLE ? 2 : 9);
                }
                break;
            case 2:
                if (frames - at == 40)
                {
                    DevTestUtil.screenshot(mc, name(), "update_menu");
                    GuiButton b = button(mc.currentScreen, 8);
                    DevTestResults.check(name(), "menu has the Update button", b == null ? "none" : b.displayString, b != null, "button 8");
                    if (b != null)
                    {
                        try
                        {
                            mc.currentScreen.getClass().getMethod("actionPerformed", GuiButton.class).invoke(mc.currentScreen, b);
                        }
                        catch (Exception e)
                        {
                            DevTestResults.value(name(), "click", e.toString());
                        }
                    }
                    DevTestResults.check(name(), "Update opens our screen", mc.currentScreen == null ? "none" : mc.currentScreen.getClass().getName(),
                                         mc.currentScreen instanceof UpdateScreen, "UpdateScreen");
                    next(3);
                }
                break;
            case 3:
                if (!progressShot && s == Updater.State.DOWNLOADING && Updater.downloaded() > Updater.size() / 3)
                {
                    progressShot = true;
                    DevTestUtil.screenshot(mc, name(), "update_progress");
                }
                if (s == Updater.State.READY || s == Updater.State.FAILED || frames - at > 2400)
                {
                    DevTestResults.check(name(), "download checked", s + " (" + Updater.message() + ")", s == Updater.State.READY, "READY");
                    next(4);
                }
                break;
            case 4:
                if (frames - at == 20)
                {
                    DevTestUtil.screenshot(mc, name(), "update_ready");
                    int code = Updater.swap(true);
                    byte[] head = head(target, 6);
                    boolean swapped = new String(head).equals("NEWJAR") && target.length() == Updater.size();
                    DevTestResults.check(name(), "swap helper replaced the jar", "exit " + code + ", starts " + new String(head) + ", "
                        + target.length() + " bytes", swapped, "NEWJAR, " + Updater.size() + " bytes");
                    next(9);
                }
                break;
            default:
                if (frames - at > 5)
                {
                    mc.displayGuiScreen(null);
                    net.decimation.worldgen.DevAutoTest.keepScreen = false;
                    System.clearProperty("deciworldgen.update.target");
                    Updater.reset();
                    done = true;
                }
        }
        return !done;
    }

    private void next(int s)
    {
        step = s;
        at = frames;
    }

    @SuppressWarnings("unchecked")
    private static GuiButton button(GuiScreen screen, int id)
    {
        try
        {
            java.lang.reflect.Field f = GuiScreen.class.getDeclaredField("buttonList");
            f.setAccessible(true);
            for (Object o : (java.util.List<Object>) f.get(screen))
            {
                if (((GuiButton) o).id == id)
                {
                    return (GuiButton) o;
                }
            }
        }
        catch (Exception e)
        {
            DevTestResults.value("update", "buttons", e.toString());
        }
        return null;
    }

    private static void write(File f, byte[] b)
    {
        try
        {
            FileOutputStream o = new FileOutputStream(f);
            o.write(b);
            o.close();
        }
        catch (Exception e)
        {
            DevTestResults.value("update", "write", e.toString());
        }
    }

    private static byte[] head(File f, int n)
    {
        byte[] b = new byte[n];
        try
        {
            FileInputStream in = new FileInputStream(f);
            int r = in.read(b);
            in.close();
            return r == n ? b : new byte[0];
        }
        catch (Exception e)
        {
            return new byte[0];
        }
    }
}
