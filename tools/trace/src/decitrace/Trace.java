package decitrace;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The call recorder of the dev call logger (docs/call_trace.md). Lives on the boot class path (the agent jar's
 * Boot-Class-Path), so the game's class loaders all see this one copy. TraceAgent gives every method of the traced
 * packages an id and puts a call to enter(id) at its start; enter costs one volatile read while recording is off.
 * Only JDK classes here: the boot class path sees nothing else.
 */
public final class Trace
{
    private static final Object LOCK = new Object();
    /** Recording: enter() looks at nothing else while this is false. */
    public static volatile boolean on;

    private static String[] names = new String[8192];
    private static int count;

    private static String[] prefixes = new String[0];
    private static String prefixText = "";
    private static boolean[] wanted = new boolean[0];
    private static int[] calls = new int[0];
    private static long t0;
    private static int matched;

    private static long[] evTime = new long[0];
    private static int[] evId = new int[0], evThread = new int[0], evRepeat = new int[0];
    private static int evCount, evMax;
    private static boolean full;

    private static final Map<Thread, Integer> threads = new IdentityHashMap<Thread, Integer>();
    private static final List<String> threadNames = new ArrayList<String>();
    /** Per thread: index of its last event, so a call repeating back to back on one thread only counts up. */
    private static int[] lastEvent = new int[0];

    private Trace()
    {
    }

    /** Called by TraceAgent for every method it marks: "owner/Class.name(desc)". */
    public static int register(String name)
    {
        synchronized (LOCK)
        {
            if (count == names.length)
            {
                names = Arrays.copyOf(names, count * 2);
            }
            names[count] = name;
            if (on)
            {
                // a class loaded while recording: size the tables and judge it like the others
                grow(count + 1);
                if (matches(name))
                {
                    wanted[count] = true;
                    matched++;
                }
            }
            return count++;
        }
    }

    public static void enter(int id)
    {
        if (on)
        {
            hit(id);
        }
    }

    private static void hit(int id)
    {
        long now = System.nanoTime();
        synchronized (LOCK)
        {
            if (!on || id >= wanted.length || !wanted[id])
            {
                return;
            }
            calls[id]++;
            Thread t = Thread.currentThread();
            Integer ti = threads.get(t);
            if (ti == null)
            {
                ti = threadNames.size();
                threads.put(t, ti);
                threadNames.add(t.getName());
                if (ti >= lastEvent.length)
                {
                    int old = lastEvent.length;
                    lastEvent = Arrays.copyOf(lastEvent, Math.max(16, old * 2));
                    Arrays.fill(lastEvent, old, lastEvent.length, -1);
                }
            }
            int last = lastEvent[ti];
            if (last >= 0 && evId[last] == id)
            {
                evRepeat[last]++;
                return;
            }
            if (evCount >= evMax)
            {
                full = true;
                return;
            }
            evTime[evCount] = now;
            evId[evCount] = id;
            evThread[evCount] = ti;
            evRepeat[evCount] = 1;
            lastEvent[ti] = evCount++;
        }
    }

    /**
     * Starts recording calls of methods whose name starts with one of the comma separated prefixes ("deci.aE",
     * "deci/n/d$a", "net.decimation.fixes.NpcShots.apply"; dots and slashes alike). Returns a line for the user.
     */
    public static String start(String prefixList, int maxEvents)
    {
        synchronized (LOCK)
        {
            List<String> p = new ArrayList<String>();
            for (String s : prefixList.split(","))
            {
                if (s.trim().length() > 0)
                {
                    p.add(s.trim().replace('/', '.'));
                }
            }
            prefixes = p.toArray(new String[0]);
            prefixText = prefixList;
            wanted = new boolean[0];
            calls = new int[0];
            grow(count);
            matched = 0;
            for (int i = 0; i < count; i++)
            {
                if (matches(names[i]))
                {
                    wanted[i] = true;
                    matched++;
                }
            }
            evMax = Math.max(1000, maxEvents);
            evTime = new long[evMax];
            evId = new int[evMax];
            evThread = new int[evMax];
            evRepeat = new int[evMax];
            evCount = 0;
            full = false;
            threads.clear();
            threadNames.clear();
            lastEvent = new int[0];
            t0 = System.nanoTime();
            on = true;
            return "tracing " + matched + " of " + count + " marked methods (" + prefixList + ")";
        }
    }

    /** Stops recording and writes the trace to path. Returns a line for the user. */
    public static String stop(String path)
    {
        synchronized (LOCK)
        {
            on = false;
            long total = 0;
            for (int c : calls)
            {
                total += c;
            }
            File f = new File(path);
            if (f.getParentFile() != null)
            {
                f.getParentFile().mkdirs();
            }
            try
            {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
                out.println("# decitrace " + prefixText);
                out.println("# marked " + count + " methods, traced " + matched + ", calls " + total + ", events " + evCount
                            + (full ? " (event list FULL at " + evMax + ": later calls only counted)" : ""));
                out.println("## events: ms since start, thread, method, back to back repeats on that thread");
                for (int i = 0; i < evCount; i++)
                {
                    out.println(String.format("%10.3f  %-24s %s%s", (evTime[i] - t0) / 1e6, threadNames.get(evThread[i]),
                                              names[evId[i]], evRepeat[i] > 1 ? "  x" + evRepeat[i] : ""));
                }
                out.println("## calls per method, most first");
                Integer[] order = new Integer[calls.length];
                for (int i = 0; i < order.length; i++)
                {
                    order[i] = i;
                }
                final int[] c = calls;
                Arrays.sort(order, new java.util.Comparator<Integer>()
                {
                    public int compare(Integer a, Integer b)
                    {
                        return c[b] - c[a];
                    }
                });
                for (Integer i : order)
                {
                    if (c[i] == 0)
                    {
                        break;
                    }
                    out.println(String.format("%9d  %s", c[i], names[i]));
                }
                out.close();
            }
            catch (Exception e)
            {
                return "trace stopped, cannot write " + f + ": " + e;
            }
            return "trace " + f.getAbsolutePath() + ": " + total + " calls, " + evCount + " events" + (full ? " (full)" : "");
        }
    }

    private static boolean matches(String name)
    {
        String dotted = name.replace('/', '.');
        for (String p : prefixes)
        {
            if (dotted.startsWith(p))
            {
                return true;
            }
        }
        return false;
    }

    private static void grow(int n)
    {
        if (wanted.length < n)
        {
            int size = Math.max(n, names.length);
            wanted = Arrays.copyOf(wanted, size);
            calls = Arrays.copyOf(calls, size);
        }
    }
}
