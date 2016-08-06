package com.bowen.natie.example.noweb.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.concurrent.TimeUnit;

/**
 * Created by mylonelyplanet on 16/8/4.
 */
public class LeaderLatchDemo {
    public static void main(String[] args) throws Exception{
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .sessionTimeoutMs(2000).connectionTimeoutMs(10000).retryPolicy(retryPolicy)
                .namespace("text").build();
        client.start();

        //选举Leader 启动
        LeaderLatch latch = new LeaderLatch(client,"/path");
        latch.start();
        if(latch.await(3, TimeUnit.SECONDS)){
            System.out.println("started.....");
            Thread.currentThread().sleep(1000000);
            latch.close();
        }else {
            System.out.println("interrupt...");
            latch.close();
        }


        System.out.println("end......");
        client.close();

    }
}
