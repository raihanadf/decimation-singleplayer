package net.decimation.fixes;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/**
 * Our own update screen, since v0.64.7 only for what Decimation's progress screen cannot say (UpdateProgress opens it):
 * a check that found nothing yet, or a failed download with its reason, "Try again" / "Back". It still shows a bar and
 * "Quit game" / "Later" if a download runs or is ready while it is open. Client only.
 */
public class UpdateScreen extends GuiScreen
{
    private static final int QUIT = 1, LATER = 2, RETRY = 3, BACK = 4;
    private Updater.State shown;

    public void initGui()
    {
        shown = null;
        if (Updater.state() == Updater.State.AVAILABLE)
        {
            Updater.download();
        }
        buttons();
    }

    private void buttons()
    {
        buttonList.clear();
        Updater.State s = Updater.state();
        int y = height / 2 + 40;
        if (s == Updater.State.READY)
        {
            buttonList.add(new GuiButton(QUIT, width / 2 - 102, y, 100, 20, "Quit game"));
            buttonList.add(new GuiButton(LATER, width / 2 + 2, y, 100, 20, "Later"));
        }
        else if (s == Updater.State.FAILED)
        {
            buttonList.add(new GuiButton(RETRY, width / 2 - 102, y, 100, 20, "Try again"));
            buttonList.add(new GuiButton(BACK, width / 2 + 2, y, 100, 20, "Back"));
        }
        else if (s != Updater.State.DOWNLOADING)
        {
            buttonList.add(new GuiButton(BACK, width / 2 - 50, y, 100, 20, "Back"));
        }
        shown = s;
    }

    public void updateScreen()
    {
        if (Updater.state() != shown)
        {
            buttons();
        }
    }

    protected void actionPerformed(GuiButton b)
    {
        switch (b.id)
        {
            case QUIT:
                mc.shutdown();
                break;
            case RETRY:
                Updater.download();
                buttons();
                break;
            default:
                mc.displayGuiScreen(Deci.homeMenu());
        }
    }

    protected void keyTyped(char c, int key)
    {
        if (key == 1 && Updater.state() != Updater.State.DOWNLOADING)
        {
            mc.displayGuiScreen(Deci.homeMenu());          // Escape
        }
    }

    public void drawScreen(int mx, int my, float partial)
    {
        drawDefaultBackground();
        Updater.State s = Updater.state();
        String title = Updater.latest() == null ? "Decimation update" : "Decimation update " + Updater.latest();
        drawCenteredString(fontRendererObj, title, width / 2, height / 2 - 50, 0xFFFFFF);
        String line;
        switch (s)
        {
            case DOWNLOADING:
                line = "Downloading " + (Updater.downloaded() >> 20) + " of " + (Updater.size() >> 20) + " MB";
                break;
            case READY:
                line = "Ready. Restart the game to play " + Updater.latest() + ".";
                break;
            case CHECKING:
                line = "Checking for updates...";
                break;
            default:
                line = Updater.message();
        }
        drawCenteredString(fontRendererObj, line, width / 2, height / 2 - 30, s == Updater.State.FAILED ? 0xFF6060 : 0xC0C0C0);
        if (s == Updater.State.DOWNLOADING || s == Updater.State.READY)
        {
            int w = 200, x0 = width / 2 - w / 2, y0 = height / 2 - 10;
            double f = s == Updater.State.READY ? 1 : Updater.size() <= 0 ? 0 : Math.min(1, Updater.downloaded() / (double) Updater.size());
            drawRect(x0 - 1, y0 - 1, x0 + w + 1, y0 + 11, 0xFF000000);
            drawRect(x0, y0, x0 + w, y0 + 10, 0xFF303030);
            drawRect(x0, y0, x0 + (int) (w * f), y0 + 10, 0xFF4CAF50);
            drawCenteredString(fontRendererObj, (int) (f * 100) + " %", width / 2, y0 + 1, 0xFFFFFF);
        }
        if (s == Updater.State.READY)
        {
            drawCenteredString(fontRendererObj, "Quit game puts the new version in place; Later does it when you close the game.",
                               width / 2, height / 2 + 14, 0x909090);
        }
        super.drawScreen(mx, my, partial);
    }

    public boolean doesGuiPauseGame()
    {
        return false;
    }
}
