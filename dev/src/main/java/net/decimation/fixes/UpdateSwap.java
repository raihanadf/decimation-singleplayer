package net.decimation.fixes;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Puts a downloaded update in place after the game has closed (Updater): the running jar cannot be
 * replaced while the game holds it open on Windows. Started by the game as its own small Java process
 * from a copy of this class in a temporary folder (no Minecraft or Forge classes, nothing from the jar
 * it replaces). Retries for up to two minutes until the old jar can be moved aside, then moves the new
 * one into its place and deletes the old one. Writes what it did to update.log beside the jar.
 *
 * usage: java -cp DIR net.decimation.fixes.UpdateSwap TARGET_JAR STAGED_FILE
 */
public final class UpdateSwap
{
    private UpdateSwap()
    {
    }

    public static void main(String[] args) throws Exception
    {
        File target = new File(args[0]), staged = new File(args[1]);
        File old = new File(target.getPath() + ".old");
        PrintWriter log = new PrintWriter(new FileWriter(new File(target.getParentFile(), "update.log"), true));
        log.println(new java.util.Date() + " swap " + staged + " -> " + target);
        boolean done = false;
        for (int i = 0; i < 240 && !done; i++)
        {
            if (!staged.isFile())
            {
                log.println("no staged file");
                break;
            }
            old.delete();
            boolean moved = !target.exists() || target.renameTo(old);
            if (moved && staged.renameTo(target))
            {
                old.delete();
                done = true;
            }
            else
            {
                if (moved && !target.exists())
                {
                    old.renameTo(target);                 // put the old one back before trying again
                }
                Thread.sleep(500);
            }
        }
        log.println(done ? "done" : "FAILED: the old jar stayed (try again after closing the game)");
        log.close();
    }
}
