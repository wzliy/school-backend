package com.zlwang.school.infrastructure.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;
import tools.jackson.databind.ObjectMapper;

class OperationRequestSanitizerTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OperationRequestSanitizer sanitizer = new OperationRequestSanitizer(objectMapper);

    @Test
    void redactsNestedSecretsAndQueryCredentials() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setParameter("accessToken", "query-secret");
        request.setContent("""
            {
              "username": "editor",
              "password": "plain-secret",
              "profile": {
                "apiToken": "nested-secret",
                "displayName": "编辑员"
              }
            }
            """.getBytes(StandardCharsets.UTF_8));
        ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request, 8_192);
        wrapper.getInputStream().readAllBytes();

        String summary = sanitizer.summarize(wrapper);

        assertThat(summary)
            .contains("\"username\":\"editor\"")
            .contains("\"displayName\":\"编辑员\"")
            .contains("\"password\":\"***\"")
            .contains("\"apiToken\":\"***\"")
            .doesNotContain("plain-secret", "nested-secret", "query-secret");
    }

    @Test
    void omitsRawNonJsonBodies() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.MULTIPART_FORM_DATA_VALUE);
        request.setContent("binary-content".getBytes(StandardCharsets.UTF_8));
        ContentCachingRequestWrapper wrapper = new ContentCachingRequestWrapper(request, 8_192);
        wrapper.getInputStream().readAllBytes();

        assertThat(sanitizer.summarize(wrapper))
            .contains("\"contentType\":\"multipart/form-data\"")
            .doesNotContain("binary-content");
    }
}
