import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * Javassist patch: Decimation's main menu shows its own background scene again (user, 17 Oktober 2026: "replace with
 * real scene", "just use component from decimation gui menu its self"; docs/main_menu.md).
 *
 * RenderUtil.drawMenuBackground (deci.j.a.b(GuiScreen)) draws one of three background pictures (background0..2.png,
 * picked at random each launch) with a slow sway, the mouse parallax and a slow zoom, then the drifting fog, and then
 * covers all of it with an opaque black rectangle (drawRectShadowed, alpha 1) and draws a rotating lava "earth" cube,
 * a white "sun" cube and six sun glow sprites on the black. This removes only those three things (the black layer, the
 * two drawModel calls, the six glow sprites): the picture with its own motion, the fog, the side shadow and the top
 * and bottom gradients stay. The pictures themselves are our real scenes, put into the jar by tools/menu_assets.py.
 * The one picture draw (drawTextureCentered) goes through net.decimation.fixes.MenuBackground.draw with the same
 * arguments: a new picture every 7 seconds (user: "make the background image changed every 7 second"). Needs our
 * compiled classes on the Javassist class path (third argument).
 *
 *   javac -cp tools/lib/javassist.jar --release 8 -d build/patch_menubg tools/patches/PatchMenuBackground.java
 *   java -cp tools/lib/javassist.jar:build/patch_menubg PatchMenuBackground ORIGINAL.jar build/patch_menubg/out dev/build/classes/java/main
 *   (cd build/patch_menubg/out && zip <jar> deci/j/a.class)   # each of the three jars
 */
public class PatchMenuBackground {
    public static void main(String[] a) throws Exception {
        ClassPool pool = new ClassPool(true);
        pool.insertClassPath(a[0]);
        pool.insertClassPath("tools/lib/minecraft-1.7.10-srg.jar");
        pool.insertClassPath("tools/lib/forge-1.7.10-srg.jar");
        pool.insertClassPath(a[2]);                                   // our classes: net.decimation.fixes.MenuBackground

        CtClass render = pool.get("deci.j.a");
        CtMethod background = render.getMethod("b", "(Lnet/minecraft/client/gui/GuiScreen;)V");
        final int[] removed = new int[4];
        background.instrument(new ExprEditor() {
            @Override
            public void edit(MethodCall m) throws CannotCompileException {
                if (!m.getClassName().equals("deci.j.a")) {
                    return;
                }
                String call = m.getMethodName() + m.getSignature();
                if (call.equals("b(DDDDLjava/lang/String;F)V")) {
                    m.replace("{ }");                         // drawRectShadowed: the opaque black layer
                    removed[0]++;
                } else if (call.equals("a(IIILnet/minecraft/client/model/ModelBase;Lnet/minecraft/util/ResourceLocation;F)V")) {
                    m.replace("{ }");                         // drawModel: the sun and earth cubes
                    removed[1]++;
                } else if (call.equals("a(DDLnet/minecraft/util/ResourceLocation;FFFD)V")) {
                    m.replace("{ }");                         // drawTextureCenteredAlpha: the sun glow sprites
                    removed[2]++;
                } else if (call.equals("a(DDLnet/minecraft/util/ResourceLocation;FFF)V")) {
                    // drawTextureCentered: the background picture, now one of the slideshow
                    m.replace("{ net.decimation.fixes.MenuBackground.draw($1, $2, $3, $4, $5, $6); }");
                    removed[3]++;
                }
            }
        });
        if (removed[0] != 1 || removed[1] != 2 || removed[2] != 6 || removed[3] != 1) {
            throw new IllegalStateException("expected 1 black layer, 2 cubes, 6 glow sprites, 1 picture; found "
                                            + removed[0] + ", " + removed[1] + ", " + removed[2] + ", " + removed[3]);
        }
        render.writeFile(a[1]);
        System.out.println("patched deci/j/a.b(GuiScreen): removed the black layer, 2 cubes and 6 sun glow sprites; picture through MenuBackground");
    }
}
