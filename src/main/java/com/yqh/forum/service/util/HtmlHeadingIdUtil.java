package com.yqh.forum.service.util; // 请确保包名与您的项目结构一致

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

/**
 * 实现对HTML 标题添加 ID
 */
public class HtmlHeadingIdUtil {

    public static String addIdsToHtmlHeadings(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return "";
        }

        Document doc = Jsoup.parseBodyFragment(htmlContent);
        Elements headings = doc.select("h1, h2, h3, h4, h5, h6");

        Set<String> existingIds = new HashSet<>();
        int headingCounter = 0; // 用于生成唯一ID的全局计数器

        // 第一遍：收集所有已经存在的ID，以避免冲突
        for (Element heading : headings) {
            if (heading.hasAttr("id") && !heading.attr("id").isEmpty()) {
                existingIds.add(heading.attr("id"));
            }
        }

        // 第二遍：为没有ID的标题生成并添加ID
        for (Element heading : headings) {
            if (!heading.hasAttr("id") || heading.attr("id").isEmpty()) {
                String basePrefix = "toc-heading-"; // 可以为所有标题使用统一前缀
                String generatedId;
                do {
                    // 使用更简单的全局计数器生成ID，例如 toc-heading-0, toc-heading-1
                    generatedId = basePrefix + headingCounter++;
                } while (existingIds.contains(generatedId)); // 确保生成的ID是唯一的

                heading.attr("id", generatedId);
                existingIds.add(generatedId); // 将新生成的ID也加入到已使用ID集合中
            }
        }
        return doc.body().html();
    }

    private HtmlHeadingIdUtil() {
        // 私有构造函数，防止实例化工具类
    }
}