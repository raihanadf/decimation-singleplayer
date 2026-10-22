package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Relay sockets that landed far from the room are tried again (docs/player_hosting.md 16). */
class ColosTest
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
    void southeastAsianRoomsWantAnAsianDataCenter()
    {
        assertTrue(Colos.near("apac-se", "SIN"));
        assertTrue(Colos.near("apac-se", "CGK"));
        assertFalse(Colos.near("apac-se", "FRA"));
        assertFalse(Colos.near("apac-se", "CDG"));
        assertFalse(Colos.near("apac-se", "HKG")); // measured: a Hong Kong socket for a Singapore room added about 65 ms
        assertTrue(Colos.near("apac-ne", "NRT"));
        assertFalse(Colos.near("apac-ne", "LHR"));
    }

    @Test
    void unknownRegionOrDataCenterNeverRetries()
    {
        assertTrue(Colos.near(null, "FRA"));
        assertTrue(Colos.near("weur", "FRA"));
        assertTrue(Colos.near("apac-se", null));
    }

    @Test
    void aSocketThatLandedFarIsOpenedAgainUntilItLandsNear() throws Exception
    {
        RelaySocket s = RelaySocket.openNear(new URI("ws://127.0.0.1:" + port + "/hop/FRA,LHR,SIN"), null, "apac-se", 3);
        assertEquals("SIN", s.colo());
        assertTrue(s.isOpen());
        s.close("done");
    }

    @Test
    void afterThreeFarTriesTheLastOneIsKept() throws Exception
    {
        RelaySocket s = RelaySocket.openNear(new URI("ws://127.0.0.1:" + port + "/hop/FRA,LHR,CDG,SIN"), null, "apac-se", 3);
        assertEquals("CDG", s.colo());
        assertTrue(s.isOpen());
        s.close("done");
    }

    @Test
    void withOverlapTheNextSocketOpensBeforeTheFarOneCloses() throws Exception
    {
        for (int i = 0; i < 200 && EchoServer.openHops.get() > 0; i++)
        {
            Thread.sleep(10);       // an earlier test's socket closes on the server's thread: its "close" lands late
        }
        EchoServer.hopEvents.clear();
        RelaySocket s = RelaySocket.openNear(new URI("ws://127.0.0.1:" + port + "/hop/FRA,SIN"), null, "apac-se", 3, true);
        assertEquals("SIN", s.colo());
        Thread.sleep(200);
        // the relay pairs a host data socket with its joiner: closing the far one first would take the joiner down
        assertEquals(java.util.Arrays.asList("open 0", "open 1", "close 0"), EchoServer.hopEvents);
        s.close("done");
    }

    @Test
    void whatTheFarSocketHeardIsHandedToTheKeptOne() throws Exception
    {
        java.util.concurrent.BlockingQueue<String> heard = new java.util.concurrent.LinkedBlockingQueue<String>();
        RelaySocket.Listener l = new RelaySocket.Listener()
        {
            public void binary(byte[] d)
            {
            }

            public void text(String s)
            {
                heard.add(s);
            }

            public void closed(int code, String reason)
            {
            }
        };
        // the relay already sent "ready" or a joiner's first bytes to the far socket before the host chose the near one
        RelaySocket s = RelaySocket.openNear(new URI("ws://127.0.0.1:" + port + "/hop/FRA,SIN/greet"), l, "apac-se", 3, true);
        assertEquals("first", heard.poll(2, java.util.concurrent.TimeUnit.SECONDS));
        s.close("done");
    }
}

