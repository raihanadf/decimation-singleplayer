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
}
