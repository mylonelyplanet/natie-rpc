package com.bowen.natie.rpc.basic.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.annotate.JsonRootName;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by mylonelyplanet on 16/7/24.
 * POJO that represents a service instance
 * ip：port 作为一个实例的唯一标识
 */
@JsonRootName("ServerInfo")
public class ServerInfo {

    public static final int DEFAULT_WEIGHT = 100;

    private final String   name;
    private final String   uniqueID;
    private final String   address;
    private final int  port;
    private final long    registrationTimeUTC;

    private String label;// 服务器标签（用于路由规则设定）
    private int weight = DEFAULT_WEIGHT; // 权重

    public ServerInfo(String name,  String address,int port, long registrationTimeUTC){
        this.name = Preconditions.checkNotNull(name,"name can't be null");
        this.address = Preconditions.checkNotNull(address,"address can't be null");;
        this.port = port;
        this.registrationTimeUTC = registrationTimeUTC;
        this.uniqueID = address.concat(":").concat(Integer.toString(port));
    }

    //only for de-serialization
    public ServerInfo(){this("", "",  0,0 );}

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public long getRegistrationTimeUTC() {
        return registrationTimeUTC;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", registrationTimeUTC=" + registrationTimeUTC +
                ", label='" + label + '\'' +
                ", weight=" + weight +
                '}';
    }
}
