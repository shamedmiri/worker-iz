package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.demo.config.ApiUrlsProperties;

@SpringBootApplication
@EnableConfigurationProperties(ApiUrlsProperties.class)


public class JavaworkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(JavaworkerApplication.class, args);
    }
}
