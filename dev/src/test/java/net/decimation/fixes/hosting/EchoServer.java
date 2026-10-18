package net.decimation.fixes.hosting;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** A WebSocket echo server on any path (tests): binary back as binary, text back as "echo <text>"; frame sizes recorded. */
final class EchoServer
{
    static final List<Integer> frameSizes = new CopyOnWriteArrayList<Integer>();
    static final java.util.concurrent.atomic.AtomicInteger pings = new java.util.concurrent.atomic.AtomicInteger();
    /** /hop/A,B,C: the n-th connection on that path is told it landed in data center n (x-colo), as the relay does. */
    static final java.util.Map<String, java.util.concurrent.atomic.AtomicInteger> hops = new java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.atomic.AtomicInteger>();
    private static NioEventLoopGroup group;
    private static Channel server;

    static synchronized int start() throws InterruptedException
    {
        if (server == null)
        {
            group = new NioEventLoopGroup(1);
            server = new ServerBootstrap().group(group).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>()
            {
                protected void initChannel(SocketChannel ch)
                {
                    ch.pipeline().addLast(new HttpServerCodec(), new HttpObjectAggregator(1 << 20), new SimpleChannelInboundHandler<Object>()
                    {
                        private WebSocketServerHandshaker hs;

                        protected void channelRead0(ChannelHandlerContext ctx, Object msg)
                        {
                            if (msg instanceof FullHttpRequest)
                            {
                                FullHttpRequest r = (FullHttpRequest) msg;
                                hs = new WebSocketServerHandshakerFactory("ws://127.0.0.1" + r.getUri(), null, false, 1 << 20).newHandshaker(r);
                                io.netty.handler.codec.http.HttpHeaders extra = new io.netty.handler.codec.http.DefaultHttpHeaders();
                                if (r.getUri().startsWith("/hop/"))
                                {
                                    String[] colos = r.getUri().substring(5).split(",");
                                    int n = hops.computeIfAbsent(r.getUri(), k -> new java.util.concurrent.atomic.AtomicInteger()).getAndIncrement();
                                    extra.add("x-colo", colos[Math.min(n, colos.length - 1)]);
                                }
                                hs.handshake(ctx.channel(), r, extra, ctx.channel().newPromise());
                                if (r.getUri().startsWith("/join/"))
                                {
                                    ctx.writeAndFlush(new TextWebSocketFrame("{\"ready\":true}")); // as the relay does once the host answers
                                }
                                if (r.getUri().startsWith("/join/full"))
                                {
                                    ctx.close();
                                }
                                return;
                            }
                            WebSocketFrame f = (WebSocketFrame) msg;
                            if (f instanceof BinaryWebSocketFrame)
                            {
                                frameSizes.add(f.content().readableBytes());
                                ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.copiedBuffer(f.content())));
                            }
                            else if (f instanceof TextWebSocketFrame)
                            {
                                ctx.writeAndFlush(new TextWebSocketFrame("echo " + ((TextWebSocketFrame) f).text()));
                            }
                            else if (f instanceof io.netty.handler.codec.http.websocketx.PingWebSocketFrame)
                            {
                                pings.incrementAndGet();
                                ctx.writeAndFlush(new io.netty.handler.codec.http.websocketx.PongWebSocketFrame(Unpooled.copiedBuffer(f.content())));
                            }
                            else if (f instanceof CloseWebSocketFrame)
                            {
                                hs.close(ctx.channel(), (CloseWebSocketFrame) f.retain());
                            }
                        }
                    });
                }
            }).bind("127.0.0.1", 0).sync().channel();
        }
        return ((java.net.InetSocketAddress) server.localAddress()).getPort();
    }

    static synchronized void stop()
    {
        if (server != null)
        {
            server.close();
            group.shutdownGracefully();
            server = null;
        }
    }
}
