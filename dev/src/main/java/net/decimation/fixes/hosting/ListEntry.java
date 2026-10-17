package net.decimation.fixes.hosting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** One world on the public list, as the relay's GET /list answers it (spec 3.3). */
public final class ListEntry
{
    public String room, name, host, version, route;
    public boolean online;
    public int players, max;
    public long since;
    public List<List<String>> mods = new ArrayList<List<String>>();

    public static List<ListEntry> parse(String json)
    {
        try
        {
            ListEntry[] a = new com.google.gson.Gson().fromJson(json, ListEntry[].class);
            List<ListEntry> l = new ArrayList<ListEntry>();
            for (ListEntry e : a == null ? new ListEntry[0] : a)
            {
                if (e != null && e.room != null && e.name != null)
                {
                    l.add(e);
                }
            }
            return l;
        }
        catch (RuntimeException e)
        {
            return Collections.emptyList();
        }
    }

    /** The mods as a ModSet. */
    public ModSet modSet()
    {
        List<String[]> l = new ArrayList<String[]>();
        for (List<String> m : mods == null ? Collections.<List<String>>emptyList() : mods)
        {
            if (m != null && m.size() == 2)
            {
                l.add(new String[] {m.get(0), m.get(1)});
            }
        }
        return ModSet.of(l);
    }

    /** A host's text for the list: formatting codes removed, trimmed, at most 32 characters; null when nothing is left. */
    public static String cleanName(String raw)
    {
        if (raw == null)
        {
            return null;
        }
        String s = raw.replaceAll("§.", "").trim();
        if (s.length() > 32)
        {
            s = s.substring(0, 32).trim();
        }
        return s.isEmpty() ? null : s;
    }
}
