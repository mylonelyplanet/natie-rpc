package com.bowen.natie.rpc.core;

import com.bowen.natie.rpc.basic.dto.RpcRequest;
import com.bowen.natie.rpc.basic.dto.RpcResponse;
import com.bowen.natie.rpc.basic.protocol.RpcDecoder;
import com.bowen.natie.rpc.basic.protocol.RpcEncoder;
import com.bowen.natie.rpc.basic.registry.zookeeper.RegisterAgent;
import com.bowen.natie.rpc.basic.util.IPUtils;
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

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mylonelyplanet on 16/7/10.
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);
    private Map<String, Object> handlerMap = new HashMap<>();

    public RpcServer(){

    }


    @Override
    public void afterPropertiesSet() throws Exception {
        //Detect the final port and log it.
        final int finalPort = checkPort(-1);
        startNetty(finalPort, false);
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
       //get bean map
        Map<String ,Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if(MapUtils.isNotEmpty(serviceBeanMap)){
            for(Object serviceBean:serviceBeanMap.values()){
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();

                handlerMap.put(interfaceName,serviceBean);
            }
        }
    }


    public void startNetty(final int port, final boolean sslFlag) throws Exception {
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
                    }).option(ChannelOption.SO_BACKLOG,128).childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(port).sync();
            LOGGER.info("server started on port {}", port);

            //service register
            checkHostConfig(port);
            syncServiceInfo(port, sslFlag);

            // Block to wait until the server socket is closed.
            if(future != null){
                future.channel().closeFuture().sync();
            }
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /* check to find which port is available */
    private static int checkPort(int port) {
        int finalPort = port;
        //start from port 1080
        if (finalPort == -1) {
            int minPort = 1080;
            int maxPort = 65535;
            boolean hasAvailablePort = false;
            for (finalPort = minPort; finalPort <= maxPort; finalPort++) {
                if (!IPUtils.isLocalPortUsing(finalPort)) {
                    hasAvailablePort = true;
                    break;
                }
            }

            if (!hasAvailablePort) {
                LOGGER.error("No available port to be used from " + minPort + " to " + maxPort);
            }
        } else if (IPUtils.isLocalPortUsing(port)) {
            LOGGER.error("Port " + port + " already in use");
        }
        return finalPort;
    }

    private static void checkHostConfig(int port) throws UnknownHostException {
        String hostAddress = IPUtils.localIp().getHostAddress();
        if (!IPUtils.isPortUsing(hostAddress, port)) {
            throw new RuntimeException(
                    "host port error, the port " + port + " in the host " + hostAddress + " is not listening");
        }
    }

    private static void syncServiceInfo(int port, boolean isSSL) throws Exception {
        RegisterAgent.getInstance().registService(port, isSSL);
    }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 200; i++) {
            System.out.println(System.nanoTime()%1000);

        }
        Thread.currentThread().sleep(1000);
        System.out.println("------------------");
        for (int i = 0; i < 20; i++) {
            System.out.println(System.nanoTime()%1000);

        }
    }

}
