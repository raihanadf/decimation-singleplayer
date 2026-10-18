package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.network.internal.NetworkModHolder;
import cpw.mods.fml.relauncher.Side;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OptionalModsTest
{
    /** gvc's holder as Forge makes it for a mod without a version range: Forge's own default checker. */
    private static NetworkModHolder gvcHolder() throws Exception
    {
        ModMetadata md = new ModMetadata();
        md.modId = "gvc";
        md.version = "0.6.1";
        NetworkModHolder h = new NetworkModHolder(new DummyModContainer(md));
        Class<?> c = Class.forName("cpw.mods.fml.common.network.internal.NetworkModHolder$DefaultNetworkChecker");
        Constructor<?> k = c.getDeclaredConstructor(NetworkModHolder.class);
        k.setAccessible(true);
        Field f = NetworkModHolder.class.getDeclaredField("checker");
        f.setAccessible(true);
        f.set(h, k.newInstance(h));
        return h;
    }

    private static Map<String, String> remote(String... idVersion)
    {
        Map<String, String> m = new HashMap<String, String>();
        for (int i = 0; i < idVersion.length; i += 2)
        {
            m.put(idVersion[i], idVersion[i + 1]);
        }
        return m;
    }

    @Test
    void forgeRefusesAJoinerWithoutVoiceChat() throws Exception
    {
        assertFalse(gvcHolder().check(Collections.<String, String>emptyMap(), Side.CLIENT));
    }

    @Test
    void relaxedLetsAJoinerWithoutVoiceChatIn() throws Exception
    {
        NetworkModHolder h = gvcHolder();
        OptionalMods.relax(h, "gvc");
        assertTrue(h.check(Collections.<String, String>emptyMap(), Side.CLIENT));
    }

    @Test
    void relaxedKeepsTheVersionCheck() throws Exception
    {
        NetworkModHolder h = gvcHolder();
        OptionalMods.relax(h, "gvc");
        assertTrue(h.check(remote("gvc", "0.6.1"), Side.CLIENT));
        assertFalse(h.check(remote("gvc", "0.5.0"), Side.CLIENT));
    }
}
