package com.bowen.natie.rpc.basic.constant;

/**
 * Created by mylonelyplanet on 16/7/9.
 */
public interface Constant {
    int ZK_SESSION_TIMEOUT = 50000;

    String ZK_REGISTRY_PATH = "/registry";
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
}
