import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

/**
 * Javassist patch: Decimation never contacts BoehMod's dead network (v2, 16 Oktober 2026).
 *
 * v1 (2026-10-09): the game froze about 5 s at launch: DecimationMod.postInit calls BackendClient.e(host, tcp, udp)
 * (deci.aP.a.e), which connected kryonet's Client to network.mcdecimation.net with a 5000 ms timeout on the main
 * thread. v1 made that 300 ms and made kryonet's Client.connect (bundled in Decimation.jar) honour it.
 *
 * v2 (bug.md "Backend reconnect leaks a thread every 15 seconds", docs/decimation_code/backend.md): every 15 s the
 * client tick (ClientEventHandler deci.c.c, the lambda body a(deci.aP.a)) and on a dedicated server the tick thread
 * (ServerTickHandler$1 deci.aK.q$1) did `new Thread(client).start(); client.reconnect()`: each try left one kryonet
 * Client.run thread behind for good (28 after 7 minutes). And network.mcdecimation.net now resolves to an unknown
 * host. So:
 * - BackendClient.e (connect): no message registration, no thread, no connect; one log line;
 * - ClientEventHandler.a(deci.aP.a) (client reconnect) and ServerTickHandler$1.run (server reconnect): nothing.
 * Everything that only sends when connected (stats, articles, player counts, supporter checks) stays silent, as it
 * already was with the dead server. The v1 kryonet change is kept so a rebuild from the original gives every class.
 *
 *   javac -cp tools/lib/javassist.jar --release 8 -d build/patch_backend tools/patches/PatchBackend.java
 *   java -cp tools/lib/javassist.jar:build/patch_backend PatchBackend <ORIGINAL Decimation jar ending .jar> build/patch_backend/out
 *   (cd build/patch_backend/out && zip <jar> deci/aP/a.class 'deci/c/c.class' 'deci/aK/q$1.class' com/esotericsoftware/kryonet/Client.class)
 * into the three jars (Decimation.jar.patched, dist/Decimation.jar, dev/libs/Decimation-base.jar). deci/c/c and
 * deci/aK/q$1 were unpatched in all three (checked 16 Oktober 2026); deci/aP/a is rebuilt from the original.
 */
public class PatchBackend {
    public static void main(String[] a) throws Exception {
        ClassPool pool = new ClassPool(true);
        pool.insertClassPath(a[0]);

        // BackendClient.connect: nothing at all
        CtClass client = pool.get("deci.aP.a");
        CtMethod connect = client.getMethod("e", "(Ljava/lang/String;II)V");
        connect.setBody("{ System.out.println(\"[deciworldgen] Decimation's BoehMod network is off (PatchBackend v2): no connection to \" + $1); }");
        client.writeFile(a[1]);

        // ClientEventHandler: the client reconnect every 15 s (lambda body, static, takes the BackendClient)
        CtClass handler = pool.get("deci.c.c");
        CtMethod reconnect = handler.getDeclaredMethod("a", new CtClass[] {pool.get("deci.aP.a")});
        if (!reconnect.getReturnType().equals(CtClass.voidType) || !Modifier.isStatic(reconnect.getModifiers())) {
            throw new IllegalStateException("deci.c.c.a(deci.aP.a) is not the static reconnect lambda");
        }
        reconnect.setBody("{ return; }");
        handler.writeFile(a[1]);

        // ServerTickHandler$1: the dedicated server reconnect thread
        CtClass serverThread = pool.get("deci.aK.q$1");
        serverThread.getDeclaredMethod("run").setBody("{ return; }");
        serverThread.writeFile(a[1]);

        // v1, kept: kryonet's TCP connect honours the given timeout (bundled in Decimation.jar)
        CtClass kryo = pool.get("com.esotericsoftware.kryonet.Client");
        kryo.addField(CtField.make("public static int tcpTimeout;", kryo), CtField.Initializer.constant(5000));
        CtMethod kconnect = kryo.getMethod("connect", "(ILjava/net/InetAddress;II)V");
        kconnect.insertBefore("{ tcpTimeout = $1; }");
        final int[] tcp = {0};
        kconnect.instrument(new ExprEditor() {
            public void edit(MethodCall m) throws CannotCompileException {
                if (m.getClassName().equals("com.esotericsoftware.kryonet.TcpConnection") && m.getMethodName().equals("connect")) {
                    m.replace("{ $proceed($1, $2, com.esotericsoftware.kryonet.Client.tcpTimeout); }");
                    tcp[0]++;
                }
            }
        });
        if (tcp[0] != 1) {
            throw new IllegalStateException("expected one TcpConnection.connect call, found " + tcp[0]);
        }
        kryo.writeFile(a[1]);
        System.out.println("patched deci.aP.a.e (no BoehMod connect), deci.c.c.a(deci.aP.a) and deci.aK.q$1.run (no "
                           + "reconnect), kryonet Client.connect (v1 timeout)");
    }
}
