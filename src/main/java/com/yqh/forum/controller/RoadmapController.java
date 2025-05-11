package com.yqh.forum.controller; // Adjust package

import com.yqh.forum.dto.PdfInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller // 这是一个 Controller 类
@RequestMapping("/roadmap") // 映射所有以 /roadmap 开头的请求
public class RoadmapController {

    @GetMapping // 处理对 /roadmap 的 GET 请求
    public String showRoadmapPage(Model model) {
        // **创建 PDF 信息列表**
        // 这里先硬编码一些示例数据。未来这些数据可以从数据库、配置文件或外部服务加载。
        List<PdfInfo> pdfList = new ArrayList<>();
        // 假设您的 PDF 文件放在了 src/main/resources/static/documents/ 目录下
        pdfList.add(new PdfInfo("前端技术路线图", "/documents/frontend.pdf")); // 请确保文件名和 URL 匹配您实际的文件
        pdfList.add(new PdfInfo("后端技术路线图", "/documents/frontend.pdf"));
        pdfList.add(new PdfInfo("DevOps 路线图", "/documents/frontend.pdf"));
        pdfList.add(new PdfInfo("AI 工程师路线图", "/documents/frontend.pdf"));
        pdfList.add(new PdfInfo("数据分析师路线图", "/documents/frontend.pdf"));
        // ... 添加更多您的 PDF 信息 ...


        // 将 PDF 列表添加到 Model 中，名称为 "pdfList"
        model.addAttribute("pdfList", pdfList);

        // 您之前添加的 roadmap.sh 链接或 PDF 嵌入也可以保留在模板中，或者在这里选择只展示网格
        // model.addAttribute("onlineRoadmapUrl", "https://roadmap.sh/");


        // 返回对应的 Thymeleaf 模板名称
        return "roadmap/index";
    }
}