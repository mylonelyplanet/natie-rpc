package com.bowen.natie.rpc.core;

import com.bowen.natie.rpc.basic.registry.zookeeper.RegisterAgent;
import com.bowen.natie.rpc.basic.util.IPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Created by mylonelyplanet on 16/7/25.
 */
@SpringBootApplication
public class StartContainer {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Value("${registry.address}")
    private String registryAddress;

    @Bean(name="rpcServer")
    public RpcServer initServer(){

        try {
            if(registryAddress != null){
                System.setProperty("registry.address",registryAddress);
            }

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
