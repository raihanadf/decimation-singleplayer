import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Javassist patch: Decimation never asks for an update (bug.md "New players are forced to update",
 * user 13 Oktober 2026: "remove the update button in there so new player will be play directly").
 *
 * UpdateChecker.isUpdateRequired (net.decimation.mod.common.utils.h.gI) answered true when
 * mods/DecimationVoiceChat.jar is missing or when BoehMod's latestversion.txt on GitHub names another
 * version than 1.21.10f. The menu (GuiMenuBase and the profile / supporter pages) then shows "Update"
 * instead of "Play", and the update leads to the dead BoehMod server list with no world buttons. The
 * servers are gone, so there is nothing to update to: the check now always answers false.
 *
 * v2 (v0.59.0, auto update, docs/ideas.md item 20): gI answers net.decimation.fixes.Updater.required(),
 * true only once OUR GitHub releases have a newer version; the "Update" button then opens our update
 * screen (Updater; since v0.64.7 UpdateProgress swaps Decimation's update page for its progress screen). Needs our compiled classes on the
 * Javassist class path (third argument); patch from the ORIGINAL class (Decimation.jar.original.bak
 * copied to a name ending in .jar).
 *
 *   javac -cp tools/lib/javassist.jar --release 8 -d build/patch_noupdate tools/patches/PatchNoUpdate.java
 *   java -cp tools/lib/javassist.jar:build/patch_noupdate PatchNoUpdate ORIGINAL.jar build/patch_noupdate/out dev/build/classes/java/main
 *   (cd build/patch_noupdate/out && zip <jar> net/decimation/mod/common/utils/h.class)   # each of the three jars
 */
public class PatchNoUpdate {
    public static void main(String[] a) throws Exception {
        ClassPool pool = new ClassPool(true);
        pool.insertClassPath(a[0]);
        pool.insertClassPath("tools/lib/minecraft-1.7.10-srg.jar");
        pool.insertClassPath("tools/lib/forge-1.7.10-srg.jar");
        pool.insertClassPath(a[2]);                                   // our classes: net.decimation.fixes.Updater

        CtClass checker = pool.get("net.decimation.mod.common.utils.h");
        CtMethod required = checker.getMethod("gI", "()Z");
        required.setBody("{ return net.decimation.fixes.Updater.required(); }");
        checker.writeFile(a[1]);
        System.out.println("patched net/decimation/mod/common/utils/h.gI: update required = Updater.required()");
    }
}
