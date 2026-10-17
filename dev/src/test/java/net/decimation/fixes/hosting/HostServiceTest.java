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
    volatile boolean gone;

    @BeforeEach
    void up() throws Exception
    {
        http = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        http.createContext("/", ex -> {
            String p = ex.getRequestURI().getPath();
            ex.getRequestBody().close();
            String a = "{}";
            if (p.equals("/list/host"))
            {
                a = "{\"room\":\"r" + hosts.incrementAndGet() + "\",\"secret\":\"s\"}";
            }
            if (p.equals("/list/beat"))
            {
                beats.incrementAndGet();
                a = gone ? "{\"gone\":true}" : "{\"ok\":true}";
            }
            if (p.equals("/list/stop"))
            {
                stops.incrementAndGet();
                a = "{\"ok\":true}";
            }
            byte[] b = a.getBytes();
            ex.sendResponseHeaders(200, b.length);
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
}

