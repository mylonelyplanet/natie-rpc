package com.bowen.natie.example.noweb.curator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * Created by mylonelyplanet on 16/8/4.
 */
@Configuration
public class SmsClient {

    @Bean
    public SmsConnector init(){

        try{
            SmsConnector connector = new SmsConnector();

            connector.syncConnect("192.168.100.166", 16908);
            return connector;

        }catch (Exception e){
            System.out.println("Exception");
        }
        return null;
    }

}
