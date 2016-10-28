package com.bowen.natie.rpc.basic.registry.zookeeper;

import com.bowen.natie.rpc.basic.entity.ServerInfo;
import com.bowen.natie.rpc.basic.protocol.JsonInstanceSerializer;
import com.bowen.natie.rpc.basic.util.IPUtils;
import com.google.common.collect.Maps;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by mylonelyplanet on 16/7/24.
 */
public final class DiscoverAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoverAgent.class);

    //local cache for service discover
    private PathChildrenCache cache = null;
    private final ConcurrentMap<String, ServerInfo> serverList = Maps.newConcurrentMap();

    public static String ZK_HOST_PORT = "127.0.0.1:2181";
    private static final String basePath = "/registry";
    private static String appName = "default.domain";
    private static volatile DiscoverAgent instance;

    private int connectionTimeout = 10000;
    private int sessionTimeout = 30000;

    private CuratorFramework curatorClient;
    private JsonInstanceSerializer serializer;

    static {
        String registry = System.getProperty("registry.address");
        if(registry != null){
            ZK_HOST_PORT = registry;
        }
    }

    private DiscoverAgent() throws Exception {
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
            String path = pathForGroup(appName);
            this.cache = new PathChildrenCache(curatorClient,path,true);
            cache.start();
            addListener(cache);

        }catch (Exception e) {
            LOGGER.error("[connect zookeeper server fail]", e);
            throw new Exception(e.toString(), e);
        }
    }

    /*get singleton*/
    public static DiscoverAgent getInstance() throws Exception {
        if(instance == null){
            synchronized (DiscoverAgent.class){
                if(instance == null){
                    instance = new DiscoverAgent();
                }
            }
        }
        return instance;
    }

    public void printServers(){
        if(serverList.isEmpty()){
            LOGGER.info("there is no available server on service :)");
        }else{
            serverList.keySet().stream().forEach(System.out::print);
        }
    }

    /*
    * add strategy control later
     */
    public String discover(){
        if(serverList.isEmpty()){
            LOGGER.info("there is no available server on service :)");
            return null;
        }else{
            int size = serverList.size();
            serverList.keySet().stream().forEach(System.out::print);
            return (String)serverList.keySet().toArray()[ThreadLocalRandom.current().nextInt(size)];
        }
    }

    public void registService(int port, boolean isSSL) throws Exception{

        long registrationTimeUTC = System.currentTimeMillis();
        ServerInfo info = new ServerInfo(IPUtils.getHostName(), IPUtils.localIp4Str(),port,registrationTimeUTC);

        LOGGER.info("register Server Info : {}",info );

        byte[] bytes = serializer.serialize(info);
        String path = pathForInstance(appName, info.getUniqueID());

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

    private void addListener(PathChildrenCache cache)
    {

        PathChildrenCacheListener listener = new PathChildrenCacheListener()
        {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception
            {
                switch ( event.getType() )
                {
                    case CHILD_ADDED:
                    {
                        ServerInfo config = queryForInstance(event.getData().getPath());
                        LOGGER.info("Node added: {}", config.getUniqueID() );
                        serverList.put(config.getUniqueID(), config);
                        break;
                    }

                    case CHILD_UPDATED:
                    {
                        ServerInfo config = queryForInstance(event.getData().getPath());
                        LOGGER.info("Node changed: {}", config.getUniqueID());
                        serverList.replace(config.getUniqueID(), config);
                        break;
                    }

                    case CHILD_REMOVED:
                    {
                        String node = ZKPaths.getNodeFromPath(event.getData().getPath());
                        LOGGER.info("Node removed: {}" , node);
                        serverList.remove(node);
                        break;
                    }
                }
            }
        };
        cache.getListenable().addListener(listener);
    }

    private ServerInfo queryForInstance(String path) throws Exception {
        try
        {
            byte[] bytes = curatorClient.getData().forPath(path);
            return serializer.deserialize(bytes);
        }
        catch ( KeeperException.NoNodeException ignore )
        {
            // ignore
        }
        return null;
    }

    private String pathForInstance(String group, String instance) { return ZKPaths.makePath(pathForGroup(group), instance); }
    private String pathForGroup(String group) { return ZKPaths.makePath(basePath, group);}


    /*get registerClient*/
    private CuratorFramework getCuratorClient() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        LOGGER.info("ZK: {}",ZK_HOST_PORT);
        CuratorFramework theCuratorClient = CuratorFrameworkFactory.newClient(ZK_HOST_PORT,sessionTimeout,connectionTimeout,retryPolicy);
        theCuratorClient.start();
        theCuratorClient.getZookeeperClient().blockUntilConnectedOrTimedOut();
        if (!theCuratorClient.getZookeeperClient().isConnected()) {
            throw new Exception("can't connect to zk!");
        }
        return theCuratorClient;
    }


    public void shutdown() throws Exception {}
    private static void clearInstance(){
        DiscoverAgent.instance = null;
    }
}
