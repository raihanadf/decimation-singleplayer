package net.decimation.worldgen.devtest;

import java.io.File;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import cpw.mods.fml.common.FMLLog;

/**
 * Our side of the dev call logger (docs/call_trace.md): starts and stops the recorder decitrace.Trace, which is on
 * the boot class path only when the game runs with the agent (`gradlew runClient -Ptrace`, tools/trace/build.py).
 * Without the agent every call answers that it is missing. Traces go to trace/ in the game folder (dev/run/client).
 */
public final class CallTrace
{
    private static Method start, stop;
    private static volatile Thread timer;

    private CallTrace()
    {
    }

    /** True when the game runs with the agent. */
    public static boolean available()
    {
        if (start == null)
        {
            try
            {
                Class<?> t = Class.forName("decitrace.Trace", false, null);
                start = t.getMethod("start", String.class, int.class);
                stop = t.getMethod("stop", String.class);
            }
            catch (Exception e)
            {
                return false;
            }
        }
        return true;
    }

    /** Starts recording the methods under the comma separated prefixes; seconds > 0 stops and writes by itself. */
    public static String start(String prefixes, final int seconds)
    {
        if (!available())
        {
            return "no call logger: start the game with -Ptrace (python3 tools/trace/build.py first)";
        }
        try
        {
            String r = (String) start.invoke(null, prefixes, 200000);
            if (seconds > 0)
            {
                Thread t = new Thread("decitrace timer")
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Thread.sleep(seconds * 1000L);
                        }
                        catch (InterruptedException e)
                        {
                            return;
                        }
                        if (timer == this)
                        {
                            FMLLog.info("[deciworldgen] %s", CallTrace.stop());
                        }
                    }
                };
                t.setDaemon(true);
                timer = t;
                t.start();
                r += ", stops in " + seconds + " s";
            }
            FMLLog.info("[deciworldgen] %s", r);
            return r;
        }
        catch (Exception e)
        {
            return "call logger failed: " + e;
        }
    }

    /** Stops recording and writes trace/trace_<time>.txt. */
    public static String stop()
    {
        if (!available())
        {
            return "no call logger: start the game with -Ptrace";
        }
        timer = null;
        String name = "trace/trace_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".txt";
        try
        {
            return (String) stop.invoke(null, new File(name).getAbsolutePath());
        }
        catch (Exception e)
        {
            return "call logger failed: " + e;
        }
    }
}
