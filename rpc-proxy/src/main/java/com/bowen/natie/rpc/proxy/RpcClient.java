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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by mylonelyplanet on 16/7/9.
 */

public class RpcClient extends SimpleChannelInboundHandler<RpcResponse>{

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);

    private String host;
    private int port;

    private RpcResponse response;

    private final Object obj = new Object();

    public RpcClient(String host, int port){
        this.host = host;
        this.port = port;
    }


    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        LOGGER.info("proxy read response: {}", rpcResponse);
        this.response = rpcResponse;
        synchronized (obj){
            obj.notifyAll(); //收到响应,唤醒线程.
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("proxy caught exception: {}", cause);
        ctx.close();
    }

    public RpcResponse send(RpcRequest request) throws Exception {
        LOGGER.info("proxy sending request: {}", request);
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    channel.pipeline().addLast(new RpcEncoder(RpcRequest.class)) //encode sending request
                            .addLast(new RpcDecoder(RpcResponse.class)) // decode received response
                            .addLast(RpcClient.this); // using RpcClient to process
                }
            }).option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.connect(host,port).sync();
            future.channel().writeAndFlush(request).sync();
            LOGGER.info("proxy waiting..... ");
            synchronized (obj){
                obj.wait();
            }
            if(this.response != null ){
                future.channel().closeFuture().sync();
            }
            LOGGER.info("proxy receive response: {}", this.response);
            return this.response;
        }finally {
            group.shutdownGracefully();
        }
    }
}
