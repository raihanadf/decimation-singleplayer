package net.decimation.fixes;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.FMLLog;

/**
 * Writes the graphics card and Java the game really runs on into the log at start (bug.md "Every Decimation
 * mob drawn solid black"): Minecraft 1.7.10 logs neither, and on a laptop with two GPUs Java may run on the
 * integrated one. Client only, on the client thread (the GL context exists before mods load).
 */
public final class GlInfo
{
    private GlInfo()
    {
    }

    public static void log()
    {
        try
        {
            FMLLog.info("[deciworldgen] graphics: %s | %s | OpenGL %s", GL11.glGetString(GL11.GL_RENDERER),
                        GL11.glGetString(GL11.GL_VENDOR), GL11.glGetString(GL11.GL_VERSION));
        }
        catch (Throwable e)
        {
            FMLLog.info("[deciworldgen] graphics: unknown (%s)", e);
        }
        FMLLog.info("[deciworldgen] java: %s %s (%s), %s, os %s %s", System.getProperty("java.vendor"),
                    System.getProperty("java.version"), System.getProperty("os.arch"), System.getProperty("java.home"),
                    System.getProperty("os.name"), System.getProperty("os.version"));
    }
}
