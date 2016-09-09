package com.bowen.natie.rpc.basic.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mylonelyplanet on 16/7/24.
 */
public final class ZKAgent implements ServiceRegistry{

    private static final Logger LOGGER = LoggerFactory.getLogger(ZKAgent.class);

    public static String ZK_HOST_PORT = "127.0.0.1:2181";

    private static volatile ZKAgent instance;
    private static AtomicBoolean shutdownFlag = new AtomicBoolean(false);

    private int connectionTimeout = 10000;// 连接超时时间
    private int sessionTimeout = 10000; // 会话超时时间

    private CuratorFramework curatorClient;

    static {
        String result = System.getProperty("registry.address");
        if(result != null){
            ZK_HOST_PORT = result;
        }
    }

    private ZKAgent() throws Exception {
        try {
            this.curatorClient = getCuratorClient();
            curatorClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                @Override
                public void stateChanged(CuratorFramework curatorFramework, ConnectionState newState) {
                    if ((newState == ConnectionState.LOST) || (newState == ConnectionState.SUSPENDED)) {

                    } else if (newState == ConnectionState.RECONNECTED) {

                    } else if (newState == ConnectionState.CONNECTED) {

                    }
                }
            });
        }catch (Exception e) {
            LOGGER.error("[connect zookeeper server fail]", e);
            throw new Exception(e.toString(), e);
        }
    }

    /*获取单例*/
    public ZKAgent getInstance() throws Exception {
        if(instance == null){
            synchronized (instance){
                if(instance == null){
                    instance = new ZKAgent();
                }
            }
        }
        return instance;
    }

    @Override
    public void registService(int port, boolean isSSL) {

    }

    @Override
    public void shutdown() throws Exception {

    }

    public static boolean isShutdown() {
        return shutdownFlag.get();
    }

   private static void clearInstance(){
       ZKAgent.instance = null;
   }

    /*获取zk客户端*/
    private CuratorFramework getCuratorClient() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        CuratorFramework theCuratorClient = CuratorFrameworkFactory.newClient(ZK_HOST_PORT,sessionTimeout,connectionTimeout,retryPolicy);
        theCuratorClient.getZookeeperClient().blockUntilConnectedOrTimedOut();
        if (!theCuratorClient.getZookeeperClient().isConnected()) {
            throw new Exception("无法获取ZK连接");
        }
        return theCuratorClient;
    }
}
