package com.yqh.forum.controller; // Adjust package

import com.yqh.forum.dto.PdfInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * 路线图控制器
 */
@Controller
@RequestMapping("/roadmap")
public class RoadmapController {

    /**
     * 显示路线图页面 /roadmap
     * @param model
     * @return "roadmap/index"
     */
    @GetMapping
    public String showRoadmapPage(Model model) {
        // **创建 PDF 信息列表**
        List<PdfInfo> pdfList = new ArrayList<>();
        // src/main/resources/static/documents/ 目录下
        pdfList.add(new PdfInfo("前端技术路线图", "/documents/frontend.pdf"));
        pdfList.add(new PdfInfo("后端技术路线图", "/documents/backend.pdf"));
        pdfList.add(new PdfInfo("DevOps 路线图", "/documents/devops.pdf"));
        pdfList.add(new PdfInfo("AI 工程师路线图", "/documents/ai-engineer.pdf"));
        pdfList.add(new PdfInfo("数据分析师路线图", "/documents/data-analyst.pdf"));


        // 将 PDF 列表添加到 Model 中，名称为 "pdfList"
        model.addAttribute("pdfList", pdfList);

        // 您之前添加的 roadmap.sh 链接或 PDF 嵌入也可以保留在模板中，或者在这里选择只展示网格
        // model.addAttribute("onlineRoadmapUrl", "https://roadmap.sh/");


        // 返回对应的 Thymeleaf 模板名称
        return "roadmap/index";
    }
}