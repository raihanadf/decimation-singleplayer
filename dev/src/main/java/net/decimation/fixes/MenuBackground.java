package net.decimation.fixes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Decimation's main menu background as a slideshow of real scenes of our world (user, 17 Oktober 2026: "replace with
 * real scene", "make the background image changed every 7 second"; docs/main_menu.md). RenderUtil.drawMenuBackground
 * draws its one picture through here (PatchMenuBackground), with its own sway, slow zoom and mouse parallax already
 * applied; this draws the picture of the current 7 seconds and fades the next one in over the last second and a bit.
 * The pictures are Decimation's menu backgrounds background0.png, background1.png, ... (our scenes, written into the
 * jar by tools/menu_assets.py; Decimation's own three when that was not run), starting with the one Decimation picked
 * for this launch. Client only.
 */
@SideOnly(Side.CLIENT)
public final class MenuBackground
{
    /** Time each picture is on screen, and the fade into the next one at its end. */
    static final long SHOW = 7000, FADE = 1200;
    private static ResourceLocation[] slides;
    private static long start;

    private MenuBackground()
    {
    }

    /** Draws the menu background picture: RenderUtil.drawTextureCentered's arguments, `picked` is Decimation's choice. */
    public static void draw(double x, double y, ResourceLocation picked, float w, float h, float zoom)
    {
        if (slides == null)
        {
            slides = find(picked);
            start = System.currentTimeMillis();
        }
        long t = System.currentTimeMillis() - start;
        int i = (int) (t / SHOW % slides.length);
        long into = t % SHOW;
        Deci.drawCentered(x, y, slides[i], w, h, zoom);
        if (slides.length > 1 && into > SHOW - FADE)
        {
            Deci.drawCentered(x, y, slides[(i + 1) % slides.length], w, h, zoom, (into - (SHOW - FADE)) / (double) FADE);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    /** Pictures in the show and the one on screen now (dev tests); 0 and -1 before the menu was drawn. */
    public static int[] shown()
    {
        ResourceLocation[] s = slides;
        return s == null ? new int[] {0, -1} : new int[] {s.length, (int) ((System.currentTimeMillis() - start) / SHOW % s.length)};
    }

    /** background0.png, background1.png, ... while they exist, rotated so the launch's own pick comes first. */
    private static ResourceLocation[] find(ResourceLocation picked)
    {
        List<ResourceLocation> all = new ArrayList<ResourceLocation>();
        for (int n = 0; n < 16; n++)
        {
            ResourceLocation r = new ResourceLocation("deci", "textures/gui/menu/background" + n + ".png");
            try
            {
                Minecraft.getMinecraft().getResourceManager().getResource(r);
            }
            catch (IOException missing)
            {
                break;
            }
            all.add(r);
        }
        int first = all.indexOf(picked);
        if (first > 0)
        {
            Collections.rotate(all, -first);
        }
        if (all.isEmpty())
        {
            all.add(picked);
        }
        return all.toArray(new ResourceLocation[0]);
    }
}
