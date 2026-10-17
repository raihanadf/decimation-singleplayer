package net.decimation.fixes.hosting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class LoginNameTest
{
    static void varint(ByteArrayOutputStream o, int v)
    {
        while ((v & ~0x7F) != 0)
        {
            o.write((v & 0x7F) | 0x80);
            v >>>= 7;
        }
        o.write(v);
    }

    static void string(ByteArrayOutputStream o, String s)
    {
        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        varint(o, b.length);
        o.write(b, 0, b.length);
    }

    /** A packet: length, id 0, body. */
    static byte[] packet(byte[] body)
    {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        varint(o, body.length + 1);
        o.write(0);
        o.write(body, 0, body.length);
        return o.toByteArray();
    }

    static byte[] handshake(int next)
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        varint(b, 5);
        string(b, "127.0.0.1");
        b.write(0x63);
        b.write(0xDD);
        varint(b, next);
        return packet(b.toByteArray());
    }

    static byte[] loginStart(String name)
    {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        string(b, name);
        return packet(b.toByteArray());
    }

    static byte[] join(byte[]... parts)
    {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for (byte[] p : parts) o.write(p, 0, p.length);
        return o.toByteArray();
    }

    @Test
    void readsTheNameOfALogin()
    {
        assertEquals("Ucil", LoginName.of(join(handshake(2), loginStart("Ucil"))));
    }

    @Test
    void waitsForMoreBytes()
    {
        byte[] all = join(handshake(2), loginStart("Ucil"));
        byte[] part = new byte[all.length - 2];
        System.arraycopy(all, 0, part, 0, part.length);
        assertNull(LoginName.of(part));
    }

    @Test
    void aStatusPingIsNoLogin()
    {
        assertEquals("", LoginName.of(handshake(1)));
    }

    @Test
    void garbageIsRefusedAsNoName()
    {
        assertEquals("", LoginName.of(new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 1}));
    }
}
