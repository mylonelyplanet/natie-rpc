package com.bowen.natie.rpc.example.service;

import com.bowen.natie.rpc.core.StartContainer;
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
        apps.add(StartContainer.class);
        apps.add(Application.class);

        SpringApplication start = new SpringApplication(apps.toArray());
        //start.setWebEnvironment(false);
        start.run(args);
    }
}

