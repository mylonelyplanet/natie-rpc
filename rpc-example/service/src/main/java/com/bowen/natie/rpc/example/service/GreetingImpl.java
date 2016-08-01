package com.bowen.natie.rpc.example.service;

/**
 * Created by bowen.jin on 2016-7-27.
 */

import com.bowen.natie.rpc.basic.dto.EnvRequest;
import com.bowen.natie.rpc.core.RpcService;
import com.bowen.natie.rpc.example.api.Greeting;

@RpcService(Greeting.class)
public class GreetingImpl implements Greeting {

    @Override
    public String hello(String name, EnvRequest env) {

        StringBuilder sb = new StringBuilder();
        return sb.append("hello").append(name).append(env.getHost()).toString();
    }
}
