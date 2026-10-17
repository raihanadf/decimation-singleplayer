package net.decimation.fixes.hosting;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import java.util.List;
import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;

/**
 * The pause menu's Host Online button (in Server Status' place) and the LAN globe both open HostScreen
 * (docs/superpowers/specs/2026-10-18-player-hosting-part1-design.md 3.1); the heartbeat runs on the client tick.
 */
public final class HostMenu
{
    public static final int ID_HOST = 9300;
    private static final int SERVER_STATUS = 11, LAN = 25;

    public HostMenu()
    {
        // full or failed hosting is told once, whatever screen is open (review: the toast showed only on the Host page)
        HostService.INSTANCE.setStateListener(new HostService.StateListener()
        {
            public void changed(final HostService.State state, final String error)
            {
                Minecraft.getMinecraft().func_152344_a(new Runnable()
                {
                    public void run()
                    {
                        Deci.toast(state == HostService.State.FULL_TODAY ? I18n.format("deciworldgen.host.full")
                                                                          : I18n.format("deciworldgen.host.failed", error));
                    }
                });
            }
        });
    }

    @SubscribeEvent
    public void onInit(GuiScreenEvent.InitGuiEvent.Post e)
    {
        Minecraft mc = Minecraft.getMinecraft();
        if (!Deci.isHomeMenu(e.gui) || mc.theWorld == null || !mc.isSingleplayer())
        {
            return;
        }
        @SuppressWarnings("unchecked")
        List<GuiButton> buttons = e.buttonList;
        for (int i = 0; i < buttons.size(); i++)
        {
            GuiButton b = buttons.get(i);
            if (b.id == SERVER_STATUS)
            {
                boolean on = HostService.INSTANCE.state() == HostService.State.ONLINE;
                buttons.set(i, Deci.menuButton(ID_HOST, b.xPosition, b.yPosition, b.width, b.height,
                                               (on ? "§a" : "") + I18n.format("deciworldgen.host.button"),
                                               I18n.format("deciworldgen.host.tooltip")));
            }
        }
    }

    @SubscribeEvent
    public void onClick(GuiScreenEvent.ActionPerformedEvent.Pre e)
    {
        if (Deci.isHomeMenu(e.gui) && Minecraft.getMinecraft().isSingleplayer() && (e.button.id == ID_HOST || e.button.id == LAN))
        {
            e.setCanceled(true);
            Minecraft.getMinecraft().displayGuiScreen(new HostScreen(e.gui));
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e)
    {
        if (e.phase == TickEvent.Phase.END)
        {
            HostService.INSTANCE.tick(System.currentTimeMillis());
        }
    }
}
