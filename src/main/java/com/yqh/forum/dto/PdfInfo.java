package com.yqh.forum.dto;

import lombok.Data;

@Data
public class PdfInfo {
    private String title; // PDF 的标题 (显示在卡片上)
    private String url;   // PDF 文件的 Web 访问 URL (例如 /documents/前端路线图.pdf)

    public PdfInfo(String title, String url) {
        this.title = title;
        this.url = url;
    }
}