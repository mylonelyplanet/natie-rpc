package com.bowen.natie.rpc.proxy;

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

    @Value("${registry.address}")
    private String registryAddress;

    @Bean(name="rpcProxy")
    public RpcProxy init(){

        ServiceDiscovery serviceDiscovery = new ServiceDiscovery(registryAddress);
        RpcProxy rpcProxy = new RpcProxy(serviceDiscovery);
        return rpcProxy;
    }

}
