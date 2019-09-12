package com.bowen.natie.example.noweb.curator;

import com.bowen.natie.rpc.proxy.ConnectionListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mylonelyplanet on 2019/5/30.
 */
public class SmsConnector {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Bootstrap bootstrap = new Bootstrap();

    public SmsConnector(){
        EventLoopGroup group = new NioEventLoopGroup();

        this.bootstrap.group(group);

        this.bootstrap.channel(NioSocketChannel.class);

        this.bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ChannelPipeline p = ch.pipeline();

                p.addLast(new StringDecoder());
                p.addLast(new StringDecoder());
                p.addLast(new ClientHandler());
            }
        });

        this.bootstrap.option(ChannelOption.SO_KEEPALIVE, true);

    }

    public Channel syncConnect(String host, int port) throws Exception{
        logger.info("客户端正在启动---------------");
        Bootstrap b = this.bootstrap;
        ChannelFuture connectFuture = b.connect(host, port);
        connectFuture.addListener(new ConnectionListener());
        Channel channel = null;
        try {
            channel = connectFuture.sync().channel();
        }catch(Exception e){
            connectFuture.cancel(true);
            channel = connectFuture.channel();
            if(channel != null){
                channel.close();
            }
            logger.error("connect server fail " + host + ":" +port);
        }
        return channel;
    }
}
