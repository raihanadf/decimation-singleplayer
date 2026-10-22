package net.decimation.fixes.gunpack;

import java.util.List;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.decimation.fixes.Deci;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.item.Item;

/** The client's part of GunPacks: the package files served to Decimation, the like gun's sounds and hands. */
@SideOnly(Side.CLIENT)
public final class GunPacksClient
{
    private static GunPackResources installed;

    private GunPacksClient()
    {
    }

    /** The packages' resource pack, or null when none was installed (dev test). */
    public static GunPackResources resources()
    {
        return installed;
    }

    /**
     * Serves the packages' files through Minecraft's resource manager now (GunItem's constructor loads the model through
     * it) and after every reload (Minecraft's default packs, the list FML adds mod jars to: field defaultResourcePacks).
     */
    @SuppressWarnings("unchecked")
    static void install(List<GunPacks.Loaded> packs)
    {
        GunPackResources res = new GunPackResources();
        for (GunPacks.Loaded l : packs)
        {
            res.add(l.pack, l.source);
        }
        Minecraft mc = Minecraft.getMinecraft();
        try
        {
            List<IResourcePack> defaults = (List<IResourcePack>) ReflectionHelper.getPrivateValue(Minecraft.class, mc,
                                                                                                "defaultResourcePacks", "field_110449_ao");
            defaults.add(res);
        }
        catch (RuntimeException e)
        {
            FMLLog.warning("[deciworldgen] gun packages: Minecraft's resource pack list not found (%s): files served until "
                           + "the next resource reload only", e);
        }
        IResourceManager rm = mc.getResourceManager();
        if (rm instanceof SimpleReloadableResourceManager)
        {
            ((SimpleReloadableResourceManager) rm).reloadResourcePack(res);
        }
        installed = res;
        FMLLog.info("[deciworldgen] gun packages: %d files served to Decimation", res.size());
    }

    /** The like gun's sound files for this gun (Deci.useGunSounds: no audio copied). */
    static int sounds(GunPack p, Item like)
    {
        return Deci.useGunSounds(p.name, p.category, GameRegistry.findUniqueIdentifierFor(like).name, Deci.gunCategory(like));
    }

    /** A barrel attachment moved on this gun (points.barrelShift), as NewGuns moves the MAC-10's suppressor. */
    static boolean barrel(Item gun, String attachment, double[] d)
    {
        return Deci.offsetAttachment(gun, attachment, (float) d[0], (float) d[1], (float) d[2]);
    }

    /** The like gun's hand poses when the package's model sets neither hand (no rhPos / lhPos line). */
    static boolean hands(Item gun, Item like, PackModel model)
    {
        if (model.header("rhPos") != null || model.header("lhPos") != null)
        {
            return false;
        }
        float[] h = Deci.gunHands(like);
        return h != null && Deci.setGunHands(gun, h);
    }
}
