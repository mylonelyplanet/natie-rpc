package com.bowen.natie.rpc.example.test;

import com.bowen.natie.rpc.basic.dto.EnvRequest;
import com.bowen.natie.rpc.example.api.Greeting;
import com.bowen.natie.rpc.proxy.RpcProxy;
import com.bowen.natie.rpc.proxy.StartProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mylonelyplanet on 16/8/22.
 */
@SpringBootApplication
public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception{
        List<Object> apps = new ArrayList();
        apps.add(StartProxy.class);
        apps.add(Main.class);

        ApplicationContext ctx = SpringApplication.run(apps.toArray(),args);

        RpcProxy helper = ctx.getBean(RpcProxy.class);

        logger.info("start test....... ");
        long start_time = System.currentTimeMillis();

        for (int i = 0; i < 10; i++) {

            Greeting serviceHelper = helper.create(Greeting.class);
            EnvRequest env = new EnvRequest();
            serviceHelper.hello("baby", env);

        }
        long end_time = System.currentTimeMillis();
        logger.info("end test....... ");
        System.out.println(end_time-start_time);
    }
}
