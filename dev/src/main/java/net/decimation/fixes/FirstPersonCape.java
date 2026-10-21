package net.decimation.fixes;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.RenderPlayerEvent;

/**
 * A player with a cape (Mojang or OptiFine) saw it in front of the camera in first person (user 22 Oktober 2026):
 * Decimation draws your own body in first person (PlayerModelSwapper deci.J.e on RenderWorldLastEvent, with its own
 * RenderPlayer deci.J.e$a), and that renderer runs vanilla's renderEquippedItems whenever no gun is held, cape included.
 * Vanilla never draws its own player in first person, so the cape is left out for that renderer only; third person,
 * the inventory preview and other players keep it.
 */
@SideOnly(Side.CLIENT)
public final class FirstPersonCape
{
    @SubscribeEvent
    public void onSpecials(RenderPlayerEvent.Specials.Pre e)
    {
        if (Deci.isFirstPersonBodyRenderer(e.renderer))
        {
            e.renderCape = false;
        }
    }
}
