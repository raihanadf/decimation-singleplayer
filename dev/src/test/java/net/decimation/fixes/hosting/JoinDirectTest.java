package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** The joiner's side of direct joins (docs/player_hosting.md section 15): direct when the host answers, else the relay. */
class JoinDirectTest
{
    static
    {
        Direct.loopbackForTests = true; // ::1 stands in for a global address
    }

    static int echo;
    private final List<AutoCloseable> open = new ArrayList<AutoCloseable>();
    private final List<String> toasts = Collections.synchronizedList(new ArrayList<String>());

    @org.junit.jupiter.api.BeforeEach
    void toasts()
    {
        JoinService.INSTANCE.setToasts(toasts::add);
    }

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
    void close() throws Exception
    {
        JoinService.INSTANCE.closeAll();
        JoinService.INSTANCE.setDirectLookup(null);
        for (AutoCloseable c : open)
        {
            c.close();
        }
    }

    /** A plain TCP world on 127.0.0.1 that answers every byte with the same byte plus one (tells it from the echo relay). */
    private int world() throws Exception
    {
        final ServerSocket ss = new ServerSocket(0, 8, InetAddress.getLoopbackAddress());
        open.add(ss);
        Thread t = new Thread(() -> {
            try
            {
                while (true)
                {
                    final Socket s = ss.accept();
                    new Thread(() -> {
                        try
                        {
                            InputStream in = s.getInputStream();
                            OutputStream out = s.getOutputStream();
                            for (int b; (b = in.read()) >= 0; )
                            {
                                out.write((b + 1) & 0xff);
                            }
                        }
                        catch (Exception e)
                        {
                            // closed
                        }
                    }).start();
                }
            }
            catch (Exception e)
            {
                // closed
            }
        });
        t.setDaemon(true);
        t.start();
        return ss.getLocalPort();
    }

    private static ListEntry entry(String route)
    {
        ListEntry e = JoinServiceTest.entry();
        e.room = "echo";
        e.route = route;
        return e;
    }

    private static byte[] roundTrip(int port, byte[] out) throws Exception
    {
        Socket s = new Socket(InetAddress.getLoopbackAddress(), port);
        s.getOutputStream().write(out);
        s.setSoTimeout(5000);
        byte[] back = new byte[out.length];
        new DataInputStream(s.getInputStream()).readFully(back);
        s.close();
        return back;
    }

    @Test
    void aLoginGoesStraightToTheHostWhenItAnswers() throws Exception
    {
        Direct.Gate g = Direct.Gate.open(world(), "echo", soFar -> LoginName.of(soFar) == null ? Pipe.Gate.MORE : Pipe.Gate.ALLOW, null);
        open.add(g);
        JoinService.INSTANCE.setDirectLookup(room -> Collections.singletonList("[::1]:" + g.port()));
        int port = JoinService.INSTANCE.listen(entry("direct"), ModSet.of(Collections.<String[]>emptyList()));
        byte[] login = LoginNameTest.join(LoginNameTest.handshake(2), LoginNameTest.loginStart("Rizky"));
        byte[] back = roundTrip(port, login);
        assertEquals((login[0] + 1) & 0xff, back[0] & 0xff); // the host's world answered, not the echo relay
        assertEquals("direct", JoinService.INSTANCE.routeOf("Ucil world"));
        assertEquals(Collections.singletonList("Joined Ucil world directly (faster)"), toasts);
    }

    @Test
    void aLoginFallsBackToTheRelayWhenTheHostDoesNotAnswer() throws Exception
    {
        ServerSocket dead = new ServerSocket(0);
        final int deadPort = dead.getLocalPort();
        dead.close();
        JoinService.INSTANCE.setDirectLookup(room -> Collections.singletonList("[::1]:" + deadPort));
        int port = JoinService.INSTANCE.listen(entry("direct"), ModSet.of(Collections.<String[]>emptyList()));
        byte[] login = LoginNameTest.join(LoginNameTest.handshake(2), LoginNameTest.loginStart("Rizky"));
        assertArrayEquals(login, roundTrip(port, login)); // the echo relay
        assertEquals("relay", JoinService.INSTANCE.routeOf("Ucil world"));
        assertEquals(Collections.singletonList("Joined Ucil world through the relay"), toasts);
    }

    @Test
    void aRowPingNeverProbesTheHost() throws Exception
    {
        final AtomicInteger asked = new AtomicInteger();
        JoinService.INSTANCE.setDirectLookup(room -> {
            asked.incrementAndGet();
            return Collections.<String>emptyList();
        });
        int port = JoinService.INSTANCE.listen(entry("direct"), ModSet.of(Collections.<String[]>emptyList()));
        byte[] ping = LoginNameTest.handshake(1);
        assertArrayEquals(ping, roundTrip(port, ping)); // through the relay
        assertEquals(0, asked.get(), "only a real join may make the game contact a listed address");
    }

    @Test
    void aRelayWorldIsNeverProbed() throws Exception
    {
        final AtomicInteger asked = new AtomicInteger();
        JoinService.INSTANCE.setDirectLookup(room -> {
            asked.incrementAndGet();
            return Collections.<String>emptyList();
        });
        int port = JoinService.INSTANCE.listen(entry("relay"), ModSet.of(Collections.<String[]>emptyList()));
        byte[] login = LoginNameTest.join(LoginNameTest.handshake(2), LoginNameTest.loginStart("Rizky"));
        assertArrayEquals(login, roundTrip(port, login));
        assertEquals(0, asked.get());
    }
}
