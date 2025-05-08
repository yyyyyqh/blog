package com.yqh.forum.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PostDTO {
    private Long id;
    private String title;
    private String content;
    private UserDTO author;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 