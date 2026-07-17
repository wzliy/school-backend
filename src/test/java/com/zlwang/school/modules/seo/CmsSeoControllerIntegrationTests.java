package com.zlwang.school.modules.seo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class CmsSeoControllerIntegrationTests {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void resolvesColumnNaturalTitleAndSiteDefaults() throws Exception {
        String token = login();

        mockMvc.perform(get("/api/admin/seo/columns/104").headers(auth(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.title").value("教育教学"))
            .andExpect(jsonPath("$.data.keywords").value("高校官网,高校,教育"))
            .andExpect(jsonPath("$.data.description").value("高校官网门户网站"))
            .andExpect(jsonPath("$.data.canonicalPath").value("/education"));
    }

    @Test
    void resolvesContentSeoOverridesAndCanonicalPath() throws Exception {
        String token = login();
        long contentId = createContent(token);
        try {
            mockMvc.perform(get("/api/admin/seo/contents/{id}", contentId).headers(auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("自定义 SEO 标题"))
                .andExpect(jsonPath("$.data.keywords").value("关键词一,关键词二"))
                .andExpect(jsonPath("$.data.description").value("自定义 SEO 描述"))
                .andExpect(jsonPath("$.data.canonicalPath").value("/education/" + contentId));
        } finally {
            mockMvc.perform(delete("/api/admin/contents/{id}", contentId).headers(auth(token)));
        }
    }

    @Test
    void endpointRequiresMatchingContentAuthority() throws Exception {
        mockMvc.perform(get("/api/admin/seo/contents/1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "cms:media")
    void unrelatedAuthorityCannotReadContentSeo() throws Exception {
        mockMvc.perform(get("/api/admin/seo/contents/1"))
            .andExpect(status().isForbidden());
    }

    private long createContent(String token) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("columnId", 104L);
        body.put("title", "SEO 测试内容 " + SEQUENCE.incrementAndGet());
        body.put("summary", "SEO 测试摘要");
        body.put("contentHtml", "<p>正文</p>");
        body.put("topFlag", false);
        body.put("recommendFlag", false);
        body.put("sortNo", 10);
        body.put("seoTitle", "自定义 SEO 标题");
        body.put("seoKeywords", "关键词一,关键词二");
        body.put("seoDescription", "自定义 SEO 描述");
        body.put("extensionData", Map.of());
        body.put("attachments", List.of());
        MvcResult result = mockMvc.perform(post("/api/admin/contents")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data").longValue();
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("username", "admin", "password", "Admin@123456"))))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray())
            .path("data")
            .path("accessToken")
            .stringValue();
    }

    private HttpHeaders auth(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
