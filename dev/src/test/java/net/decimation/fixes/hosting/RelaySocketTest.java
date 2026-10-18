package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RelaySocketTest
{
    static int port;

    @BeforeAll
    static void up() throws Exception
    {
        port = EchoServer.start();
    }

    @AfterAll
    static void down()
    {
        EchoServer.stop();
    }

    private static RelaySocket.Listener into(final BlockingQueue<Object> in)
    {
        return new RelaySocket.Listener()
        {
            public void binary(byte[] d) { in.add(d); }
            public void text(String t) { in.add(t); }
            public void closed(int c, String r) { in.add("closed " + c); }
        };
    }

    @Test
    void textAndBinaryRoundTrip() throws Exception
    {
        BlockingQueue<Object> in = new LinkedBlockingQueue<Object>();
        RelaySocket s = RelaySocket.open(new URI("ws://127.0.0.1:" + port + "/echo"), into(in));
        s.sendText("hi");
        assertEquals("echo hi", in.poll(3, TimeUnit.SECONDS));
        s.send(new byte[] {1, 2, 3}, 0, 3);
        assertArrayEquals(new byte[] {1, 2, 3}, (byte[]) in.poll(3, TimeUnit.SECONDS));
        s.close("done");
    }

    @Test
    void smallWritesWithinOneTickBecomeOneFrame() throws Exception
    {
        RelaySocket s = RelaySocket.open(new URI("ws://127.0.0.1:" + port + "/echo"), into(new LinkedBlockingQueue<Object>()));
        Thread.sleep(100);
        EchoServer.frameSizes.clear();
        for (int i = 0; i < 20; i++)
        {
            s.send(new byte[] {(byte) i}, 0, 1);
        }
        Thread.sleep(300);
        // 20 one byte writes: the first goes at once (quiet before), the rest share at most 2 tick frames; never 20
        int sum = 0;
        for (int n : EchoServer.frameSizes)
        {
            sum += n;
        }
        assertEquals(20, sum);
        assertTrue(EchoServer.frameSizes.size() <= 3, EchoServer.frameSizes.toString());
        s.close("done");
    }

    @Test
    void refusedConnectionThrows()
    {
        assertThrows(java.io.IOException.class, () -> RelaySocket.open(new URI("ws://127.0.0.1:1/echo"), null));
    }

    @Test
    void closeStopsTheTickFlush() throws Exception
    {
        RelaySocket s = RelaySocket.open(new URI("ws://127.0.0.1:" + port + "/echo"), into(new LinkedBlockingQueue<Object>()));
        s.close("done");
        Thread.sleep(200);
        assertTrue(s.flushStopped(), "the 50 ms flush task is cancelled when the socket closes");
    }

    @Test
    void anIdleSocketSendsPingsSoCloudflareNeverClosesIt() throws Exception
    {
        int port = EchoServer.start();
        RelaySocket.pingMsForTests(150);
        try
        {
            int before = EchoServer.pings.get();
            RelaySocket s = RelaySocket.open(java.net.URI.create("ws://127.0.0.1:" + port + "/idle"), null);
            Thread.sleep(800);
            assertTrue(EchoServer.pings.get() - before >= 3, "pings while idle: " + (EchoServer.pings.get() - before));
            assertTrue(s.isOpen(), "a pong is not mistaken for data or a close");
            s.close("done");
        }
        finally
        {
            RelaySocket.pingMsForTests(0);
        }
    }

    @Test
    void aLoneWriteAfterAQuietMomentGoesAtOnce() throws Exception
    {
        BlockingQueue<Object> in = new LinkedBlockingQueue<Object>();
        RelaySocket s = RelaySocket.open(new URI("ws://127.0.0.1:" + port + "/echo"), into(in));
        int slow = 0;
        StringBuilder times = new StringBuilder();
        for (int i = 0; i < 5; i++)
        {
            Thread.sleep(120); // quiet: more than one tick since the last frame
            long t0 = System.nanoTime();
            s.send(new byte[] {(byte) i}, 0, 1);
            assertNotNull(in.poll(3, TimeUnit.SECONDS));
            long ms = (System.nanoTime() - t0) / 1_000_000;
            times.append(ms).append(' ');
            slow += ms >= 20 ? 1 : 0;
        }
        // the old tick wait put 3 of 5 over 20 ms on average; one slow round trip on a busy machine is allowed
        assertTrue(slow <= 1, "a click or a shot should not wait for the 50 ms tick: " + times + "ms");
        s.close("done");
    }
}

