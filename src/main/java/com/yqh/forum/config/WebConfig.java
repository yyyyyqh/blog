package com.yqh.forum.config; // 放在您的配置包下

import org.springframework.beans.factory.annotation.Value; // 导入 Value
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration // 标记这是一个配置类
public class WebConfig implements WebMvcConfigurer {

    // **注入配置文件中定义的上传目录路径**
    @Value("${app.avatar-upload-dir}")
    private String avatarUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // **配置资源处理器，将 Web URL 路径映射到外部文件目录**

        // 1. 配置 Web URL 路径模式："/uploads/avatars/**" 表示所有以 /uploads/avatars/ 开头的请求
        //    这与您在步骤 2 中保存到数据库的 webAccessiblePath 的开头相匹配。
        // 2. 配置资源实际位置： "file:" + avatarUploadDir + "/"
        //    - "file:" 前缀指示这是一个文件系统路径。
        //    - `avatarUploadDir` 是您在 application.properties 中配置的外部目录路径。
        //    - **末尾的 "/" 很重要**，它指示这是一个目录。

        registry.addResourceHandler("/uploads/avatars/**") // Web URL 路径模式
                .addResourceLocations("file:" + avatarUploadDir + "/"); // 资源实际存放的文件系统目录


        // 如果您在 src/main/resources/static 等默认位置还有其他静态资源，
        // 并且您的安全配置 (如 permitAll) 包含了这些路径，
        // Spring Boot 通常会自动为它们配置处理器。如果您覆盖了默认配置，可能需要手动添加回来。
        /*
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", "classpath:/public/", "classpath:/META-INF/resources/");
        */
    }

    // ... 可能还有其他 WebMvcConfigurer 的方法实现 (如 addViewControllers 等)
}