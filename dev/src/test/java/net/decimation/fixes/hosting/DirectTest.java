package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Direct joins over IPv6 (docs/player_hosting.md section 15): the host's addresses, its gate, the joiner's probe. */
class DirectTest
{
    private final List<AutoCloseable> open = new ArrayList<AutoCloseable>();

    @AfterEach
    void closeAll() throws Exception
    {
        for (AutoCloseable c : open)
        {
            c.close();
        }
    }

    /** A plain TCP echo server standing in for the hosted world's LAN port. */
    private int lanEcho() throws Exception
    {
        final ServerSocket ss = new ServerSocket(0, 8, InetAddress.getLoopbackAddress());
        open.add(ss);
        Thread t = new Thread(new Runnable()
        {
            public void run()
            {
                try
                {
                    while (true)
                    {
                        final Socket s = ss.accept();
                        new Thread(new Runnable()
                        {
                            public void run()
                            {
                                try
                                {
                                    InputStream in = s.getInputStream();
                                    OutputStream out = s.getOutputStream();
                                    byte[] b = new byte[4096];
                                    for (int n; (n = in.read(b)) > 0; )
                                    {
                                        out.write(b, 0, n);
                                    }
                                }
                                catch (Exception e)
                                {
                                    // closed
                                }
                            }
                        }).start();
                    }
                }
                catch (Exception e)
                {
                    // closed
                }
            }
        });
        t.setDaemon(true);
        t.start();
        return ss.getLocalPort();
    }

    private static Pipe.Gate gate(final int verdict)
    {
        return new Pipe.Gate()
        {
            public int decide(byte[] soFar)
            {
                return LoginName.of(soFar) == null ? MORE : verdict;
            }
        };
    }

    @Test
    void onlyGlobalIpv6AddressesAreOffered() throws Exception
    {
        List<InetAddress> all = Arrays.asList(InetAddress.getByName("2001:db8::5"), InetAddress.getByName("fe80::1"),
            InetAddress.getByName("fd00::1"), InetAddress.getByName("::1"), InetAddress.getByName("127.0.0.1"),
            InetAddress.getByName("192.168.1.8"), InetAddress.getByName("2400:cb00:2049::1"));
        assertEquals(Arrays.asList("[2001:db8:0:0:0:0:0:5]:51234", "[2400:cb00:2049:0:0:0:0:1]:51234"), Direct.addresses(all, 51234));
    }

    @Test
    void atMostFourAddresses() throws Exception
    {
        List<InetAddress> all = new ArrayList<InetAddress>();
        for (int i = 1; i <= 6; i++)
        {
            all.add(InetAddress.getByName("2001:db8::" + i));
        }
        assertEquals(4, Direct.addresses(all, 51234).size());
    }

    @Test
    void theGateAnswersTheProbeForItsRoomOnly() throws Exception
    {
        Direct.Gate g = Direct.Gate.open(lanEcho(), "room1", gate(Pipe.Gate.ALLOW), null);
        open.add(g);
        String at = "[::1]:" + g.port();
        assertTrue(Direct.probe(at, "room1", 2000));
        assertFalse(Direct.probe(at, "room2", 2000));
    }

    @Test
    void theProbeFailsFastWhereNothingListens() throws Exception
    {
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        ss.close();
        long t = System.currentTimeMillis();
        assertFalse(Direct.probe("[::1]:" + port, "room1", 1500));
        assertTrue(System.currentTimeMillis() - t < 3000);
    }

    @Test
    void anAllowedLoginReachesTheWorld() throws Exception
    {
        final AtomicReference<String> closed = new AtomicReference<String>();
        Direct.Gate g = Direct.Gate.open(lanEcho(), "room1", gate(Pipe.Gate.ALLOW), new Direct.Closed()
        {
            public void closed(String name)
            {
                closed.set(name);
            }
        });
        open.add(g);
        Socket s = new Socket(InetAddress.getByName("::1"), g.port());
        byte[] login = LoginNameTest.join(LoginNameTest.handshake(2), LoginNameTest.loginStart("Rizky"));
        s.getOutputStream().write(login);
        s.setSoTimeout(3000);
        byte[] back = new byte[login.length];
        new DataInputStream(s.getInputStream()).readFully(back); // the world (echo) answered through the gate
        assertArrayEquals(login, back);
        s.close();
        for (int i = 0; i < 30 && closed.get() == null; i++)
        {
            Thread.sleep(100);
        }
        assertEquals("Rizky", closed.get()); // the name reservation is freed when the connection ends
        assertEquals(0, g.connections());
    }

    @Test
    void aRefusedLoginNeverReachesTheWorld() throws Exception
    {
        Direct.Gate g = Direct.Gate.open(lanEcho(), "room1", gate(Pipe.Gate.DENY), null);
        open.add(g);
        Socket s = new Socket(InetAddress.getByName("::1"), g.port());
        s.getOutputStream().write(LoginNameTest.join(LoginNameTest.handshake(2), LoginNameTest.loginStart("Rizky")));
        s.setSoTimeout(3000);
        assertEquals(-1, s.getInputStream().read());
        s.close();
    }

    @Test
    void theJoinerUsesTheFirstAddressThatAnswers() throws Exception
    {
        Direct.Gate g = Direct.Gate.open(lanEcho(), "room1", gate(Pipe.Gate.ALLOW), null);
        open.add(g);
        ServerSocket dead = new ServerSocket(0);
        int deadPort = dead.getLocalPort();
        dead.close();
        assertEquals("[::1]:" + g.port(), Direct.pick(Arrays.asList("[::1]:" + deadPort, "[::1]:" + g.port()), "room1", 1500));
        assertNull(Direct.pick(Collections.singletonList("[::1]:" + deadPort), "room1", 1500));
        assertNull(Direct.pick(Collections.<String>emptyList(), "room1", 1500));
    }
}
