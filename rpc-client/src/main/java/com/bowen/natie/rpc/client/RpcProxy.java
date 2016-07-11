package com.bowen.natie.rpc.client;

import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.Proxy;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by mylonelyplanet on 16/7/10.
 */
public class RpcProxy {
    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress){ this.serverAddress = serverAddress;}
    public RpcProxy(ServiceDiscovery serviceDiscovery){ this.serviceDiscovery = serviceDiscovery;}

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);

                        if(serviceDiscovery != null){
                            serverAddress = serviceDiscovery.discover(); // 发现服务
                        }else {
                            throw new IllegalStateException("no zookeeper");
                        }
                        String[] array = serverAddress.split(":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);

                        System.out.println("invoke method to host: " + host + " | port: " + port);

                        RpcClient client = new RpcClient(host,port);
                        RpcResponse response = client.send(request);
                        System.out.println("invoke finish: " + response);
                        if(response.isError()){
                            throw response.getError();
                        }else {
                            return response.getResult();
                        }
                    }
                });
    }
}
