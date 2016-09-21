package com.bowen.natie.rpc.core;

import com.bowen.natie.rpc.basic.registry.zookeeper.RegisterAgent;
import com.bowen.natie.rpc.basic.util.IPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Created by mylonelyplanet on 16/7/25.
 */
@SpringBootApplication
public class StartContainer {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

//    @Value("${registry.address:127.0.0.1:2181}")
//    private String registryAddress;

    //// TODO: 16/8/6
//    @Value("${service.host:127.0.0.1}")
//    private String serviceHost;
//
//    @Value("${service.port:7000}")
//    private String servicePort;



    @Bean(name="rpcServer")
    public RpcServer initServer(){

        try {
            // 检测注册中心
            RegisterAgent.getInstance();
            // init the local address to avoid first time timeout
            IPUtils.localIp4Str();

            RpcServer rpcServer = new RpcServer();
            rpcServer.startServer(false);

            return rpcServer;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

}
