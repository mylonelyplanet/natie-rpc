package com.bowen.natie.rpc.core;

import com.bowen.natie.rpc.client.RpcRequest;
import com.bowen.natie.rpc.client.RpcResponse;
import com.bowen.natie.rpc.common.RpcDecoder;
import com.bowen.natie.rpc.common.RpcEncoder;
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

    private String serverAddress;
    private ServiceRegistry serviceRegistry;

    private Map<String, Object> handlerMap = new HashMap<>();

    public RpcServer(String serverAddress) {this.serverAddress = serverAddress;}

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry){
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        //获取所有带有RpcService注解的SpringBean
        System.out.println("=================get @RpcService Bean=========");
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
                            channel.pipeline().addLast(new RpcDecoder(RpcRequest.class))  //反序列化，处理请求
                                    .addLast(new RpcEncoder(RpcResponse.class))   //序列化,发送响应
                                    .addLast(new RpcHandler(handlerMap)); //处理rpc请求
                        }
                    }).option(ChannelOption.SO_BACKLOG,128).childOption(ChannelOption.SO_KEEPALIVE,true);
            String[] array = serverAddress.split(":");
            String host = array[0];
            int port = Integer.parseInt(array[1]);

            ChannelFuture future = bootstrap.bind(host,port).sync();
            LOGGER.info("server started on port {}", port);

            if(serviceRegistry != null){
                serviceRegistry.registry(serverAddress);  // 注册服务地址
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