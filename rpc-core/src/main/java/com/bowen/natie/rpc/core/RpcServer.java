package com.bowen.natie.rpc.core;

import com.bowen.natie.rpc.basic.dto.RpcRequest;
import com.bowen.natie.rpc.basic.dto.RpcResponse;
import com.bowen.natie.rpc.basic.protocol.RpcDecoder;
import com.bowen.natie.rpc.basic.protocol.RpcEncoder;
import com.bowen.natie.rpc.basic.registry.RegisterAgent;
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
public class RpcServer implements ApplicationContextAware{

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);


    private Map<String, Object> handlerMap = new HashMap<>();

    public RpcServer(){
    }

    public void startServer(final boolean sslFlag) throws Exception{

        //Detect the final port and log it.
        final int finalPort = checkPort(-1);

        //service register
        checkHostConfig(finalPort);
        syncServiceInfo(finalPort, sslFlag);
        startNetty(finalPort);

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


    public void startNetty(final int port) throws Exception {
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


            ChannelFuture future = bootstrap.bind(port).sync();
            LOGGER.info("server started on port {}", port);


            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    /** 检测端口的合法性（即是否在制定范围内） */
    private static int checkPort(int port) {
        int finalPort = port;
        // 如果未设置端口，从1080开始查找一可用端口
        if (finalPort == -1) {
            int minPort = 1080;// 起始端口
            int maxPort = 65535;// 最大端口
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
    /**
     * 注册到ZK上
     *
     * @param port 监听的端口
     * @param isSSL 是否为SSL
     */
    private static void syncServiceInfo(int port, boolean isSSL) throws Exception {
        RegisterAgent.getInstance().registService(port, isSSL);
    }
}
