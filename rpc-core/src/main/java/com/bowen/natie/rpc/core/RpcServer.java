package com.bowen.natie.rpc.core;

import com.bowen.natie.rpc.basic.dto.RpcRequest;
import com.bowen.natie.rpc.basic.dto.RpcResponse;
import com.bowen.natie.rpc.basic.protocol.RpcDecoder;
import com.bowen.natie.rpc.basic.protocol.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mylonelyplanet on 16/7/10.
 */
public class RpcServer implements ApplicationContextAware, InitializingBean{

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    private String serverHost;
    private String serverPort;
    private ServiceRegistry serviceRegistry;

    private Map<String, Object> handlerMap = new HashMap<>();


    public RpcServer(String serverHost, String serverPort, ServiceRegistry serviceRegistry){

        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
       //get bean map
        Map<String ,Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if(MapUtils.isNotEmpty(serviceBeanMap)){
            for(Object serviceBean:serviceBeanMap.values()){
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                System.out.println("BEAN: "+ interfaceName);
                handlerMap.put(interfaceName,serviceBean);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new RpcDecoder(RpcRequest.class))  //decode incoming request
                                    .addLast(new RpcEncoder(RpcResponse.class))  //encode sending response
                                    .addLast(new RpcHandler(handlerMap)); //using RpcHandler for processing
                        }
                    }).option(ChannelOption.SO_BACKLOG,128).childOption(ChannelOption.SO_KEEPALIVE,true);

            StringBuilder sb = new StringBuilder();
            String serverAddress = sb.append(serverHost).append(":").append(serverPort).toString();
            int port = Integer.parseInt(serverPort);

            ChannelFuture future = bootstrap.bind(serverHost,port).sync();
            LOGGER.info("server started on port {}", serverPort);

            if(serviceRegistry != null){
                serviceRegistry.registry(serverAddress);
            }else {
                throw new IllegalStateException("no zk found.");
            }

            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
