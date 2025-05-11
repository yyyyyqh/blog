package com.yqh.forum.dto; // 您可以将它放在 DTO 包，或者新建一个 model.info 包

import lombok.Data; // 假设您使用了 Lombok

@Data // Lombok 会自动生成 getter, setter, toString 等方法
public class PdfInfo {
    private String title; // PDF 的标题 (显示在卡片上)
    private String url;   // PDF 文件的 Web 访问 URL (例如 /documents/前端路线图.pdf)
    // 如果需要，您可以添加更多字段，例如描述、小图标等

    // 可选：添加一个构造函数方便创建对象
    public PdfInfo(String title, String url) {
        this.title = title;
        this.url = url;
    }

    // Lombok 会处理其他的 getter/setter 等
}