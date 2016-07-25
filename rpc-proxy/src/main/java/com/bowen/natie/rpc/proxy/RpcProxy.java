package com.bowen.natie.rpc.proxy;

import com.bowen.natie.rpc.basic.dto.RpcRequest;
import com.bowen.natie.rpc.basic.dto.EnvRequest;
import com.bowen.natie.rpc.basic.dto.RpcResponse;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.Proxy;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by mylonelyplanet on 16/7/10.
 */
public class RpcProxy {
    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress){
        this.serverAddress = serverAddress;
    }
    public RpcProxy(ServiceDiscovery serviceDiscovery){ this.serviceDiscovery = serviceDiscovery;}

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method method, Object[] args) throws Throwable {

                        if(serviceDiscovery != null){
                            serverAddress = serviceDiscovery.discover(); // 发现服务
                        }else {
                            throw new IllegalStateException("no zookeeper");
                        }
                        String[] array = serverAddress.split(":");
                        String host = array[0];
                        int port = Integer.parseInt(array[1]);


                        /*get environment info*/
                        RpcRequest request = new RpcRequest();
                        EnvRequest env = new EnvRequest();
                        env.setHost(host);
                        env.setSign(UUID.randomUUID().toString());
                        if(args.length > 0){
                            for(int i =0; i<args.length;i++){
                                if(args[i] instanceof EnvRequest){
                                    args[i] = env;
                                }
                            }
                        }

                        request.setRequestId(env.getSign());
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);

                        System.out.println("invoke method to host: " + host + " | port: " + port +" | " + request.toString());

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
