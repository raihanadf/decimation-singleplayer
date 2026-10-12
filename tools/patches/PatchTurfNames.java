import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * Javassist patch: turf names with spaces in the chat line (docs/zones.md "Place names").
 *
 * IngameHud (deci.g.j) render b(IIF)V strips every space from the turf list entries and shows the
 * name twice: the "ENTERING LOCATION" banner (it turns "_" into spaces itself) and the chat line
 * "Now entering NAME controlled by OWNER" (it does not). Our place names (PlaceNames, "Ashford:
 * Downtown - 7") travel with "_" for spaces; this patch makes the chat line turn "_" into spaces too,
 * at the three addChatComponentMessage (func_146105_b) calls of that method (turf, safezone,
 * wilderness lines). deci.g.j is ALREADY patched by PatchMenuStats: take it from dist/Decimation.jar.
 *
 * Step 2 (v0.52.1, argument "layer"): ClanTurfLayer (deci.M.l) render c(RenderWorldLastEvent) draws
 * the name of the turf the player stands in as floating text over the turf's middle (and, in
 * creative with the selection tool, "Turf name: NAME"); every RenderUtil (deci.j.a) text call there
 * gets its String argument with "_" turned into spaces. deci.M.l was unpatched before this step.
 *
 *   javac -cp tools/lib/javassist.jar --release 8 -d build/patch_turf tools/patches/PatchTurfNames.java
 *   java -cp tools/lib/javassist.jar:build/patch_turf PatchTurfNames dist/Decimation.jar build/patch_turf/out [hud|layer]
 *   (cd build/patch_turf/out && zip <jar> deci/g/j.class)   # hud (default), for each of the three jars
 *   (cd build/patch_turf/out && zip <jar> deci/M/l.class)   # layer
 */
public class PatchTurfNames {
    public static void main(String[] a) throws Exception {
        ClassPool pool = new ClassPool(true);
        pool.insertClassPath(a[0]);
        pool.insertClassPath("tools/lib/minecraft-1.7.10-srg.jar");
        pool.insertClassPath("tools/lib/forge-1.7.10-srg.jar");
        if (a.length > 2 && a[2].equals("layer")) {
            layer(pool, a[1]);
            return;
        }
        CtClass hud = pool.get("deci.g.j");
        CtMethod render = hud.getMethod("b", "(IIF)V");
        final int[] done = new int[1];
        render.instrument(new ExprEditor() {
            public void edit(MethodCall m) throws javassist.CannotCompileException {
                if (m.getMethodName().equals("func_146105_b")) {
                    m.replace("{ $proceed(new net.minecraft.util.ChatComponentText("
                              + "((net.minecraft.util.IChatComponent) $1).func_150260_c().replace('_', ' '))); }");
                    done[0]++;
                }
            }
        });
        if (done[0] != 3) {
            throw new IllegalStateException("expected 3 chat lines in IngameHud.render, got " + done[0]);
        }
        hud.writeFile(a[1]);
        System.out.println("patched deci/g/j.b(IIF)V: " + done[0] + " chat lines show _ as spaces");
    }

    /** ClanTurfLayer: floating turf names with spaces. */
    static void layer(ClassPool pool, String out) throws Exception {
        CtClass layer = pool.get("deci.M.l");
        CtMethod render = layer.getMethod("c", "(Lnet/minecraftforge/client/event/RenderWorldLastEvent;)V");
        final int[] done = new int[1];
        render.instrument(new ExprEditor() {
            public void edit(MethodCall m) throws javassist.CannotCompileException {
                if (m.getClassName().equals("deci.j.a") && m.getSignature().startsWith("(Ljava/lang/String;")
                    && m.getSignature().endsWith(")V")) {     // not w(String) = colour from "#FFFFFF"
                    m.replace("{ $1 = $1.replace('_', ' '); $_ = $proceed($$); }");
                    done[0]++;
                }
            }
        });
        if (done[0] != 7) {
            throw new IllegalStateException("expected 7 text calls in ClanTurfLayer.render, got " + done[0]);
        }
        layer.writeFile(out);
        System.out.println("patched deci/M/l.c: " + done[0] + " turf texts show _ as spaces");
    }
}
