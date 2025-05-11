package com.yqh.forum.controller; // 示例：可以放在您的 Controller 包下

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity; // 导入 ResponseEntity
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody; // **导入 @ResponseBody**
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap; // 导入 HashMap
import java.util.Map; // 导入 Map
import java.util.UUID; // 导入 UUID

@Controller
@RequestMapping("/upload") // 示例基础路径：/upload
public class FileUploadController {

    // 注入配置文件中定义的帖子图片上传目录
    @Value("${app.post-image-upload-dir}")
    private String postImageUploadDir;

    /**
     * 处理来自 Markdown 编辑器的图片上传请求
     * @param file 上传的图片文件。@RequestParam 的名字 "image" 需要与前端 EasyMDE 配置中发送的文件字段名一致。
     * @return 包含图片 Web 可访问 URL 的 ResponseEntity。EasyMDE 通常期望一个包含 "url" 字段的 JSON 响应。
     */
    @PostMapping("/image") // 示例上传接口 URL: /upload/image
    @ResponseBody // **标记返回值为响应体，不是视图名称**
    public ResponseEntity<?> uploadPostImage(@RequestParam("image") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("请选择要上传的图片");
        }

        try {
            // **确保上传文件的目标目录存在**
            Path uploadPath = Paths.get(postImageUploadDir);
            if (!Files.exists(uploadPath)) {
                // 使用 createDirectories 创建多级目录
                Files.createDirectories(uploadPath);
            }

            // **生成一个唯一的文件名**
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // 示例：使用 UUID 作为文件名主体 + 原文件扩展名
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;

            // **定义文件保存的完整路径**
            Path filePath = uploadPath.resolve(uniqueFilename);

            // **将上传的文件写入磁盘**
            // 使用 StandardCopyOption.REPLACE_EXISTING 选项，如果同名文件已存在则替换
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // **生成文件在 Web 上可访问的 URL**
            // 这个 URL 路径需要与您在步骤 2 中配置的资源处理器相匹配
            String webAccessibleUrl = "/uploads/post-images/" + uniqueFilename; // Web 上访问该文件的路径

            // **构建响应体：EasyMDE 通常期望一个包含 "url" 字段的 JSON 响应**
            // 例如: { "url": "/uploads/post-images/unique-filename.png" }
            Map<String, String> response = new HashMap<>();
            response.put("url", webAccessibleUrl);

            // 返回成功响应和包含 URL 的 JSON
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace(); // 在日志中记录异常
            // 返回服务器内部错误响应
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("图片上传失败：" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // 捕获其他可能出现的异常
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("图片上传失败：" + e.getMessage());
        }
    }
}