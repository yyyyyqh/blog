package com.yqh.forum.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostDTO {
    private Long id;
    private String title;
    private String content; // 原始 Markdown 内容
    private String summaryContent; // **新增：用于列表页显示的摘要内容**
    private UserDTO author;
    private CategoryDTO category;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 