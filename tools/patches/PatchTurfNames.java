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
 *   javac -cp tools/lib/javassist.jar --release 8 -d build/patch_turf tools/patches/PatchTurfNames.java
 *   java -cp tools/lib/javassist.jar:build/patch_turf PatchTurfNames dist/Decimation.jar build/patch_turf/out
 *   (cd build/patch_turf/out && zip <jar> deci/g/j.class)   # for each of the three jars
 */
public class PatchTurfNames {
    public static void main(String[] a) throws Exception {
        ClassPool pool = new ClassPool(true);
        pool.insertClassPath(a[0]);
        pool.insertClassPath("tools/lib/minecraft-1.7.10-srg.jar");
        pool.insertClassPath("tools/lib/forge-1.7.10-srg.jar");
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
}
