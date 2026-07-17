package com.zlwang.school.modules.content;

import static org.assertj.core.api.Assertions.assertThat;

import com.zlwang.school.modules.content.service.RichTextSanitizer;
import org.junit.jupiter.api.Test;

class RichTextSanitizerTests {

    private final RichTextSanitizer sanitizer = new RichTextSanitizer();

    @Test
    void removesExecutableMarkupAndKeepsRelativeUploadImages() {
        String cleaned = sanitizer.sanitize("""
            <p onclick="alert(1)">正文</p>
            <img src="/uploads/news.jpg" onerror="alert(1)" alt="新闻图">
            <script>alert('xss')</script>
            """);

        assertThat(cleaned)
            .contains("<p>正文</p>")
            .contains("src=\"/uploads/news.jpg\"")
            .contains("alt=\"新闻图\"")
            .doesNotContain("onclick", "onerror", "script", "alert");
    }
}
