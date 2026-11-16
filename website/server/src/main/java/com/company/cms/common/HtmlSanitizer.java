package com.company.cms.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

/**
 * 富文本白名单清洗与受控媒体节点归一化。
 * - 支持 <img data-media-id="123"> 受控媒体节点，自动补齐或归一化为 /api/public/media/123
 * - 长度上限统一为 100,000 字符（与开发文档一致），且不在标签中间硬截断
 */
@Component
public class HtmlSanitizer {

    public static final int MAX_LENGTH = 100_000;
    private static final String RELATIVE_IMAGE = "/api/public/media/";
    private static final String ADMIN_IMAGE_PREFIX = "/api/admin/media/";
    private static final String PLACEHOLDER = "https://cms-local.invalid";

    private final Safelist safelist = Safelist.relaxed()
            .addTags("figure", "figcaption", "h2", "h3", "h4")
            .addAttributes("img", "src", "alt", "width", "height", "loading", "data-media-id")
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
        // 将后台预览地址 /api/admin/media/{id}/file 统一还原为公开受控地址 /api/public/media/{id}
        String normalizedAdmin = html.replaceAll(
                "src\\s*=\\s*[\"']/api/admin/media/(\\d+)/file[\"']",
                "data-media-id=\"$1\" src=\"/api/public/media/$1\"");
        String prepared = normalizedAdmin.replaceAll(
                "src\\s*=\\s*[\"']" + RELATIVE_IMAGE, "src=\"" + PLACEHOLDER + RELATIVE_IMAGE);
        String cleaned = Jsoup.clean(prepared, "", safelist, outputSettings)
                .replace(PLACEHOLDER, "");

        Document doc = Jsoup.parseBodyFragment(cleaned);
        doc.outputSettings(outputSettings);
        for (Element img : doc.select("img")) {
            String mediaId = img.attr("data-media-id").trim();
            String src = img.attr("src").trim();
            if (mediaId.matches("^\\d{1,18}$")) {
                src = RELATIVE_IMAGE + mediaId;
                img.attr("src", src);
            } else if (src.startsWith(RELATIVE_IMAGE)) {
                String tail = src.substring(RELATIVE_IMAGE.length()).replaceAll("[^0-9].*$", "");
                if (tail.matches("^\\d{1,18}$")) {
                    img.attr("data-media-id", tail);
                    img.attr("src", RELATIVE_IMAGE + tail);
                }
            }
            String lower = img.attr("src").trim().toLowerCase();
            boolean allowed = lower.startsWith(RELATIVE_IMAGE)
                    || lower.startsWith("http://") || lower.startsWith("https://");
            if (!allowed) {
                img.remove();
            }
        }
        return doc.body().html();
    }

    /** 纯文本字段：去掉所有标签，只保留文字。 */
    public String text(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return Jsoup.parse(html).text().trim();
    }
}
