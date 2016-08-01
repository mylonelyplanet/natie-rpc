package com.bowen.natie.rpc.example.web.controller;


import com.bowen.natie.rpc.example.web.service.GreetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by mylonelyplanet on 16/7/27.
 */

@RestController
public class ApiController {

    @Autowired
    private GreetingService greetingService;

    @RequestMapping("/hello")
    public String hello(){
        System.out.println("hello");
        return greetingService.sayhello();
    }
}
