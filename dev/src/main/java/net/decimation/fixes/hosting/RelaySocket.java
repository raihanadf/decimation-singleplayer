package net.decimation.fixes.hosting;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslHandler;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

/**
 * A WebSocket to the relay on the netty 4.0.10 Minecraft 1.7.10 ships (docs/superpowers/specs/2026-10-18-player-hosting-
 * part1-design.md 3.1): TLS with SNI for wss, binary writes coalesced per 50 ms (one game tick) or 64 KB so Cloudflare
 * counts fewer messages (spec 7).
 */
public final class RelaySocket
{
    public interface Listener
    {
        void binary(byte[] data);

        void text(String s);

        void closed(int code, String reason);
    }

    static final NioEventLoopGroup GROUP = new NioEventLoopGroup(2, new ThreadFactory()
    {
        public Thread newThread(Runnable r)
        {
            Thread t = new Thread(r, "DeciRemnant relay");
            t.setDaemon(true);
            return t;
        }
    });
    private static final int FLUSH_MS = 50, MAX_FRAME = 65536, TIMEOUT_S = 10;
    /**
     * Keep-alive: a WebSocket ping every 25 s. Cloudflare closes a WebSocket that carries nothing for a while (its docs; the
     * host's control socket is idle while nobody joins, and its drop kicked every player, docs/player_hosting.md 16); its
     * runtime answers pings itself without waking the room or counting a message.
     */
    static final int PING_MS = 25_000;
    private static volatile int pingMs = 0;

    /** Tests: the keep-alive period in ms (0: the default). */
    static void pingMsForTests(int ms)
    {
        pingMs = ms;
    }

    private final Channel channel;
    private final ByteBuf pending = Unpooled.buffer();
    private volatile boolean open = true;

    private final java.util.concurrent.ScheduledFuture<?> flusher, pinger;

    private RelaySocket(final Channel channel)
    {
        this.channel = channel;
        flusher = channel.eventLoop().scheduleAtFixedRate(new Runnable()
        {
            public void run()
            {
                flush();
            }
        }, FLUSH_MS, FLUSH_MS, TimeUnit.MILLISECONDS);
        int every = pingMs > 0 ? pingMs : PING_MS;
        pinger = channel.eventLoop().scheduleAtFixedRate(new Runnable()
        {
            public void run()
            {
                if (open && channel.isActive())
                {
                    channel.writeAndFlush(new io.netty.handler.codec.http.websocketx.PingWebSocketFrame());
                }
            }
        }, every, every, TimeUnit.MILLISECONDS);
    }

    /** Connects and finishes the WebSocket handshake within 10 s, else IOException (the message carries the HTTP status). */
    public static RelaySocket open(final URI uri, final Listener listener) throws IOException
    {
        final boolean tls = "wss".equals(uri.getScheme());
        final String host = uri.getHost();
        final int port = uri.getPort() > 0 ? uri.getPort() : tls ? 443 : 80;
        final WebSocketClientHandshaker hs = WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false,
                                                                                              new DefaultHttpHeaders(), 1 << 20);
        final CountDownLatch handshaken = new CountDownLatch(1);
        final Throwable[] failure = new Throwable[1];
        final String[] colo = new String[1];
        Bootstrap b = new Bootstrap().group(GROUP).channel(NioSocketChannel.class)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT_S * 1000).option(ChannelOption.TCP_NODELAY, true)
            .handler(new ChannelInitializer<SocketChannel>()
            {
                protected void initChannel(SocketChannel ch) throws Exception
                {
                    if (tls)
                    {
                        SSLEngine e = SSLContext.getDefault().createSSLEngine(host, port);
                        e.setUseClientMode(true);
                        // TLS 1.2 only: Minecraft's netty 4.0.10 never finishes a TLS 1.3 handshake with Cloudflare (timed
                        // out on the deployed relay, 18 Oktober 2026; with 1.2 the control socket opens)
                        e.setEnabledProtocols(new String[] {"TLSv1.2"});
                        SSLParameters p = e.getSSLParameters();
                        p.setServerNames(Collections.<SNIServerName>singletonList(new SNIHostName(host)));
                        p.setEndpointIdentificationAlgorithm("HTTPS"); // the certificate must name this host
                        e.setSSLParameters(p);
                        ch.pipeline().addLast(new SslHandler(e));
                    }
                    ch.pipeline().addLast(new HttpClientCodec(), new HttpObjectAggregator(1 << 20), new SimpleChannelInboundHandler<Object>()
                    {
                        public void channelActive(ChannelHandlerContext ctx)
                        {
                            hs.handshake(ctx.channel());
                        }

                        protected void channelRead0(ChannelHandlerContext ctx, Object msg)
                        {
                            if (!hs.isHandshakeComplete())
                            {
                                try
                                {
                                    colo[0] = ((FullHttpResponse) msg).headers().get("x-colo"); // the relay's data center
                                    hs.finishHandshake(ctx.channel(), (FullHttpResponse) msg);
                                }
                                catch (RuntimeException e)
                                {
                                    failure[0] = e;
                                    ctx.close();
                                }
                                handshaken.countDown();
                                return;
                            }
                            if (msg instanceof BinaryWebSocketFrame && listener != null)
                            {
                                ByteBuf c = ((BinaryWebSocketFrame) msg).content();
                                byte[] d = new byte[c.readableBytes()];
                                c.readBytes(d);
                                listener.binary(d);
                            }
                            else if (msg instanceof TextWebSocketFrame && listener != null)
                            {
                                listener.text(((TextWebSocketFrame) msg).text());
                            }
                            else if (msg instanceof CloseWebSocketFrame)
                            {
                                CloseWebSocketFrame c = (CloseWebSocketFrame) msg;
                                if (listener != null)
                                {
                                    listener.closed(c.statusCode(), c.reasonText());
                                }
                                ctx.close();
                            }
                        }

                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable t)
                        {
                            if (failure[0] == null)
                            {
                                failure[0] = t;
                            }
                            handshaken.countDown();
                            ctx.close();
                        }
                    });
                }
            });
        ChannelFuture connect = b.connect(host, port);
        if (!connect.awaitUninterruptibly(TIMEOUT_S, TimeUnit.SECONDS) || !connect.isSuccess())
        {
            connect.channel().close();
            throw new IOException("relay not reachable: " + connect.cause());
        }
        boolean done;
        try
        {
            done = handshaken.await(TIMEOUT_S, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            done = false;
        }
        if (!done || failure[0] != null || !hs.isHandshakeComplete())
        {
            connect.channel().close();
            throw new IOException("relay handshake failed: " + (failure[0] == null ? "timeout" : failure[0].getMessage()));
        }
        final RelaySocket s = new RelaySocket(connect.channel());
        s.colo = colo[0];
        connect.channel().closeFuture().addListener(new ChannelFutureListener()
        {
            public void operationComplete(ChannelFuture f)
            {
                s.flusher.cancel(false); // else every socket ever opened keeps a 50 ms task on the shared threads
                s.pinger.cancel(false);
                synchronized (s.pending)
                {
                    s.pending.clear();
                    s.pending.capacity(0);
                }
                boolean wasOpen = s.open;
                s.open = false;
                if (wasOpen && listener != null)
                {
                    listener.closed(1006, "connection lost");
                }
            }
        });
        return s;
    }

    /** When the last frame went out (nanoTime): a write after a quiet tick goes at once. */
    private volatile long lastFrame;

    /**
     * Queues bytes for the next tick's frame, or sends at once when no frame went out in the last tick (a click or a shot
     * after a quiet moment does not wait up to 50 ms; a busy stream still shares one frame per tick, the message count
     * stays as before) or when 64 KB are queued.
     */
    public void send(byte[] b, int off, int len)
    {
        synchronized (pending)
        {
            pending.writeBytes(b, off, len);
            boolean quiet = System.nanoTime() - lastFrame >= FLUSH_MS * 1_000_000L;
            if (pending.readableBytes() < MAX_FRAME && !quiet)
            {
                return;
            }
        }
        flush();
    }

    private void flush()
    {
        ByteBuf out;
        synchronized (pending)
        {
            if (pending.readableBytes() == 0)
            {
                return;
            }
            out = Unpooled.copiedBuffer(pending);
            pending.clear();
            lastFrame = System.nanoTime();
        }
        channel.writeAndFlush(new BinaryWebSocketFrame(out));
    }

    public void sendText(String s)
    {
        flush();
        channel.writeAndFlush(new TextWebSocketFrame(s));
    }

    private volatile String colo;

    /** The Cloudflare data center this socket landed in (the relay's x-colo), or null. */
    public String colo()
    {
        return colo;
    }

    /**
     * Opens and, while the socket landed far from the room's region (free Worker traffic from Indonesia lands in Singapore,
     * Frankfurt, London or Paris per connection, docs/player_hosting.md 14), closes it and opens again: at most `tries`,
     * the last one kept. The listener only ever hears the socket that is kept.
     */
    public static RelaySocket openNear(URI uri, Listener listener, String region, int tries) throws IOException
    {
        for (int i = 1; ; i++)
        {
            Forward f = new Forward();
            RelaySocket s = open(uri, f);
            if (i >= tries || Colos.near(region, s.colo()))
            {
                HostService.log("relay socket landed in " + s.colo() + (i > 1 ? " after " + i + " tries" : ""));
                f.to(listener); // early messages reach the listener now, in order
                return s;
            }
            s.close("landed far: " + s.colo()); // never handed a listener: what it says is dropped
        }
    }

    /** Holds a socket's messages until it is kept (openNear), then passes them on in order. */
    private static final class Forward implements Listener
    {
        private Listener target;
        private java.util.List<Object[]> held = new java.util.ArrayList<Object[]>();

        synchronized void to(Listener l)
        {
            target = l;
            java.util.List<Object[]> h = held;
            held = null;
            if (l != null)
            {
                for (Object[] m : h)
                {
                    deliver(l, m);
                }
            }
        }

        private static void deliver(Listener l, Object[] m)
        {
            if (m[0] == null)
            {
                l.binary((byte[]) m[1]);
            }
            else if (m[0] instanceof String)
            {
                l.text((String) m[0]);
            }
            else
            {
                l.closed((Integer) m[0], (String) m[1]);
            }
        }

        private synchronized void on(Object[] m)
        {
            if (held != null)
            {
                held.add(m);
            }
            else if (target != null)
            {
                deliver(target, m);
            }
        }

        public void binary(byte[] d)
        {
            on(new Object[] {null, d});
        }

        public void text(String s)
        {
            on(new Object[] {s, null});
        }

        public void closed(int code, String reason)
        {
            on(new Object[] {code, reason});
        }
    }

    /** Tests: cuts the connection as a network drop does (no close frame; the listener hears it closed). */
    void dropForTests()
    {
        channel.close();
    }

    /** True once the tick flush task is cancelled (tests). */
    boolean flushStopped()
    {
        return flusher.isCancelled();
    }

    public boolean isOpen()
    {
        return open && channel.isActive();
    }

    public void close(String reason)
    {
        flush();
        open = false;
        channel.writeAndFlush(new CloseWebSocketFrame(1000, reason)).addListener(ChannelFutureListener.CLOSE);
    }
}
