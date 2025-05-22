package com.yqh.forum.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 标记这是一个配置类
public class WebConfig implements WebMvcConfigurer {

    // **注入配置文件中定义的上传目录路径**
    @Value("${app.avatar-upload-dir}")
    private String avatarUploadDir;

    // **注入帖子图片上传目录**
    @Value("${app.post-image-upload-dir}")
    private String postImageUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // **头像资源处理器，将 Web URL 路径映射到外部文件目录**
        registry.addResourceHandler("/uploads/avatars/**") // Web URL 路径模式
                .addResourceLocations("file:" + avatarUploadDir + "/"); // 资源实际存放的文件系统目录

        // **新增：帖子图片资源处理器**
        registry.addResourceHandler("/uploads/post-images/**")
                .addResourceLocations("file:" + postImageUploadDir + "/");
    }
}