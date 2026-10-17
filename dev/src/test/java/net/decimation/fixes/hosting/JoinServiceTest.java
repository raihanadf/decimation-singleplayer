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
        int port = JoinService.INSTANCE.listen(entry("gvc", "0.6.1"), ModSet.of(Collections.<String[]>emptyList()));
        Socket s = new Socket(InetAddress.getLoopbackAddress(), port);
        s.setSoTimeout(2000);
        assertEquals(-1, s.getInputStream().read());
        Thread.sleep(100);
        assertEquals(Collections.singletonList("You need gvc 0.6.1 to join Ucil world"), toasts);
    }

    @Test
    void aJoinIsCarriedToTheRoom() throws Exception
    {
        int port = JoinService.INSTANCE.listen(entry(), ModSet.of(Collections.<String[]>emptyList()));
        assertEquals(port, JoinService.INSTANCE.portOf("Ucil world"));
        Socket s = new Socket(InetAddress.getLoopbackAddress(), port);
        // the echo relay never says ready: nothing comes back, the pipe gives up after its wait
        s.setSoTimeout(1000);
        assertThrows(java.net.SocketTimeoutException.class, () -> new DataInputStream(s.getInputStream()).readByte());
        s.close();
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
}
