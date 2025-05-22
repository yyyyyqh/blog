package com.yqh.forum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // 启用 Spring Cache 功能
public class ForumApplication {
    public static void main(String[] args) {
        SpringApplication.run(ForumApplication.class, args);
    }
} 