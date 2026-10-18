package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class JoinServiceTest
{
    static int echo;

    @BeforeAll
    static void up() throws Exception
    {
        echo = EchoServer.start();
        System.setProperty("deciworldgen.relay", "http://127.0.0.1:" + echo);
    }

    @AfterAll
    static void down()
    {
        EchoServer.stop();
    }

    @AfterEach
    void close()
    {
        JoinService.INSTANCE.closeAll();
    }

    static ListEntry entry(String... mods)
    {
        ListEntry e = new ListEntry();
        e.room = "echo";
        e.name = "Ucil world";
        e.version = "0.64.9";
        e.max = 4;
        for (int i = 0; i < mods.length; i += 2)
        {
            e.mods.add(Arrays.asList(mods[i], mods[i + 1]));
        }
        return e;
    }

    @Test
    void missingModIsRefusedWithItsName() throws Exception
    {
        final List<String> toasts = Collections.synchronizedList(new ArrayList<String>());
        JoinService.INSTANCE.setToasts(toasts::add);
        int port = JoinService.INSTANCE.listen(entry("ruins", "16.4"), ModSet.of(Collections.<String[]>emptyList()));
        Socket s = new Socket(InetAddress.getLoopbackAddress(), port);
        s.setSoTimeout(2000);
        assertEquals(-1, s.getInputStream().read());
        Thread.sleep(100);
        assertEquals(Collections.singletonList("You need ruins 16.4 to join Ucil world"), toasts);
    }

    @Test
    void aJoinIsCarriedToTheRoomAndTold() throws Exception
    {
        final List<Integer> told = Collections.synchronizedList(new ArrayList<Integer>());
        JoinService.INSTANCE.setJoinHook((name, p) -> told.add(p));
        int port = JoinService.INSTANCE.listen(entry(), ModSet.of(Collections.<String[]>emptyList()));
        assertEquals(port, JoinService.INSTANCE.portOf("Ucil world"));
        Socket s = new Socket(InetAddress.getLoopbackAddress(), port);
        byte[] login = LoginNameTest.join(LoginNameTest.handshake(2), LoginNameTest.loginStart("Rizky"));
        s.getOutputStream().write(login);
        s.setSoTimeout(3000);
        byte[] back = new byte[login.length];
        new DataInputStream(s.getInputStream()).readFully(back); // the echo relay sends it back
        s.close();
        Thread.sleep(100);
        assertEquals(Collections.singletonList(port), told);
    }

    @Test
    void aPingIsNoJoin() throws Exception
    {
        final List<Integer> told = Collections.synchronizedList(new ArrayList<Integer>());
        JoinService.INSTANCE.setJoinHook((name, p) -> told.add(p));
        int port = JoinService.INSTANCE.listen(entry(), ModSet.of(Collections.<String[]>emptyList()));
        Socket s = new Socket(InetAddress.getLoopbackAddress(), port);
        byte[] ping = LoginNameTest.handshake(1);
        s.getOutputStream().write(ping);
        s.setSoTimeout(3000);
        new DataInputStream(s.getInputStream()).readFully(new byte[ping.length]);
        s.close();
        Thread.sleep(100);
        assertTrue(told.isEmpty(), "a Community row's ping must not arm the join retry");
    }

    @Test
    void closeFreesThePorts() throws Exception
    {
        JoinService.INSTANCE.listen(entry(), ModSet.of(Collections.<String[]>emptyList()));
        assertTrue(JoinService.INSTANCE.openPorts() > 0);
        JoinService.INSTANCE.closeAll();
        assertEquals(0, JoinService.INSTANCE.openPorts());
        assertEquals(-1, JoinService.INSTANCE.portOf("Ucil world"));
    }

    @Test
    void theDailyLimitIsToldToTheJoiner() throws Exception
    {
        com.sun.net.httpserver.HttpServer quota = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        quota.createContext("/", ex -> {
            byte[] b = "error code: 1027".getBytes();
            ex.sendResponseHeaders(429, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        quota.start();
        System.setProperty("deciworldgen.relay", "http://127.0.0.1:" + quota.getAddress().getPort());
        try
        {
            final List<String> toasts = Collections.synchronizedList(new ArrayList<String>());
            JoinService.INSTANCE.setToasts(toasts::add);
            int port = JoinService.INSTANCE.listen(entry(), ModSet.of(Collections.<String[]>emptyList()));
            Socket s = new Socket(InetAddress.getLoopbackAddress(), port);
            s.setSoTimeout(5000);
            assertEquals(-1, s.getInputStream().read());
            Thread.sleep(200);
            assertEquals(Collections.singletonList("The free relay is full until tomorrow 07:00 WIB"), toasts);
        }
        finally
        {
            System.setProperty("deciworldgen.relay", "http://127.0.0.1:" + echo);
            quota.stop(0);
        }
    }
}

