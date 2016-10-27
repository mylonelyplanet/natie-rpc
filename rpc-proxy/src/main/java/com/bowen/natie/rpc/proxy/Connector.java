package com.bowen.natie.rpc.proxy;

import com.bowen.natie.rpc.basic.dto.RpcRequest;
import com.bowen.natie.rpc.basic.dto.RpcResponse;
import com.bowen.natie.rpc.basic.protocol.RpcDecoder;
import com.bowen.natie.rpc.basic.protocol.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by mylonelyplanet on 16/8/23.
 */

public class Connector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Bootstrap bootstrap = new Bootstrap();
    private String host;
    private int port;

    public Connector(String host,int port){
        this.host = host;
        this.port = port;

        EventLoopGroup workerGroup = new NioEventLoopGroup(20,new DefaultThreadFactory("natie-client-worker",Boolean.TRUE));

        this.bootstrap.group(workerGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel channel) throws Exception {
                channel.pipeline().addLast(new RpcEncoder(RpcRequest.class)) //encode sending request
                        .addLast(new RpcDecoder(RpcResponse.class)) // decode received response
                        .addLast(new RpcClient(host,port)); // using RpcClient to process
            }
        }).option(ChannelOption.SO_KEEPALIVE, true);
    }

    public Channel syncConnect(String host, int port) throws Exception{

        ChannelFuture connectFuture = null;
        Bootstrap strap = this.bootstrap;
        connectFuture = strap.connect(host,port);

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
