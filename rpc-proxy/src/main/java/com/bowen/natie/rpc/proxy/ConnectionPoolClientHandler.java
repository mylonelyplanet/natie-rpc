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
 * Created by bowen.jin on 2016-10-28.
 */
public class ConnectionPoolClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionPoolClientHandler.class);
    private RpcResponse response;
    private final Object obj = new Object();

    public ConnectionPoolClientHandler(){}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        this.response = rpcResponse;
        synchronized (obj){
            obj.notifyAll(); //收到响应,唤醒线程.
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)  {
        try {
            super.exceptionCaught(ctx, cause);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        LOGGER.warn("Unexpected exception：" + cause.getMessage());
        Channel channel = ctx.channel();
        ConnectionPool.getChannelPool(channel).forceClose(channel);
    }

    public RpcResponse send(Channel channel,RpcRequest request) throws Exception {

        ChannelFuture future = channel.writeAndFlush(request).sync();
        synchronized (obj){
            obj.wait();
        }
        if(this.response != null ){
            future.channel().closeFuture().sync();
        }
        return this.response;

    }
}
