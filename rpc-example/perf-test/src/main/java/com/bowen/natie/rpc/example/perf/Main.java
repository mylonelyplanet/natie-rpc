package com.bowen.natie.rpc.example.perf;

import com.bowen.natie.rpc.basic.dto.EnvRequest;
import com.bowen.natie.rpc.example.api.Greeting;
import com.bowen.natie.rpc.proxy.RpcProxy;
import com.bowen.natie.rpc.proxy.StartProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ScheduledExecutorTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by mylonelyplanet on 16/8/22.
 */
@SpringBootApplication
public class Main {

    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    private static ApplicationContext ctx;

    public static void main(String[] args) throws Exception{
        List<Object> apps = new ArrayList();
        apps.add(StartProxy.class);
        apps.add(Main.class);
        ApplicationContext ctx = SpringApplication.run(apps.toArray(), args);

        ExecutorService exec = Executors.newCachedThreadPool();

        logger.info("start test....... ");
        long start_time = System.currentTimeMillis();
        for(int i =0; i < 100; i ++){
            final int num = i;
            Callable<String> task = new Callable<String>() {
                @Override
                public String call() throws Exception {

                    RpcProxy helper = ctx.getBean(RpcProxy.class);
                    Greeting serviceHelper = helper.create(Greeting.class);
                    EnvRequest env = new EnvRequest();
                    Thread.currentThread().sleep(1000);
                    String result = serviceHelper.hello("baby" + num, env);
                    System.out.println("运行结果为:" + result);
                    return result;
                }
            };
            System.out.println("子进程正在运行: " +num);
            exec.submit(task);
        }

        long end_time = System.currentTimeMillis();
        logger.info("end test....... ");
        System.out.println(end_time - start_time);
        exec.shutdown();
    }

}
