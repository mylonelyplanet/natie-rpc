package com.bowen.natie.example.noweb.curator;

import com.bowen.natie.rpc.basic.dto.RpcRequest;
import com.bowen.natie.rpc.basic.dto.RpcResponse;
import com.bowen.natie.rpc.proxy.ConnectionPool;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by bowen.jin on 2016-10-28.
 */
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler {

    public ClientHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Channel Active");

    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        System.out.println("client received: " + msg);
        ctx.writeAndFlush(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        cause.printStackTrace();
        ctx.close();
    }

}