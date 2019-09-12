package com.bowen.natie.example.noweb.curator;

/**
 * Created by mylonelyplanet on 2019/5/30.
 */
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public final class Client {

    static final String HOST = System.getProperty("host", "192.168.100.166");
    static final int PORT = Integer.parseInt(System.getProperty("port", "16908"));

    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group);

            b.channel(NioSocketChannel.class);

            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();

                    p.addLast(new StringDecoder());
                    p.addLast(new StringDecoder());
                    p.addLast(new ClientHandler());
                }
            });

            b.option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.connect(HOST, PORT).sync();

        }catch (Exception e){
            System.out.println("Exception");
        }
        finally {
            group.shutdownGracefully();
        }
    }
}