package com.bowen.natie.rpc.basic.protocol;

import com.bowen.natie.rpc.basic.entity.ServerInfo;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;

import java.io.ByteArrayOutputStream;

/**
 * Created by bowen.jin on 2016-10-25.
 * A serializer that uses Jackson to serialize/deserialize as JSON.
 * the instance payload must support Jackson.
 */
public class JsonInstanceSerializer  {
    private final ObjectMapper  mapper;
    private final JavaType      type;

    public JsonInstanceSerializer(){
        mapper = new ObjectMapper();
        type = mapper.getTypeFactory().constructType(ServerInfo.class);
    }

    @SuppressWarnings({"unchecked"})
    public ServerInfo deserialize(byte[] bytes) throws Exception {
        ServerInfo rawServerInfo = mapper.readValue(bytes,type);
        return  rawServerInfo;
    }

    public byte[] serialize(ServerInfo instance) throws Exception {
        ByteArrayOutputStream   out = new ByteArrayOutputStream();
        mapper.writeValue(out,instance);
        return out.toByteArray();
    }
}
