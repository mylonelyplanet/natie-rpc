package com.bowen.natie.rpc.proxy;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by bowen.jin on 2016-10-28.
 * 真正的连接池
 */
public class ChannelPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelPool.class);

    public static final AttributeKey<String> UNIQUE_INFO_ATTRIBUTEKEY = AttributeKey.valueOf("osp.uniqueInfo");
    public static final AttributeKey<Integer> INDEX_ATTRIBUTE = AttributeKey.valueOf("osp.channel.indexOfPool");
    public static final AttributeKey<AtomicInteger> SEQ_ATTRIBUTEKEY = AttributeKey.valueOf("osp.seq");

    private static final int MAX_CONNECTIONS = Math.max(2, Runtime.getRuntime().availableProcessors() / 4);

    //使用数组，避免使用锁,提升性能
    private Channel[] channels;
    private Object[] locks;
    private Connector connector;
    private InetSocketAddress serverAddress;

    public ChannelPool(Connector ospConnector, InetSocketAddress serverAddress) throws Exception {

        this.channels = new Channel[MAX_CONNECTIONS];
        this.locks = new Object[MAX_CONNECTIONS];
        for (int i = 0; i < MAX_CONNECTIONS; i++) {
            this.locks[i] = new Object();
        }
        this.connector = ospConnector;
        this.serverAddress = serverAddress;

    }

    public Channel synGetChannel() throws Exception {

        //1. get a random channel
        final int pos = ThreadLocalRandom.current().nextInt(0, MAX_CONNECTIONS);
        Channel target = channels[pos];

        //2. return it if get an active channel
        if (target != null && target.isActive()) {
            return target;
        }

        synchronized (locks[pos]) {
            target = channels[pos];
            //2. return it if get an active channel
            if (target != null && target.isActive()) {
                return target;
            }
            //3. connect a new channel
            String host = serverAddress.getAddress().getHostAddress();
            int port = serverAddress.getPort();

            LOGGER.info("connecting to " + host + ":" + port);
            target = connector.syncConnect(host, port);
            LOGGER.info("connected to " + host + ":" + port);

            target.attr(INDEX_ATTRIBUTE).set(pos);
            //target.attr(HEART_BEAT_FAIL).set(new AtomicInteger(0));
            channels[pos] = target;

            return target;
        }
    }

    public boolean close(Channel channel) {
        forceClose(channel);
        return true;
    }

    public void forceClose(Channel channel) {
        int index = channel.attr(INDEX_ATTRIBUTE).get();
        if (index < 0 || index >= MAX_CONNECTIONS) {
            return;
        }
        synchronized (locks[index]) {
            if (channels[index] == channel) {
                channels[index] = null;
            } else {
                LOGGER.error("big error: channel index not match!");
            }
        }
        channel.close();
        LOGGER.info("closed from " + channel.remoteAddress());
    }

    public static int getNextSequence(Channel channel) {
        return channel.attr(ChannelPool.SEQ_ATTRIBUTEKEY).get().incrementAndGet();
    }
}
