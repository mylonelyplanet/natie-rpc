package com.bowen.natie.rpc.example.web.service;

import com.bowen.natie.rpc.basic.dto.EnvRequest;
import com.bowen.natie.rpc.example.api.Greeting;
import com.bowen.natie.rpc.proxy.RpcProxy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by mylonelyplanet on 16/7/27.
 */

@Service
public class GreetingService {

    @Resource(name="rpcProxy")
    private RpcProxy rpcProxy;

    public String sayhello(){
        Greeting serviceHelper = rpcProxy.create(Greeting.class);
        EnvRequest env = new EnvRequest();
        return serviceHelper.hello("baby", env);
    }
}

