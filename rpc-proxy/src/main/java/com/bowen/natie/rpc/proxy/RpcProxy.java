package com.bowen.natie.rpc.proxy;

import com.bowen.natie.rpc.basic.dto.RpcRequest;
import com.bowen.natie.rpc.basic.dto.EnvRequest;
import com.bowen.natie.rpc.basic.dto.RpcResponse;
import com.bowen.natie.rpc.basic.entity.ServerInfo;
import com.bowen.natie.rpc.basic.registry.zookeeper.DiscoverAgent;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.Proxy;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by mylonelyplanet on 16/7/10.
 */
public class RpcProxy {

    private DiscoverAgent serviceDiscovery;
    private ServerInfo serverInfo;

    public RpcProxy( DiscoverAgent serviceDiscovery){
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method method, Object[] args) throws Throwable {

                        if(serviceDiscovery != null){
                            serverInfo = serviceDiscovery.discover(); // 发现服务
                        }else {
                            throw new IllegalStateException("Discover Agent is not working.");
                        }

                        /*get environment info*/
                        RpcRequest request = new RpcRequest();
                        EnvRequest env = new EnvRequest();
                        env.setHost(serverInfo.getAddress());
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

                        //System.out.println("invoke method to host: " + host + " | port: " + port +" | " + request.toString());

                        Channel channel = null;
                        channel = ConnectionPool.getClientInstance().getChannelByServerInfo(serverInfo);
                        if(channel == null){
                            ServerInfo nextServerInfo = serviceDiscovery.discover();
                            channel =  ConnectionPool.getClientInstance().getChannelByServerInfo(nextServerInfo);
                        }

                        //TODO 这里还有问题，这个handler并没有被处理，必须修改
                        ConnectionPoolClientHandler handler = new ConnectionPoolClientHandler();

                        RpcResponse response = handler.send(channel,request);
                        //System.out.println("invoke finish: " + response);
                        if(response.isError()){
                            throw response.getError();
                        }else {
                            return response.getResult();
                        }
                    }
                });
    }

}
