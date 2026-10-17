package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HostServiceTest
{
    HttpServer http;
    final AtomicInteger hosts = new AtomicInteger(), beats = new AtomicInteger(), stops = new AtomicInteger();
    volatile boolean gone, slowHost, beat503, host429;

    @BeforeEach
    void up() throws Exception
    {
        http = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        http.createContext("/", ex -> {
            String p = ex.getRequestURI().getPath();
            ex.getRequestBody().close();
            String a = "{}";
            int code = 200;
            if (p.equals("/list/host"))
            {
                if (slowHost)
                {
                    try { Thread.sleep(1500); } catch (InterruptedException e) { }
                }
                a = "{\"room\":\"r" + hosts.incrementAndGet() + "\",\"secret\":\"s\"}";
                if (host429)
                {
                    code = 429;
                    a = "error code: 1027";
                }
            }
            if (p.equals("/list/beat"))
            {
                beats.incrementAndGet();
                a = gone ? "{\"gone\":true}" : "{\"ok\":true}";
                if (beat503)
                {
                    code = 503;
                    a = "busy";
                }
            }
            if (p.equals("/list/stop"))
            {
                stops.incrementAndGet();
                a = "{\"ok\":true}";
            }
            byte[] b = a.getBytes();
            ex.sendResponseHeaders(code, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        http.start();
        System.setProperty("deciworldgen.relay", "http://127.0.0.1:" + http.getAddress().getPort());
        HostService.INSTANCE.socketsForTests(false); // the fake relay has no WebSocket
    }

    @AfterEach
    void down()
    {
        HostService.INSTANCE.stop();
        http.stop(0);
    }

    static HostService.Settings settings()
    {
        HostService.Settings s = new HostService.Settings();
        s.name = "Ucil world";
        s.online = false;
        s.max = 4;
        s.player = "Ucil";
        s.version = "0.64.9";
        return s;
    }

    @Test
    void startRegistersAndBeatsEveryMinute()
    {
        long t0 = System.currentTimeMillis();
        HostService.INSTANCE.start(settings(), 25565, () -> 1);
        assertEquals(HostService.State.ONLINE, HostService.INSTANCE.state());
        assertEquals(1, hosts.get());
        HostService.INSTANCE.tick(t0 + 61_000);
        assertEquals(1, beats.get());
        HostService.INSTANCE.tick(t0 + 62_000);
        assertEquals(1, beats.get(), "only one beat a minute");
    }

    @Test
    void goneReRegisters()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 0);
        gone = true;
        HostService.INSTANCE.tick(System.currentTimeMillis() + 61_000);
        assertEquals(2, hosts.get());
        assertEquals(HostService.State.ONLINE, HostService.INSTANCE.state());
    }

    @Test
    void stopRemovesTheEntryAtOnce()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 0);
        HostService.INSTANCE.stop();
        assertEquals(1, stops.get());
        assertEquals(HostService.State.OFF, HostService.INSTANCE.state());
    }

    @Test
    void worldUnloadStopsHosting()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 0);
        HostService.INSTANCE.worldClosed();
        assertEquals(1, stops.get());
        assertEquals(HostService.State.OFF, HostService.INSTANCE.state());
    }

    @Test
    void aNameAlreadyPlayingIsTakenInAnyCase()
    {
        java.util.List<String> playing = java.util.Arrays.asList("Ucil", "Rizky");
        assertTrue(HostService.nameTaken("ucil", playing));
        assertTrue(HostService.nameTaken("RIZKY", playing));
        assertFalse(HostService.nameTaken("Dimas", playing));
    }

    @Test
    void theGateRefusesATakenNameInAnOfflineWorld()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 1);
        HostService.INSTANCE.playingForTests(() -> java.util.Arrays.asList("Ucil"));
        byte[] impostor = LoginNameTest.join(LoginNameTest.handshake(2), LoginNameTest.loginStart("ucil"));
        byte[] friend = LoginNameTest.join(LoginNameTest.handshake(2), LoginNameTest.loginStart("Rizky"));
        assertEquals(Pipe.Gate.DENY, HostService.INSTANCE.admit(impostor));
        assertEquals(Pipe.Gate.ALLOW, HostService.INSTANCE.admit(friend));
        assertEquals(Pipe.Gate.ALLOW, HostService.INSTANCE.admit(LoginNameTest.handshake(1)));
        assertEquals(Pipe.Gate.MORE, HostService.INSTANCE.admit(LoginNameTest.handshake(2)));
    }

    @Test
    void twoLoginsWithOneNewNameAtOnceLetOnlyTheFirstIn()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 1);
        HostService.INSTANCE.playingForTests(() -> java.util.Arrays.asList("Ucil"));
        byte[] a = LoginNameTest.join(LoginNameTest.handshake(2), LoginNameTest.loginStart("Rizky"));
        assertEquals(Pipe.Gate.ALLOW, HostService.INSTANCE.admit(a));
        assertEquals(Pipe.Gate.DENY, HostService.INSTANCE.admit(a), "the first Rizky is still logging in");
    }

    @Test
    void unreadableFirstBytesAreRefused()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 1);
        assertEquals(Pipe.Gate.DENY, HostService.INSTANCE.admit(new byte[] {(byte) 0xFE, 1, (byte) 0xFA}));
    }

    @Test
    void theClientTickNeverWaitsOnTheRelay() throws Exception
    {
        slowHost = true;
        HostService.INSTANCE.asyncForTests();
        HostService.INSTANCE.start(settings(), 25565, () -> 0);
        Thread.sleep(100); // the registration is now inside its slow HTTP call
        long t0 = System.nanoTime();
        for (int i = 0; i < 20; i++)
        {
            HostService.INSTANCE.tick(System.currentTimeMillis());
        }
        HostService.INSTANCE.state();
        long ms = (System.nanoTime() - t0) / 1_000_000;
        assertTrue(ms < 100, "20 ticks took " + ms + " ms while the relay was slow");
        for (int i = 0; i < 40 && HostService.INSTANCE.state() != HostService.State.ONLINE; i++) Thread.sleep(100);
        assertEquals(HostService.State.ONLINE, HostService.INSTANCE.state());
    }

    @Test
    void aPassing5xxOnABeatKeepsHosting()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 0);
        beat503 = true;
        HostService.INSTANCE.tick(System.currentTimeMillis() + 61_000);
        assertEquals(HostService.State.ONLINE, HostService.INSTANCE.state());
    }

    @Test
    void theDailyLimitIsToldOnce()
    {
        java.util.List<HostService.State> told = new java.util.ArrayList<HostService.State>();
        HostService.INSTANCE.setStateListener((s, error) -> told.add(s));
        host429 = true;
        HostService.INSTANCE.start(settings(), 25565, () -> 0);
        HostService.INSTANCE.setStateListener(null);
        assertEquals(java.util.Collections.singletonList(HostService.State.FULL_TODAY), told);
    }

    @Test
    void aReservedNameIsFreedWhenItsPipeCloses()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 1);
        HostService.INSTANCE.playingForTests(() -> java.util.Arrays.asList("Ucil"));
        byte[] a = LoginNameTest.join(LoginNameTest.handshake(2), LoginNameTest.loginStart("Rizky"));
        assertEquals(Pipe.Gate.ALLOW, HostService.INSTANCE.admit(a));
        HostService.INSTANCE.release("Rizky");
        assertEquals(Pipe.Gate.ALLOW, HostService.INSTANCE.admit(a), "the retry after a dropped join gets in");
    }

    @Test
    void aLostControlSocketRegistersTheWorldAgain()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 0);
        HostService.INSTANCE.controlLost();
        assertEquals(2, hosts.get());
        assertEquals(HostService.State.ONLINE, HostService.INSTANCE.state());
    }

    @Test
    void aSecondStartStopsTheFirstRoom()
    {
        HostService.INSTANCE.start(settings(), 25565, () -> 0);
        HostService.INSTANCE.start(settings(), 25565, () -> 0);
        assertEquals(2, hosts.get());
        assertEquals(1, stops.get());
    }

    @Test
    void noHostingMeansNoLogin()
    {
        HostService.INSTANCE.stop();
        HostService.INSTANCE.clearForTests();
        byte[] a = LoginNameTest.join(LoginNameTest.handshake(2), LoginNameTest.loginStart("Rizky"));
        assertEquals(Pipe.Gate.DENY, HostService.INSTANCE.admit(a), "the gate fails closed when nothing is hosted");
    }
}

