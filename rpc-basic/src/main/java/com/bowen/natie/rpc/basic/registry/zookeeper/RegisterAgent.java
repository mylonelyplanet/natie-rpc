package com.bowen.natie.rpc.basic.registry.zookeeper;

import com.bowen.natie.rpc.basic.entity.ServerInfo;
import com.bowen.natie.rpc.basic.protocol.JsonInstanceSerializer;
import com.bowen.natie.rpc.basic.util.IPUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mylonelyplanet on 16/7/24.
 */
public final class RegisterAgent  {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterAgent.class);

    public static String ZK_HOST_PORT = "127.0.0.1:2181";
    private static final String basePath = "RPC";
    private static String appName = "default.domain";
    private static volatile RegisterAgent instance;
    private static AtomicBoolean shutdownFlag = new AtomicBoolean(false);

    private int connectionTimeout = 10000;//
    private int sessionTimeout = 10000; //

    private CuratorFramework curatorClient;
    private JsonInstanceSerializer serializer;

    static {
        String registry = System.getProperty("registry.address");
        if(registry != null){
            ZK_HOST_PORT = registry;
        }
        String domain = System.getProperty("app.name");
        if(domain != null){
            appName = domain;
        }
    }

    private RegisterAgent() throws Exception {
        try {
            this.serializer = new JsonInstanceSerializer();
            this.curatorClient = getCuratorClient();
            curatorClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
                @Override
                public void stateChanged(CuratorFramework curatorFramework, ConnectionState newState) {
                    if ((newState == ConnectionState.LOST) || (newState == ConnectionState.SUSPENDED)) {
                        LOGGER.info("ZK connection lost.");
                    } else if (newState == ConnectionState.RECONNECTED) {
                        LOGGER.info("ZK connection reconnected");
                    } else if (newState == ConnectionState.CONNECTED) {
                        LOGGER.info("ZK connected");
                    }
                }
            });
        }catch (Exception e) {
            LOGGER.error("[connect zookeeper server fail]", e);
            throw new Exception(e.toString(), e);
        }
    }

    /*get singleton*/
    public static RegisterAgent getInstance() throws Exception {
        if(instance == null){
            synchronized (RegisterAgent.class){
                if(instance == null){
                    instance = new RegisterAgent();
                }
            }
        }
        return instance;
    }


    public void registService(int port, boolean isSSL) throws Exception{

        long registrationTimeUTC = System.currentTimeMillis();
        ServerInfo info = new ServerInfo(IPUtils.getHostName(), IPUtils.localIp4Str(),port,registrationTimeUTC);

        LOGGER.info("register Server Info : {}",info );

        byte[] bytes = serializer.serialize(info);
        String path = pathForGroup(appName);

        final int MAX_TRIES = 2;
        boolean isDone = false;
        for ( int i = 0; !isDone && (i < MAX_TRIES); ++i )
        {
            try
            {
                CreateMode mode = CreateMode.EPHEMERAL;
                curatorClient.create().creatingParentsIfNeeded().withMode(mode).forPath(path, bytes);
                isDone = true;
            }
            catch ( KeeperException.NodeExistsException e )
            {
                curatorClient.delete().forPath(path);  // must delete then re-create so that watchers fire
            }
        }
    }


    public void shutdown() throws Exception {

    }

    public static boolean isShutdown() {
        return shutdownFlag.get();
    }

    private static void clearInstance(){
       RegisterAgent.instance = null;
    }

    private String pathForGroup(String group)
    {
        return ZKPaths.makePath(basePath, group);
    }
    /*get registerClient*/
    private CuratorFramework getCuratorClient() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);

        CuratorFramework theCuratorClient = CuratorFrameworkFactory.newClient(ZK_HOST_PORT,sessionTimeout,connectionTimeout,retryPolicy);
        theCuratorClient.getZookeeperClient().blockUntilConnectedOrTimedOut();
        if (!theCuratorClient.getZookeeperClient().isConnected()) {
            throw new Exception("can't connect to zk!");
        }
        return theCuratorClient;
    }

}
