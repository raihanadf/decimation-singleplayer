package net.decimation.worldgen;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

/**
 * Decimation doors instead of vanilla ones in everything we generate (user, 13 Oktober 2026: "door
 * never use vanilla ones, use decimation door instead on all world in decimation"). Slices and the
 * small schematic placer pass every block through {@link #swap}: a vanilla wooden or iron door becomes
 * a Decimation door of the same metadata (Decimation's doors copy BlockDoor: vanilla door metadata,
 * docs/prop_catalogue.md), styled by what the structure is, the same style for every door of one
 * structure (picked by its id). DeceasedCraft's converted buildings bring wooden and iron doors
 * (tools/lctranslate.py), our bases, camps and the refuge wooden ones. Doors written before this
 * stay vanilla (chunks are never rewritten).
 */
public final class Doors
{
    /** A plan that knows what kind of building it is (city buildings: the BuildingDress kind name). */
    public interface Styled
    {
        String doorStyle();
    }

    /** Wooden door styles per kind of building; iron doors become METAL. */
    private static final Map<String, String[]> STYLES = new HashMap<String, String[]>();
    private static final String[] METAL = {"Door_Metal_3", "Door_security_1"};

    static
    {
        STYLES.put("home", new String[] {"Door_Office_1", "Door_Blue_1", "Door_Orange_1", "Door_Green_1"});
        STYLES.put("office", new String[] {"Door_Office_1", "Door_Blue_3"});
        STYLES.put("shop", new String[] {"Door_Blue_3", "Door_Green_3", "Door_Orange_3"});
        STYLES.put("food", new String[] {"Door_Blue_3", "Door_Green_3", "Door_Orange_3"});
        STYLES.put("medical", new String[] {"Door_Green_1", "Door_Green_3", "Door_Emergency_1"});
        STYLES.put("police", new String[] {"Door_Blue_3", "Door_security_1"});
        STYLES.put("industry", new String[] {"Door_Metal_3", "Door_Emergency_2"});
        STYLES.put("military", new String[] {"Door_Metal_3", "Door_security_1", "Door_Emergency_2"});
        STYLES.put("lab", new String[] {"Door_security_1", "Door_InfektorSecurity_1"});
        STYLES.put("civic", new String[] {"Door_Office_1", "Door_Blue_3", "Door_Emergency_1"});
        STYLES.put("camp", new String[] {"Door_Office_1"});
        STYLES.put("refuge", new String[] {"Door_Emergency_2", "Door_Metal_3"});
    }

    private static final Map<String, Block> BLOCKS = new HashMap<String, Block>();

    private Doors()
    {
    }

    /** The block to write for b in plan p: a Decimation door for a vanilla one, else b itself. */
    public static Block swap(Block b, Plan p)
    {
        if (b != Blocks.wooden_door && b != Blocks.iron_door)
        {
            return b;
        }
        String id = p.id();
        return swap(b, p instanceof Styled ? ((Styled) p).doorStyle() : style(id), id);
    }

    /** The same for a structure without a plan (the small schematic placer): style by name, key by id. */
    public static Block swap(Block b, String style, String key)
    {
        if (b != Blocks.wooden_door && b != Blocks.iron_door)
        {
            return b;
        }
        String[] list = b == Blocks.iron_door ? METAL : STYLES.get(style);
        if (list == null)
        {
            list = STYLES.get("home");
        }
        long h = key.hashCode() * 0x9E3779B97F4A7C15L;
        Block door = block(list[(int) ((h >>> 33) % list.length)]);
        return door != null ? door : b;
    }

    /** Style of a structure by its plan id (LargeSites / LcCity name them). */
    private static String style(String id)
    {
        return id.startsWith("M") ? "military" : id.startsWith("S") ? "camp" : id.startsWith("lcz") ? "refuge" : "home";
    }

    private static synchronized Block block(String name)
    {
        if (!BLOCKS.containsKey(name))
        {
            Block b = Block.getBlockFromName("deci:" + name);
            BLOCKS.put(name, b == null || b == Blocks.air ? null : b);
        }
        return BLOCKS.get(name);
    }
}
