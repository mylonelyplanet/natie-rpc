package com.bowen.natie.rpc.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by mylonelyplanet on 16/7/25.
 */
@SpringBootApplication
public class StartContainer {

    @Value("${registry.address:127.0.0.1:2181}")
    private String registryAddress;

    //// TODO: 16/8/6
    @Value("${service.host:127.0.0.1}")
    private String serviceHost;

    @Value("${service.port:7000}")
    private String servicePort;

    @Bean(name="serviceRegistry")
    public ServiceRegistry initRegistry(){

        ServiceRegistry registry = new ServiceRegistry(registryAddress);
        return registry;
    }

    @Bean(name="rpcServer")
    public RpcServer initServer(ServiceRegistry serviceRegistry){

        RpcServer rpcServer = new RpcServer(serviceHost,servicePort,serviceRegistry);

        return rpcServer;
    }

}
