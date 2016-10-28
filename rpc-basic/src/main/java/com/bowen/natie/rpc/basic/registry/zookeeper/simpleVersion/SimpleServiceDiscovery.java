package com.bowen.natie.rpc.basic.registry.zookeeper.simpleVersion;

import com.bowen.natie.rpc.basic.constant.Constant;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by mylonelyplanet on 16/7/10.
 * only for demo
 */

public class SimpleServiceDiscovery {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<String>();

    private static String registryAddress = "127.0.0.1:2181";

    static {
        String registry = System.getProperty("registry.address");
        if(registry != null){
            registryAddress = registry;
        }
    }

    public SimpleServiceDiscovery(){

        ZooKeeper zk = connectServer();
        if(zk != null){
            watchNode(zk);
        }
    }

    public String discover(){
        String data = null;
        int size = dataList.size();
        if(size > 0){
            if(size == 1){
                data = dataList.get(0);
                LOGGER.info("using only data: {}", data);
            }else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.info("using random data: {}", data);
            }
        }
        return data;
    }
    private ZooKeeper connectServer(){
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if( event.getState() == Event.KeeperState.SyncConnected){
                        latch.countDown();
                    }
                }
            });
            latch.await();
        }catch (IOException | InterruptedException e){
            LOGGER.error("ZK Connection Error",e);
        }
        return zk;
    }

    private void watchNode(final ZooKeeper zk){
        try{
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged){
                        watchNode(zk);
                    }
                }
            });
            List<String> dataList = new ArrayList<>();
            for(String node: nodeList){
                byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/" +node,false,null);
                dataList.add(new String(bytes));
            }
            LOGGER.info("node data: {}", dataList);
            this.dataList = dataList;
        }catch (KeeperException | InterruptedException e){
            LOGGER.error("watchNode error:", e);
        }
    }

}
