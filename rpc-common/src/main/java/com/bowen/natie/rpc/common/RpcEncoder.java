package com.bowen.natie.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by mylonelyplanet on 16/7/9.
 */
public class RpcEncoder extends MessageToByteEncoder<Object>{
    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass){ this.genericClass = genericClass;}

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        if(genericClass.isInstance(in)){
            byte[] data = SerializationUtil.serialize(in);
            out.writeInt(data.length);
            out.writeBytes(data);
            System.out.println("encode: " + in.toString());
        }
    }
}
