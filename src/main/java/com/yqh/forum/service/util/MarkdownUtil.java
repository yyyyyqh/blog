package com.yqh.forum.service.util; // Adjust package name based on your structure

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

// Optional: Import extensions if you added them in pom.xml
// import com.vladsch.flexmark.ext.tables.TablesExtension;
// import com.vladsch.flexmark.ext.strikethrough.StrikethroughExtension;
// import java.util.Arrays;

/**
 * Utility class for converting Markdown text to HTML using Flexmark-java.
 */
public class MarkdownUtil {

    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();

        // Configure parser options if needed (e.g., enable extensions)
        // 如果您添加了表格、删除线等扩展依赖，可以在这里启用它们
        // options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));

        // **新增配置：将 Markdown 中的软换行转换为 HTML 的 <br> 标签**
        // Flexmark 默认的 HtmlRenderer.SOFT_BREAK 是 "\n"，这里我们将其改为 "<br>\n"
        options.set(HtmlRenderer.SOFT_BREAK, "<br>\n");

        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }

    /**
     * Converts Markdown text to HTML.
     *
     * @param markdown The Markdown text.
     * @return The HTML representation. Returns an empty string if input is null or blank.
     */
    public static String convertMarkdownToHtml(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }
        // Parse the Markdown text into an AST (Abstract Syntax Tree)
        com.vladsch.flexmark.util.ast.Node document = PARSER.parse(markdown);
        // Render the AST into HTML
        return RENDERER.render(document);
    }

    // Private constructor to prevent instantiation
    private MarkdownUtil() {
    }
}