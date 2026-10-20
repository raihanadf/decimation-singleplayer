package net.decimation.fixes.gunsmith;

import net.minecraft.entity.player.EntityPlayer;

/** The client's way in: only the client calls it, so the dedicated server never loads GunsmithScreen (a GUI class). */
public final class GunsmithClient
{
    private GunsmithClient()
    {
    }

    /** The Gunsmith screen over a container of the client player's own (FML gives it the server's window id). */
    public static Object screen(EntityPlayer player)
    {
        return new GunsmithScreen(new GunsmithContainer(player));
    }
}
