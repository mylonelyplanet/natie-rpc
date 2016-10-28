package com.bowen.natie.rpc.proxy;

import com.bowen.natie.rpc.basic.registry.zookeeper.DiscoverAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by mylonelyplanet on 16/7/25.
 */

@SpringBootApplication
public class StartProxy {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Value("${registry.address}")
    private String registryAddress;

    @Bean(name="rpcProxy")
    public RpcProxy init(){

        try {
            if(registryAddress != null){
                System.setProperty("registry.address", registryAddress);
            }

            RpcProxy rpcProxy = new RpcProxy(DiscoverAgent.getInstance());

            return rpcProxy;

        }catch (Exception e){
            LOGGER.error("start Proxy fail: {}", e.getMessage());
        }

        return null;
    }

}
