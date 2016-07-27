package com.bowen.natie.rpc.example.api;

import com.bowen.natie.rpc.basic.dto.EnvRequest;

/**
 * Created by bowen.jin on 2016-7-27.
 */
public interface Greeting {

     String hello(String name, EnvRequest env);
}
