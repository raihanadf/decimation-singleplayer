package net.decimation.fixes.hosting;

import java.nio.charset.StandardCharsets;

/**
 * The player name in the first bytes of a Minecraft 1.7.10 connection: the handshake (packet 0: protocol, address,
 * port, next state) and, for a login (next state 2), the login start (packet 0: name). Unencrypted until after the name,
 * so the host can refuse a name before the connection reaches its world.
 */
final class LoginName
{
    /** A well formed status ping (the server list): no name to check. */
    static final String STATUS = "";
    /** Bytes this reader cannot follow: refused, never let through (a reader Minecraft disagrees with must fail closed). */
    static final String BAD = "\u0000";

    private LoginName()
    {
    }

    /** The name; null while more bytes are needed; STATUS for a status ping; BAD for anything else. */
    static String of(byte[] b)
    {
        if (b.length > 0 && (b[0] & 0xFF) == 0xFE)
        {
            return BAD; // the legacy server list ping (before 1.7): not this protocol
        }
        int[] at = {0};
        int len = varint(b, at);
        if (len > 300)
        {
            return BAD; // a 1.7.10 handshake is far shorter: never wait for more of it
        }
        if (len == -2)
        {
            return BAD;
        }
        if (len < 0 || b.length < at[0] + len)
        {
            return null;
        }
        int end = at[0] + len;
        if (varint(b, at) != 0)
        {
            return BAD;
        }
        varint(b, at); // protocol
        int addr = varint(b, at);
        if (addr < 0 || addr > 255 || at[0] + addr + 2 > end)
        {
            return BAD;
        }
        at[0] += addr + 2; // address, port
        int next = varint(b, at);
        if (next == 1 && at[0] == end)
        {
            return STATUS;
        }
        if (next != 2 || at[0] != end)
        {
            return BAD;
        }
        at[0] = end;
        int len2 = varint(b, at);
        if (len2 == -2 || len2 > 100)
        {
            return BAD;
        }
        if (len2 < 0 || b.length < at[0] + len2)
        {
            return null;
        }
        if (varint(b, at) != 0)
        {
            return BAD;
        }
        int n = varint(b, at);
        if (n < 1 || n > 64 || at[0] + n > b.length)
        {
            return BAD;
        }
        String name = new String(b, at[0], n, StandardCharsets.UTF_8);
        return name.indexOf('\u0000') >= 0 ? BAD : name;
    }

    /** The handshake's next state (1 status ping, 2 login), -1 while it cannot be read yet or at all. */
    static int nextState(byte[] b)
    {
        int[] at = {0};
        int len = varint(b, at);
        if (len < 0 || len > 300 || b.length < at[0] + len || varint(b, at) != 0)
        {
            return -1;
        }
        varint(b, at); // protocol
        int addr = varint(b, at);
        if (addr < 0 || addr > 255)
        {
            return -1;
        }
        at[0] += addr + 2;
        return at[0] < b.length ? varint(b, at) : -1;
    }

    /** A VarInt at at[0] (moved past it); -1 when the bytes end inside it, -2 when it is longer than 5 bytes. */
    private static int varint(byte[] b, int[] at)
    {
        int v = 0;
        for (int i = 0; i < 5; i++)
        {
            if (at[0] >= b.length)
            {
                return -1;
            }
            int c = b[at[0]++] & 0xFF;
            v |= (c & 0x7F) << (7 * i);
            if ((c & 0x80) == 0)
            {
                return v;
            }
        }
        return -2;
    }
}
