package com.bowen.natie.rpc.proxy;

import com.bowen.natie.rpc.basic.entity.ServerInfo;
import com.google.common.collect.Maps;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by mylonelyplanet on 16/8/23.
 * each target has a channel pool
 */
public final class ConnectionPool {

    private static class ConnectionPoolClientHolder {
        static  ConnectionPool instance= new ConnectionPool();
    }

    private Connector connector;
    private ConcurrentMap<String, ChannelPool> connectionsByServerInfo = Maps.newConcurrentMap();

    private ConnectionPool(){
        connector = new Connector();
    }

    public static ConnectionPool getClientInstance() {
        return ConnectionPoolClientHolder.instance;
    }

    public Connector getConnector() {
        return connector;
    }

    public Channel getChannelByServerInfo(ServerInfo serverInfo) throws Exception {
        ChannelPool channelPool = getChannelPool(serverInfo);
        return channelPool.synGetChannel();
    }

    public static ChannelPool getChannelPool(Channel channel) {
        String uniqueInfo = channel.attr(ChannelPool.UNIQUE_INFO_ATTRIBUTEKEY).get();

        ChannelPool pool = getClientInstance().connectionsByServerInfo.get(uniqueInfo);
        if (pool != null) {
            return pool;
        }
        return getClientInstance().connectionsByServerInfo.get(uniqueInfo);
    }

    private ChannelPool getChannelPool(ServerInfo serverInfo) throws Exception {
        String uniqueInfo = serverInfo.getUniqueID();

        ChannelPool channelPool = connectionsByServerInfo.get(uniqueInfo);
        if (channelPool == null) {
            InetSocketAddress serverAddress = new InetSocketAddress(serverInfo.getAddress(), serverInfo.getPort());
            channelPool = new ChannelPool(connector, serverAddress);

            //并发判断
            ChannelPool previousPool = connectionsByServerInfo.putIfAbsent(uniqueInfo, channelPool);
            if (previousPool != null) {
                channelPool = previousPool;
            }
        }
        return channelPool;
    }

    public ConcurrentMap<String, ChannelPool> getAllConnections() {
        return connectionsByServerInfo;
    }

}
