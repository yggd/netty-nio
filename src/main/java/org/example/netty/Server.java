package org.example.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.example.netty.handler.ServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private final int port;
    private final boolean sync;
    private final EventLoopGroup group = new NioEventLoopGroup();

    public Server(int port) {
        this.port = port;
        this.sync = true;
    }

    public static void main(String[] args) throws IOException {
        try (Server s = new Server(54321, false)) {
            s.start();
            System.in.read(); // prevent main thread from terminating.
        }
    }

    public Server(int port, boolean sync) {
        this.port = port;
        this.sync = sync;
    }

    public void start() {
        final ServerBootstrap b = new ServerBootstrap();
        b.group(group).channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(port))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new ServerHandler());
                    }
                });
        logger.info("server bootstrap started.");
        ChannelFuture f = null;
        try {
            f = b.bind().sync();
        } catch (InterruptedException e) {
            // through interruption.
        }
        if (f != null && sync) {
            sync(f);
        }
    }

    void sync(ChannelFuture f) {
        try {
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            // through interruption.
        }
    }

    @Override
    public void close() {
        try {
            logger.info("server bootstrap shutdown.");
            group.shutdownGracefully().sync();
        } catch (InterruptedException e) {
            // through interruption.
        }
    }
}
