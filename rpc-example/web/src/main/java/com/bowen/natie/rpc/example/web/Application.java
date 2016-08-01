package com.bowen.natie.rpc.example.web;

import com.bowen.natie.rpc.proxy.StartProxy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mylonelyplanet on 16/7/27.
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        List<Object> apps = new ArrayList();
        apps.add(StartProxy.class);
        apps.add(Application.class);

        SpringApplication.run(apps.toArray(),args);
    }
}
