package net.decimation.fixes.hosting;

/** Manual check (plan Task 4 step 5): a wss echo over TLS with the running Java. Not a unit test. */
public final class TlsCheck
{
    public static void main(String[] a) throws Exception
    {
        final java.util.concurrent.CountDownLatch got = new java.util.concurrent.CountDownLatch(1);
        RelaySocket s = RelaySocket.open(new java.net.URI(a[0]), new RelaySocket.Listener()
        {
            public void binary(byte[] d) { System.out.println("binary " + d.length); got.countDown(); }
            public void text(String t) { System.out.println("text " + t); got.countDown(); }
            public void closed(int c, String r) { System.out.println("closed " + c + " " + r); }
        });
        s.sendText("ping from Java " + System.getProperty("java.version"));
        System.out.println(got.await(10, java.util.concurrent.TimeUnit.SECONDS) ? "TLS OK" : "NO ANSWER");
        s.close("done");
        Thread.sleep(300);
        System.exit(0);
    }
}
