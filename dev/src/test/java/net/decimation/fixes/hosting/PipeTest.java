package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PipeTest
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

    @Test
    void bytesGoThroughTheRelayAndBack() throws Exception
    {
        ServerSocket local = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        Socket client = new Socket(InetAddress.getLoopbackAddress(), local.getLocalPort());
        Socket accepted = local.accept();
        final AtomicBoolean closed = new AtomicBoolean();
        Pipe.start(accepted, URI.create("ws://127.0.0.1:" + port + "/echo"), false, () -> closed.set(true));
        client.getOutputStream().write(new byte[] {7, 8, 9});
        client.setSoTimeout(3000);
        byte[] back = new byte[3];
        new DataInputStream(client.getInputStream()).readFully(back);
        assertArrayEquals(new byte[] {7, 8, 9}, back);
        client.close();
        Thread.sleep(300);
        assertTrue(closed.get());
        local.close();
    }

    @Test
    void waitReadyHoldsTheBytesUntilTheRelaySaysReady() throws Exception
    {
        ServerSocket local = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
        Socket client = new Socket(InetAddress.getLoopbackAddress(), local.getLocalPort());
        Socket accepted = local.accept();
        Pipe.start(accepted, URI.create("ws://127.0.0.1:" + port + "/echo"), true, null);
        Thread.sleep(100);
        EchoServer.frameSizes.clear();
        client.getOutputStream().write(new byte[] {1});
        Thread.sleep(300);
        assertTrue(EchoServer.frameSizes.isEmpty(), "nothing sent before ready");
        client.close();
        local.close();
    }
}
