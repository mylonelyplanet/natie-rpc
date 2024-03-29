package com.bowen.natie.rpc.basic.dto;

/**
 * Created by bowen.jin on 2016-7-22.
 */
public class EnvRequest {
    private String host;
    private String sign;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "EnvRequest{" +
                "host='" + host + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
