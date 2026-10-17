package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RelayClientTest
{
    HttpServer http;
    volatile int status = 200;
    volatile String answer = "{}";
    volatile String lastBody = "";

    static byte[] readAll(InputStream in) throws IOException
    {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) > 0; )
        {
            o.write(b, 0, n);
        }
        return o.toByteArray();
    }

    @BeforeEach
    void start() throws Exception
    {
        http = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        http.createContext("/", ex -> {
            lastBody = new String(readAll(ex.getRequestBody()), StandardCharsets.UTF_8);
            byte[] b = answer.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(status, b.length);
            ex.getResponseBody().write(b);
            ex.close();
        });
        http.start();
    }

    @AfterEach
    void stop()
    {
        http.stop(0);
    }

    RelayClient client()
    {
        return new RelayClient("http://127.0.0.1:" + http.getAddress().getPort());
    }

    @Test
    void hostSendsTheEntryAndReturnsRoomAndSecret() throws Exception
    {
        answer = "{\"room\":\"r0\",\"secret\":\"s0\"}";
        String[] rs = client().host("Ucil world", "Ucil", false, 4,
                                    ModSet.of(Collections.singletonList(new String[] {"deci", "1.21.10f"})), "0.64.9");
        assertArrayEquals(new String[] {"r0", "s0"}, rs);
        assertTrue(lastBody.contains("\"name\":\"Ucil world\""), lastBody);
        assertTrue(lastBody.contains("[\"deci\",\"1.21.10f\"]"), lastBody);
    }

    @Test
    void goneBeatIsFalse() throws Exception
    {
        answer = "{\"gone\":true}";
        assertFalse(client().beat("r0", "s0", 1));
        answer = "{\"ok\":true}";
        assertTrue(client().beat("r0", "s0", 1));
    }

    @Test
    void quotaMapsToRelayFull()
    {
        status = 429;
        answer = "error code: 1027";
        assertThrows(RelayClient.RelayFull.class, () -> client().list());
        status = 503;
        answer = "busy";
        assertThrows(RelayClient.RelayFull.class, () -> client().list());
    }

    @Test
    void ourOwnLimitsAreNotTheFreePlan()
    {
        status = 429;
        answer = "{\"error\":\"too often\"}";
        IOException e = assertThrows(IOException.class, () -> client().list());
        assertFalse(e instanceof RelayClient.RelayFull);
    }

    @Test
    void socketAddressesUseWsSchemes()
    {
        assertEquals("ws://127.0.0.1:" + http.getAddress().getPort() + "/join/r0", client().joinSocket("r0").toString());
        assertTrue(new RelayClient("https://x.workers.dev").hostSocket("r0", "s0").toString().startsWith("wss://x.workers.dev/host/r0?secret="));
    }
}
