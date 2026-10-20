package net.decimation.fixes.gunsmith;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;

/**
 * The Gunsmith screen (docs/superpowers/specs/2026-10-21-gunsmith-screen-design.md). Its drawing comes in Task 5 of the
 * plan; for now the container's slots only, closed by Esc, E or Decimation's attach key (I).
 */
public class GunsmithScreen extends GuiContainer
{
    private static final String ATTACH_KEY = "key.decimation.gui.attach";
    final GunsmithContainer container;
    private int attachKey = -1;

    public GunsmithScreen(GunsmithContainer container)
    {
        super(container);
        this.container = container;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partial, int mouseX, int mouseY)
    {
    }

    @Override
    protected void keyTyped(char c, int key)
    {
        if (key != 0 && key == attachKey())
        {
            mc.thePlayer.closeScreen();
            return;
        }
        super.keyTyped(c, key);
    }

    /** Decimation's attach key as the player bound it (found by its name once). */
    private int attachKey()
    {
        if (attachKey < 0)
        {
            attachKey = 0;
            for (KeyBinding k : mc.gameSettings.keyBindings)
            {
                if (ATTACH_KEY.equals(k.getKeyDescription()))
                {
                    attachKey = k.getKeyCode();
                }
            }
        }
        return attachKey;
    }
}
