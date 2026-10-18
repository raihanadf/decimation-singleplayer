package net.decimation.fixes.hosting;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraftforge.event.world.WorldEvent;

/**
 * Host Online "Commands: only me" (docs/player_hosting.md section 12): a hosted world is shared with cheats off, so
 * nobody has commands, the host included. While hosting, the host is made an operator of the world (Minecraft's ops
 * list, saved as ops.json in the game folder); joiners never are. The entry we added is written to MARK, so a game that
 * crashed while hosting takes it away again on the next start or stop; a host who was an operator already stays one.
 */
public final class HostOps
{
    public static final File MARK = new File("deciworldgen_hostop.txt");

    public static void grant(MinecraftServer s, String name)
    {
        revoke(s); // a leftover of a game that ended while hosting
        ServerConfigurationManager scm = s.getConfigurationManager();
        EntityPlayerMP p = scm.func_152612_a(name);
        if (p == null || scm.func_152603_m().func_152683_b(p.getGameProfile()) != null)
        {
            return; // not in the world, or an operator already: nothing of ours to add or take away later
        }
        GameProfile g = p.getGameProfile();
        scm.func_152605_a(g);
        try
        {
            Files.write(MARK.toPath(), (g.getId() + "\n" + g.getName() + "\n").getBytes(StandardCharsets.UTF_8));
        }
        catch (IOException e)
        {
            // the entry still goes when hosting stops in this game
        }
    }

    /** Takes away the operator entry grant added (if any). */
    public static void revoke(MinecraftServer s)
    {
        if (!MARK.exists())
        {
            return;
        }
        try
        {
            List<String> l = Files.readAllLines(MARK.toPath(), StandardCharsets.UTF_8);
            if (s != null && l.size() >= 2)
            {
                s.getConfigurationManager().func_152610_b(new GameProfile(UUID.fromString(l.get(0).trim()), l.get(1).trim()));
            }
        }
        catch (Exception e)
        {
            // a broken marker: nothing we can take away
        }
        MARK.delete();
    }

    /** Leaving the world ends hosting: the host's operator entry goes with it. */
    @SubscribeEvent
    public void onUnload(WorldEvent.Unload e)
    {
        if (!e.world.isRemote && e.world.provider.dimensionId == 0)
        {
            revoke(MinecraftServer.getServer());
        }
    }
}
