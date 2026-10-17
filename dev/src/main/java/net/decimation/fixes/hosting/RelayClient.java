package net.decimation.fixes.hosting;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The list calls of the relay (docs/superpowers/specs/2026-10-18-player-hosting-part1-design.md 3.2) over HTTP(S);
 * RelayFull when Cloudflare's free daily limit is reached (spec 5). Our own Lobby answers its refusals as JSON
 * ({"error": ...}): those are plain IOExceptions, never RelayFull.
 */
public final class RelayClient
{
    /** The deployed relay (plan Task 11 writes the real address here). */
    public static final String DEPLOYED = "https://deciremnant-relay.invalid";

    public static final class RelayFull extends IOException
    {
        RelayFull(String m)
        {
            super(m);
        }
    }

    private final String base;

    public RelayClient(String base)
    {
        this.base = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }

    /** -Ddeciworldgen.relay overrides the deployed address (dev tests, a local `wrangler dev`). */
    public static String base()
    {
        return System.getProperty("deciworldgen.relay", DEPLOYED);
    }

    public String[] host(String name, String player, boolean online, int max, ModSet mods, String version) throws IOException
    {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("name", name);
        m.put("host", player);
        m.put("online", online);
        m.put("max", max);
        m.put("mods", mods.pairs());
        m.put("version", version);
        JsonObject o = new Gson().fromJson(call("POST", "/list/host", new Gson().toJson(m)), JsonObject.class);
        return new String[] {o.get("room").getAsString(), o.get("secret").getAsString()};
    }

    /** False when the list forgot the world (missed heartbeats): register again. */
    public boolean beat(String room, String secret, int players) throws IOException
    {
        String a = call("POST", "/list/beat", "{\"room\":\"" + room + "\",\"secret\":\"" + secret + "\",\"players\":" + players + "}");
        return !a.contains("\"gone\"");
    }

    public void stop(String room, String secret) throws IOException
    {
        call("POST", "/list/stop", "{\"room\":\"" + room + "\",\"secret\":\"" + secret + "\"}");
    }

    public List<ListEntry> list() throws IOException
    {
        return ListEntry.parse(call("GET", "/list", null));
    }

    private String ws()
    {
        return base.startsWith("https") ? "wss" + base.substring(5) : "ws" + base.substring(4);
    }

    public URI hostSocket(String room, String secret)
    {
        return URI.create(ws() + "/host/" + room + "?secret=" + secret);
    }

    public URI dataSocket(String room, String secret, String id)
    {
        return URI.create(ws() + "/host/" + room + "/data/" + id + "?secret=" + secret);
    }

    public URI joinSocket(String room)
    {
        return URI.create(ws() + "/join/" + room);
    }

    private String call(String method, String path, String body) throws IOException
    {
        HttpURLConnection c = (HttpURLConnection) new URL(base + path).openConnection();
        c.setConnectTimeout(10000);
        c.setReadTimeout(10000);
        c.setRequestMethod(method);
        c.setRequestProperty("User-Agent", "DeciRemnant");
        if (body != null)
        {
            c.setDoOutput(true);
            c.setRequestProperty("Content-Type", "application/json");
            OutputStream o = c.getOutputStream();
            o.write(body.getBytes(StandardCharsets.UTF_8));
            o.close();
        }
        int status = c.getResponseCode();
        InputStream in = status < 400 ? c.getInputStream() : c.getErrorStream();
        String text = in == null ? "" : read(in);
        boolean ours = text.contains("\"error\"");
        if (!ours && (status == 429 || status >= 500 || text.contains("error code: 1027") || text.contains("error code: 1015")))
        {
            throw new RelayFull(status + " " + text);
        }
        if (status >= 400)
        {
            throw new IOException(status + " " + text);
        }
        return text;
    }

    private static String read(InputStream in) throws IOException
    {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) > 0; )
        {
            o.write(b, 0, n);
        }
        in.close();
        return new String(o.toByteArray(), StandardCharsets.UTF_8);
    }
}
