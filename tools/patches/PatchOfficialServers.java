import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

/**
 * Javassist patch: the official server list shows our rows (user, 17 Oktober 2026: "for server that is down, replace it
 * with Bukovka and Chernarus only and its say coming soon"; docs/main_menu.md).
 *
 * ServerListPinger.officialServers (deci.k.b.bg) built BoehMod's four dead servers (Bukovka and NewYork City, EU and US,
 * on *.mcdecimation.net). Its body becomes a call to net.decimation.fixes.MenuServers.official(), so the rows live in our
 * code: Bukovka and Chernarus on never resolving hosts, drawn "Coming soon" through PatchMenuTexts (ListElement). The
 * class is identical in the original and the shipped jars (checked 17 Oktober 2026). Needs our compiled classes on the
 * Javassist class path (third argument).
 *
 *   javac -cp tools/lib/javassist.jar --release 8 -d build/patch_servers tools/patches/PatchOfficialServers.java
 *   java -cp tools/lib/javassist.jar:build/patch_servers PatchOfficialServers ORIGINAL.jar build/patch_servers/out dev/build/classes/java/main
 *   (cd build/patch_servers/out && zip <jar> deci/k/b.class)   # each of the three jars
 */
public class PatchOfficialServers {
    public static void main(String[] a) throws Exception {
        ClassPool pool = new ClassPool(true);
        pool.insertClassPath(a[0]);
        pool.insertClassPath(a[2]);                                   // our classes: net.decimation.fixes.MenuServers

        CtClass pinger = pool.get("deci.k.b");
        CtMethod official = pinger.getMethod("bg", "()Ljava/util/List;");
        official.setBody("{ return net.decimation.fixes.MenuServers.official(); }");
        pinger.writeFile(a[1]);
        System.out.println("patched deci/k/b.bg(): the official server list comes from MenuServers.official()");
    }
}
