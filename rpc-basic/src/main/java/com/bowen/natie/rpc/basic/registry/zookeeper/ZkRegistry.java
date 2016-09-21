package com.bowen.natie.rpc.basic.registry.zookeeper;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * Created by mylonelyplanet on 16/9/20.
 * using curator framework to implement service register
 */
public class ZkRegistry {


    /*get registerClient*/
    public static CuratorFramework getCuratorClient(String ZK_HOST_PORT,int sessionTimeout, int connectionTimeout ) throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        CuratorFramework theCuratorClient = CuratorFrameworkFactory.newClient(ZK_HOST_PORT,sessionTimeout,connectionTimeout,retryPolicy);
        theCuratorClient.getZookeeperClient().blockUntilConnectedOrTimedOut();
        if (!theCuratorClient.getZookeeperClient().isConnected()) {
            throw new Exception("can't connect to zk!");
        }
        return theCuratorClient;
    }
}
