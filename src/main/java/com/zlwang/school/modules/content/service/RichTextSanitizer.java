package com.zlwang.school.modules.content.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RichTextSanitizer {

    private static final String SANITIZER_BASE_URI = "https://cms.local";

    private static final Safelist SAFELIST = Safelist.relaxed()
        .addTags("figure", "figcaption")
        .addAttributes("a", "target", "rel")
        .addAttributes("img", "alt", "width", "height", "loading")
        .addProtocols("a", "href", "http", "https", "mailto")
        .addProtocols("img", "src", "http", "https")
        .preserveRelativeLinks(true);

    public String sanitize(String html) {
        if (!StringUtils.hasText(html)) {
            return null;
        }
        Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
        String cleaned = Jsoup.clean(html.trim(), SANITIZER_BASE_URI, SAFELIST, outputSettings);
        return StringUtils.hasText(Jsoup.parseBodyFragment(cleaned).text()) || cleaned.contains("<img")
            ? cleaned
            : null;
    }
}
