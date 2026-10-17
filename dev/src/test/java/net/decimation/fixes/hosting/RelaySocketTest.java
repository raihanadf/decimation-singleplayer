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
        // 20 one byte writes: at most 2 frames (the writes may straddle one 50 ms flush), never 20
        int sum = 0;
        for (int n : EchoServer.frameSizes)
        {
            sum += n;
        }
        assertEquals(20, sum);
        assertTrue(EchoServer.frameSizes.size() <= 2, EchoServer.frameSizes.toString());
        s.close("done");
    }

    @Test
    void refusedConnectionThrows()
    {
        assertThrows(java.io.IOException.class, () -> RelaySocket.open(new URI("ws://127.0.0.1:1/echo"), null));
    }
}
