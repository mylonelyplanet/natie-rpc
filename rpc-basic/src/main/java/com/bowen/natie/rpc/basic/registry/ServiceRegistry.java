package com.bowen.natie.rpc.basic.registry;

/**
 * Created by mylonelyplanet on 16/7/24.
 */
public interface ServiceRegistry {

    public void registService(int port, boolean isSSL);

    public void shutdown() throws Exception;

}
