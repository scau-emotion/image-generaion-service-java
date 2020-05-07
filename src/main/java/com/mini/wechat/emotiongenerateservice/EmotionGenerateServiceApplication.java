package com.mini.wechat.emotiongenerateservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EmotionGenerateServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmotionGenerateServiceApplication.class, args);
    }

}
