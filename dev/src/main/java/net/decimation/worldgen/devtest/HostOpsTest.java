package net.decimation.worldgen.devtest;

import com.mojang.authlib.GameProfile;
import cpw.mods.fml.relauncher.ReflectionHelper;
import java.util.UUID;
import net.decimation.fixes.hosting.HostOps;
import net.decimation.fixes.hosting.HostService;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.storage.WorldInfo;

/**
 * Host Online "Commands: only me" (docs/player_hosting.md section 12): while hosting, the host is an operator of the
 * world and nobody else is. The world's cheats and the dev client's commands for everyone are turned off for the test
 * (a hosted world shares with cheats off), then put back. Fields by MCP and SRG name: the test also runs in the release jar.
 */
public class HostOpsTest extends DevTestMode
{
    public String name() { return "hostops"; }

    public boolean client(Minecraft mc)
    {
        MinecraftServer s = MinecraftServer.getServer();
        EntityPlayerMP host = DevTestUtil.player();
        if (s == null || host == null)
        {
            return true;
        }
        ServerConfigurationManager scm = s.getConfigurationManager();
        WorldInfo info = s.worldServers[0].getWorldInfo();
        boolean all = ReflectionHelper.getPrivateValue(ServerConfigurationManager.class, scm, "commandsAllowedForAll", "field_72407_n");
        boolean cheats = ReflectionHelper.getPrivateValue(WorldInfo.class, info, "allowCommands", "field_76110_t");
        GameProfile me = host.getGameProfile();
        GameProfile joiner = new GameProfile(UUID.nameUUIDFromBytes("OfflinePlayer:kucing".getBytes()), "kucing");
        try
        {
            ReflectionHelper.setPrivateValue(ServerConfigurationManager.class, scm, false, "commandsAllowedForAll", "field_72407_n");
            ReflectionHelper.setPrivateValue(WorldInfo.class, info, false, "allowCommands", "field_76110_t");
            HostOps.revoke(s); // a leftover of an earlier run
            DevTestResults.check(name(), "commands on by default", new HostService.Settings().commands,
                                 new HostService.Settings().commands, "true");
            boolean before = host.canCommandSenderUseCommand(2, "tp");
            DevTestResults.check(name(), "host without cheats before hosting", before, !before, "no /tp");
            HostOps.grant(s, host.getCommandSenderName());
            boolean during = host.canCommandSenderUseCommand(2, "tp");
            DevTestResults.check(name(), "host while hosting", during, during, "/tp allowed");
            boolean other = scm.func_152596_g(joiner);
            DevTestResults.check(name(), "joiner while hosting", other, !other, "no commands");
            HostOps.revoke(s);
            boolean after = host.canCommandSenderUseCommand(2, "tp");
            DevTestResults.check(name(), "host after hosting stops", after, !after, "no /tp");
            DevTestResults.check(name(), "marker file gone", HostOps.MARK.exists(), !HostOps.MARK.exists(), "false");
            scm.func_152605_a(me); // an operator already: hosting must not take it away
            HostOps.grant(s, host.getCommandSenderName());
            HostOps.revoke(s);
            boolean kept = scm.func_152596_g(me);
            DevTestResults.check(name(), "an operator stays one", kept, kept, "still an operator");
            scm.func_152610_b(me);
        }
        finally
        {
            ReflectionHelper.setPrivateValue(ServerConfigurationManager.class, scm, all, "commandsAllowedForAll", "field_72407_n");
            ReflectionHelper.setPrivateValue(WorldInfo.class, info, cheats, "allowCommands", "field_76110_t");
        }
        return false;
    }
}
