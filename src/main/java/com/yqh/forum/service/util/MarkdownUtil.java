package com.yqh.forum.service.util; // Adjust package name based on your structure

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
// Optional: Import extensions if you added them in pom.xml
// import com.vladsch.flexmark.ext.tables.TablesExtension;
// import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
// import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
// import java.util.Arrays;
// import java.util.List;
// import com.vladsch.flexmark.util.misc.Extension;


public class MarkdownUtil {

    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();

        options.set(HtmlRenderer.SOFT_BREAK, "<br>\n");

        //options.set(HtmlRenderer.GENERATE_HEADER_ID, true);
        //options.set(HtmlRenderer.HEADER_ID_GENERATOR_NO_DUPED_DASHES, true);

        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }

    public static String convertMarkdownToHtml(String markdown) {
        if (markdown == null || markdown.trim().isEmpty()) {
            return "";
        }
        com.vladsch.flexmark.util.ast.Node document = PARSER.parse(markdown);
        return RENDERER.render(document);
    }

    private MarkdownUtil() {
    }
}