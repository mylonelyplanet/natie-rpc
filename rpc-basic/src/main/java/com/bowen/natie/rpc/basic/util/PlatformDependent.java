package com.bowen.natie.rpc.basic.util;


import java.util.concurrent.ConcurrentMap;

//import ch.qos.logback.core.util.CachingDateFormatter;
import io.netty.util.internal.InternalThreadLocalMap;
import io.netty.util.internal.LongCounter;
import io.netty.util.internal.ThreadLocalRandom;

/**
 * Created by mylonelyplanet on 16/7/24.
 */
public class PlatformDependent {

    public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap() {
        return io.netty.util.internal.PlatformDependent.newConcurrentHashMap();
    }

    public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap(int initialCapacity) {
        return io.netty.util.internal.PlatformDependent.newConcurrentHashMap(initialCapacity);
    }

    public static <K, V> ConcurrentMap<K, V> newConcurrentHashMap(int initialCapacity, float loadFactor,
                                                                  int concurrencyLevel) {
        return io.netty.util.internal.PlatformDependent.newConcurrentHashMap(initialCapacity, loadFactor,
                concurrencyLevel);
    }

    public static LongCounter newLongCounter() {
        return io.netty.util.internal.PlatformDependent.newLongCounter();
    }

    public static StringBuilder stringBuilder() {
        return InternalThreadLocalMap.get().stringBuilder();
    }

    public static ThreadLocalRandom threadLocalRandom() {
        return InternalThreadLocalMap.get().random();
    }

//    public static CachingDateFormatter dateFormatter(String pattern) {
//        return new CachingDateFormatter(pattern);
//    }

}