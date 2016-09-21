package com.bowen.natie.rpc.basic.registry.zookeeper;

import com.bowen.natie.rpc.basic.constant.Constant;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by mylonelyplanet on 16/7/10.
 * using zookeeper own client
 * example, not used.
 */
public class ServiceRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private static String registryAddress = "127.0.0.1:2181";

    public ServiceRegistry(String registryAddress){ this.registryAddress = registryAddress;}

    public void registry(String data){

        if(data != null){
            ZooKeeper zk = connectServer();
            if(zk != null){
                createNode(zk,data);
            }
        }
    }

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try{
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT,
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent watchedEvent) {
                            if(watchedEvent.getState() == Event.KeeperState.SyncConnected){
                                latch.countDown();
                            }
                        }
                    });
            latch.await();
        }catch (IOException | InterruptedException e){
            LOGGER.error("Server Connect ZK Fail",e);
        }
        return zk;
    }

    private void createNode(ZooKeeper zk, String data){
        try{
            byte[] bytes = data.getBytes();
            String path = zk.create(Constant.ZK_DATA_PATH,bytes,
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.info("create zookeeper node {} => {}", path, data);
        }catch (KeeperException | InterruptedException e){
            LOGGER.error("create zk node fail", e);
        }
    }
}
