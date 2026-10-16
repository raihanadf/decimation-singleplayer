import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * ASM patch: our name, version and links in Decimation's menu (user, 17 Oktober 2026: "Links to our site", "for version
 * replace version code with our version code name, and for decimation replace with deciremnant"; docs/main_menu.md).
 *
 * - GuiMenuBase deci.i.a and GuiMenuHome deci.i.e: the version constant "1.21.10f" (under the logo; in the welcome box)
 *   becomes a call to net.decimation.fixes.MenuTexts.version() (our version);
 * - GuiMenuBase deci.i.a: the title line "Decimation - " becomes "DeciRemnant: " (no dash: user rule);
 * - every menu page with the red text links top right (deci.i.a, i.g, i.i, i.j, i.k, i.l and SupporterOnlyScreen
 *   deci.d.k): "Discord" becomes "Website", "Twitter" becomes "GitHub";
 * - ClientResources deci.b.c: the two addresses behind them (BoehMod's Discord and Twitter) become our website and our
 *   GitHub repository;
 * - WindowSetup deci.m.b: the window title "Minecraft - Decimation v1.21.10f" becomes MenuTexts.windowTitle().
 * Decimation's own mod version (deci.a.b) and the server list's version checks keep "1.21.10f".
 *
 * Reads the classes of the jar it is given and writes the patched ones; give it the ORIGINAL classes with only
 * PatchMenuStats applied (deci.i.a and deci.i.e carry it), never a jar this patch already ran on (its texts are gone
 * there). The welcome box sentence is Decimation's lang file (tools/menu_assets.py). Steps: docs/main_menu.md 4b.
 *
 *   ASM=~/.gradle/caches/modules-2/files-2.1/org.ow2.asm/asm-debug-all/5.0.3/f9e3*.../asm-debug-all-5.0.3.jar
 *   javac -cp $ASM -source 8 -target 8 -d build/patch_menutexts tools/patches/PatchMenuTexts.java
 *   java -cp $ASM:build/patch_menutexts PatchMenuTexts dist/Decimation.jar build/patch_menutexts/out
 *   (cd build/patch_menutexts/out && zip <jar> deci/i/a.class deci/i/e.class deci/i/g.class deci/i/i.class \
 *        deci/i/j.class deci/i/k.class deci/i/l.class deci/d/k.class deci/b/c.class deci/m/b.class)
 */
public class PatchMenuTexts
{
    static final String TEXTS = "net/decimation/fixes/MenuTexts";
    static final String SITE = "https://deciremnant.vercel.app/";
    static final String REPO = "https://github.com/ucilmenangis/decimation-singleplayer";

    public static void main(String[] a) throws Exception
    {
        Map<String, Map<String, Object>> plan = new HashMap<String, Map<String, Object>>();
        // a String value swaps the text, a String[] {method} becomes a call to MenuTexts.method()
        List<String> linkPages = Arrays.asList("deci/i/a", "deci/i/g", "deci/i/i", "deci/i/j", "deci/i/k", "deci/i/l", "deci/d/k");
        for (String page : linkPages)
        {
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("Discord", "Website");
            m.put("Twitter", "GitHub");
            plan.put(page, m);
        }
        plan.get("deci/i/a").put("1.21.10f", new String[] {"version"});
        plan.get("deci/i/a").put("Decimation - ", "DeciRemnant: ");
        Map<String, Object> home = new HashMap<String, Object>();
        home.put("1.21.10f", new String[] {"version"});
        plan.put("deci/i/e", home);
        Map<String, Object> links = new HashMap<String, Object>();
        links.put("https://www.discord.gg/boehmod", SITE);
        links.put("https://www.twitter.com/BoehMod", REPO);
        plan.put("deci/b/c", links);
        Map<String, Object> window = new HashMap<String, Object>();
        window.put("Minecraft - Decimation v1.21.10f", new String[] {"windowTitle"});
        plan.put("deci/m/b", window);

        ZipFile jar = new ZipFile(a[0]);
        int total = 0;
        for (Map.Entry<String, Map<String, Object>> p : plan.entrySet())
        {
            final Map<String, Object> swaps = p.getValue();
            ZipEntry e = jar.getEntry(p.getKey() + ".class");
            ClassReader r = new ClassReader(read(jar.getInputStream(e)));
            ClassWriter w = new ClassWriter(0); // same stack effect (a String pushed), no frame change
            final int[] changed = {0};
            r.accept(new ClassVisitor(Opcodes.ASM5, w)
            {
                @Override
                public MethodVisitor visitMethod(int acc, String name, String desc, String sig, String[] ex)
                {
                    return new MethodVisitor(Opcodes.ASM5, super.visitMethod(acc, name, desc, sig, ex))
                    {
                        @Override
                        public void visitLdcInsn(Object cst)
                        {
                            Object to = cst instanceof String ? swaps.get(cst) : null;
                            if (to instanceof String)
                            {
                                changed[0]++;
                                super.visitLdcInsn(to);
                            }
                            else if (to instanceof String[])
                            {
                                changed[0]++;
                                super.visitMethodInsn(Opcodes.INVOKESTATIC, TEXTS, ((String[]) to)[0], "()Ljava/lang/String;", false);
                            }
                            else
                            {
                                super.visitLdcInsn(cst);
                            }
                        }
                    };
                }
            }, 0);
            if (changed[0] != swaps.size())
            {
                throw new IllegalStateException(p.getKey() + ": " + changed[0] + " texts changed, expected " + swaps.size());
            }
            File out = new File(a[1], p.getKey() + ".class");
            out.getParentFile().mkdirs();
            FileOutputStream o = new FileOutputStream(out);
            o.write(w.toByteArray());
            o.close();
            System.out.println(p.getKey() + ": " + changed[0] + " texts");
            total += changed[0];
        }
        jar.close();
        System.out.println(total + " texts in " + plan.size() + " classes");
    }

    private static byte[] read(InputStream in) throws Exception
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        for (int n; (n = in.read(buf)) > 0; )
        {
            b.write(buf, 0, n);
        }
        in.close();
        return b.toByteArray();
    }
}
