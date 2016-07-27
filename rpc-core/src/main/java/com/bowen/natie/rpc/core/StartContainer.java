package com.bowen.natie.rpc.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Created by mylonelyplanet on 16/7/25.
 */
@SpringBootApplication
public class StartContainer {

    @Value("${registry.address}")
    private String registryAddress;

    @Value("${service.address}")
    private String serviceAddress="127.0.0.1:8000";

    @Bean(name="serviceRegistry")
    public ServiceRegistry initRegistry(){
        ServiceRegistry registry = new ServiceRegistry(registryAddress);
        return registry;
    }

    public static void main(String[] args) {
        SpringApplication.run(StartContainer.class,args);
    }
}
