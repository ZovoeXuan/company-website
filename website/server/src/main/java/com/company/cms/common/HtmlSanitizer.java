package com.company.cms.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

/**
 * 富文本白名单清洗。后端是唯一可信边界：编辑器过滤可被绕过，
 * 所以入库前必须清洗，前端 v-html 直接渲染清洗后的结果。
 */
@Component
public class HtmlSanitizer {

    private static final int MAX_LENGTH = 60_000;
    private static final String RELATIVE_IMAGE = "/api/public/media/";
    /** 站内相对图片地址用一个占位域名承载，清洗后还原，避免被协议白名单移除。 */
    private static final String PLACEHOLDER = "https://cms-local.invalid";

    private final Safelist safelist = Safelist.relaxed()
            .addTags("figure", "figcaption", "h2", "h3", "h4")
            .addAttributes("img", "src", "alt", "width", "height", "loading")
            .addAttributes("a", "href", "title")
            .addAttributes("td", "colspan", "rowspan")
            .addAttributes("th", "colspan", "rowspan")
            .addProtocols("img", "src", "http", "https")
            .addProtocols("a", "href", "http", "https", "mailto");

    private final Document.OutputSettings outputSettings =
            new Document.OutputSettings().prettyPrint(false);

    public String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        String prepared = html.replaceAll(
                "src\\s*=\\s*[\"']" + RELATIVE_IMAGE, "src=\"" + PLACEHOLDER + RELATIVE_IMAGE);
        String cleaned = Jsoup.clean(prepared, "", safelist, outputSettings)
                .replace(PLACEHOLDER, "");

        // 清洗后仍可能留下没有地址的 img（例如只带 onerror 的图片），
        // 这类标签既无意义又会让读屏软件读到空图片，直接移除。
        Document doc = Jsoup.parseBodyFragment(cleaned);
        doc.outputSettings(outputSettings);
        for (org.jsoup.nodes.Element img : doc.select("img")) {
            String src = img.attr("src").trim().toLowerCase();
            boolean allowed = src.startsWith(RELATIVE_IMAGE)
                    || src.startsWith("http://") || src.startsWith("https://");
            if (!allowed) {
                img.remove();
            }
        }
        String result = doc.body().html();
        return result.length() <= MAX_LENGTH ? result : result.substring(0, MAX_LENGTH);
    }

    /** 纯文本字段：去掉所有标签，只保留文字。 */
    public String text(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return Jsoup.parse(html).text().trim();
    }
}
