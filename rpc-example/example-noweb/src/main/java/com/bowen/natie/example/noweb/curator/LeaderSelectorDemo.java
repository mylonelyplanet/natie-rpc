package com.bowen.natie.example.noweb.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created by mylonelyplanet on 16/8/4.
 */
public class LeaderSelectorDemo {

    public static void main(String[] args) throws Exception{
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .sessionTimeoutMs(5000).connectionTimeoutMs(10000).retryPolicy(retryPolicy)
                .namespace("text").build();
        client.start();

        //选举leader
        final LeaderSelector leaderSelector = new LeaderSelector(client, "/led", new LeaderSelectorListenerAdapter() {
            @Override
            public void takeLeadership(CuratorFramework curatorFramework) throws Exception {
                System.out.println("working.....");
                Thread.currentThread().sleep(6000);
                System.out.println("end...");
            }
        });

        //自动重新排队
        leaderSelector.autoRequeue();
        leaderSelector.start();
        System.in.read();
    }
}
