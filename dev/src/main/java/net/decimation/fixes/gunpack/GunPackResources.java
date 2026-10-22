package net.decimation.fixes.gunpack;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

/**
 * Gun package files as Decimation reads them: one resource pack of the "deci" domain (BModelLoader, GunItemRenderer,
 * GunAnimation and the item icons all go through Minecraft's resource manager) that serves only each package's own
 * paths (models/guns/CAT/NAME.bmodel, textures/model/guns/CAT/NAME/*, textures/items/gun/CAT/NAME.png,
 * animations/NAME/*), so a package can never cover a Decimation file, plus a generated deci:lang/en_US.lang with the
 * guns' and magazines' names (the language loads every pack's file of that name). Every deci lookup asks this pack
 * first (the last added): two map lookups, nothing read from disk until a file is opened. Client only.
 */
@SideOnly(Side.CLIENT)
public final class GunPackResources implements IResourcePack
{
    static final String LANG = "lang/en_US.lang";
    private static final Charset UTF8 = Charset.forName("UTF-8");
    /** deci path -> {source, path in the package}. */
    private final Map<String, Object[]> files = new HashMap<String, Object[]>();
    private final StringBuilder lang = new StringBuilder();
    private byte[] langBytes;

    void add(GunPack p, GunPackSource s)
    {
        serve(s, p.modelPath());
        serve(s, p.iconPath());
        serve(s, p.texturePath());
        for (String kind : new String[] {"Fire", "Rack", "Reload1", "SlideBack"})
        {
            serve(s, p.animationPath(kind));
        }
        for (String folder : new String[] {p.textureFolder(), p.animationFolder()})   // weapon skins, more animations
        {
            for (String f : s.list("assets/deci/" + folder))
            {
                serve(s, f.substring("assets/deci/".length()));
            }
        }
        lang.append("item.").append(p.name).append(".name=").append(p.display).append('\n');
        if (p.magazine != null)
        {
            String d = p.magazine.display != null ? p.magazine.display : p.magazine.capacity + "rnd " + p.display + " Magazine";
            lang.append("item.").append(p.magazine.name).append(".name=").append(d).append('\n');
        }
        langBytes = lang.toString().getBytes(UTF8);
    }

    private void serve(GunPackSource s, String deciPath)
    {
        if (s.has("assets/deci/" + deciPath) && !files.containsKey(deciPath))
        {
            files.put(deciPath, new Object[] {s, "assets/deci/" + deciPath});
        }
    }

    /** How many files it serves (dev test). */
    public int size()
    {
        return files.size();
    }

    /** The generated names (dev test). */
    public String langText()
    {
        return lang.toString();
    }

    @Override
    public InputStream getInputStream(ResourceLocation rl) throws IOException
    {
        if ("deci".equals(rl.getResourceDomain()))
        {
            if (LANG.equals(rl.getResourcePath()) && langBytes != null)
            {
                return new ByteArrayInputStream(langBytes);
            }
            Object[] f = files.get(rl.getResourcePath());
            if (f != null)
            {
                return ((GunPackSource) f[0]).open((String) f[1]);
            }
        }
        throw new FileNotFoundException(rl.toString());
    }

    @Override
    public boolean resourceExists(ResourceLocation rl)
    {
        return "deci".equals(rl.getResourceDomain())
            && (files.containsKey(rl.getResourcePath()) || langBytes != null && LANG.equals(rl.getResourcePath()));
    }

    @Override
    public Set<String> getResourceDomains()
    {
        return Collections.singleton("deci");
    }

    @Override
    public IMetadataSection getPackMetadata(IMetadataSerializer serializer, String section) throws IOException
    {
        return null;
    }

    @Override
    public BufferedImage getPackImage() throws IOException
    {
        throw new FileNotFoundException("pack.png");
    }

    @Override
    public String getPackName()
    {
        return "DeciRemnant gun packages";
    }
}
