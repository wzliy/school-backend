package com.zlwang.school.modules.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class CmsContentControllerIntegrationTests {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completeContentWorkflowSanitizesHtmlAndReplacesAttachments() throws Exception {
        String token = login();
        String suffix = Integer.toString(SEQUENCE.incrementAndGet());
        long columnId = createPageColumn(token, suffix);
        long contentId = createContent(token, articleBody(
            columnId,
            "校园新闻 " + suffix,
            "<p onclick=\"alert(1)\">正文</p><script>alert('xss')</script>",
            List.of(attachment("原附件.pdf", "/uploads/original.pdf", 20))
        ));

        mockMvc.perform(get("/api/admin/contents/{id}", contentId).headers(auth(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("DRAFT"))
            .andExpect(jsonPath("$.data.siteType").value("MAIN_SITE"))
            .andExpect(jsonPath("$.data.contentHtml").value("<p>正文</p>"))
            .andExpect(jsonPath("$.data.attachments.length()").value(1))
            .andExpect(jsonPath("$.data.attachments[0].fileName").value("原附件.pdf"));

        Map<String, Object> update = articleBody(
            columnId,
            "校园新闻已更新 " + suffix,
            "<h2>更新正文</h2>",
            List.of(
                attachment("第二附件.docx", "/uploads/second.docx", 20),
                attachment("第一附件.pdf", "/uploads/first.pdf", 10)
            )
        );
        update.put("recommendFlag", true);
        mockMvc.perform(put("/api/admin/contents/{id}", contentId)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(update)))
            .andExpect(status().isOk());

        mockMvc.perform(put("/api/admin/contents/{id}/publish", contentId)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk());
        mockMvc.perform(put("/api/admin/contents/{id}/top", contentId)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/admin/contents/{id}/top", contentId)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("topFlag", true))))
            .andExpect(status().isOk());
        mockMvc.perform(put("/api/admin/contents/{id}/recommend", contentId)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("recommendFlag", true))))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/contents")
                .headers(auth(token))
                .param("keyword", "已更新")
                .param("columnId", Long.toString(columnId))
                .param("siteType", "MAIN_SITE")
                .param("status", "PUBLISHED"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.records[0].id").value(contentId))
            .andExpect(jsonPath("$.data.records[0].topFlag").value(true))
            .andExpect(jsonPath("$.data.records[0].recommendFlag").value(true));

        mockMvc.perform(get("/api/admin/contents/{id}", contentId).headers(auth(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.attachments.length()").value(2))
            .andExpect(jsonPath("$.data.attachments[0].fileName").value("第一附件.pdf"))
            .andExpect(jsonPath("$.data.attachments[1].fileName").value("第二附件.docx"));

        mockMvc.perform(delete("/api/admin/columns/{id}", columnId).headers(auth(token)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.msg").value("栏目包含内容，不能删除"));

        mockMvc.perform(put("/api/admin/contents/{id}/offline", contentId).headers(auth(token)))
            .andExpect(status().isOk());
        mockMvc.perform(delete("/api/admin/contents/{id}", contentId).headers(auth(token)))
            .andExpect(status().isOk());
        mockMvc.perform(delete("/api/admin/columns/{id}", columnId).headers(auth(token)))
            .andExpect(status().isOk());
    }

    @Test
    void draftsAllowIncompleteTemplateFieldsButPublishingRequiresThem() throws Exception {
        String token = login();
        long contentId = createContent(token, articleBody(106L, "服务入口", null, List.of()));

        mockMvc.perform(put("/api/admin/contents/{id}/publish", contentId)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("extensionData.targetUrl 不能为空"));

        Map<String, Object> update = articleBody(106L, "服务入口", null, List.of());
        update.put("extensionData", Map.of("targetUrl", "https://service.example.edu.cn"));
        mockMvc.perform(put("/api/admin/contents/{id}", contentId)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(update)))
            .andExpect(status().isOk());
        mockMvc.perform(put("/api/admin/contents/{id}/publish", contentId)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/admin/contents/{id}", contentId).headers(auth(token)))
            .andExpect(status().isOk());
    }

    @Test
    void unknownExtensionsAndUnsupportedColumnsAreRejected() throws Exception {
        String token = login();
        Map<String, Object> unknown = articleBody(100L, "非法扩展字段", "<p>正文</p>", List.of());
        unknown.put("extensionData", Map.of("script", "alert(1)"));
        mockMvc.perform(post("/api/admin/contents")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(unknown)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("extensionData 包含未定义字段：script"));

        String suffix = Integer.toString(SEQUENCE.incrementAndGet());
        long linkColumnId = createLinkColumn(token, suffix);
        mockMvc.perform(post("/api/admin/contents")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(articleBody(linkColumnId, "外链内容", null, List.of()))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("当前栏目不支持内容维护"));
        mockMvc.perform(delete("/api/admin/columns/{id}", linkColumnId).headers(auth(token)))
            .andExpect(status().isOk());
    }

    @Test
    void endpointRequiresContentAuthority() throws Exception {
        mockMvc.perform(get("/api/admin/contents"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "cms:column")
    void unrelatedAuthorityCannotReadContents() throws Exception {
        mockMvc.perform(get("/api/admin/contents"))
            .andExpect(status().isForbidden());
    }

    private long createContent(String token, Map<String, Object> body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/contents")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data").longValue();
    }

    private long createPageColumn(String token, String suffix) throws Exception {
        Map<String, Object> body = columnBody(
            "测试内容栏目" + suffix,
            "content-column-" + suffix,
            "PAGE",
            "/content-column-" + suffix
        );
        body.put("templateKey", "SINGLE_PAGE");
        body.put("templateConfig", Map.of("page", Map.of()));
        return createColumn(token, body);
    }

    private long createLinkColumn(String token, String suffix) throws Exception {
        Map<String, Object> body = columnBody(
            "测试外链栏目" + suffix,
            "link-column-" + suffix,
            "LINK",
            null
        );
        body.put("externalUrl", "https://example.edu.cn");
        return createColumn(token, body);
    }

    private long createColumn(String token, Map<String, Object> body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/columns")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data").longValue();
    }

    private Map<String, Object> articleBody(
        long columnId,
        String title,
        String contentHtml,
        List<Map<String, Object>> attachments
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("columnId", columnId);
        body.put("title", title);
        body.put("summary", "测试摘要");
        body.put("contentHtml", contentHtml);
        body.put("publishAt", null);
        body.put("topFlag", false);
        body.put("recommendFlag", false);
        body.put("sortNo", 10);
        body.put("extensionData", Map.of());
        body.put("attachments", attachments);
        return body;
    }

    private Map<String, Object> attachment(String name, String url, int sortNo) {
        return Map.of(
            "fileName", name,
            "fileUrl", url,
            "fileSize", 1024,
            "fileType", "DOCUMENT",
            "sortNo", sortNo
        );
    }

    private Map<String, Object> columnBody(String name, String code, String type, String route) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("parentId", 0);
        body.put("siteType", "MAIN_SITE");
        body.put("columnName", name);
        body.put("columnCode", code);
        body.put("columnType", type);
        body.put("routePath", route);
        body.put("templateConfig", Map.of());
        body.put("sortNo", 900);
        body.put("navVisible", false);
        body.put("enabled", true);
        return body;
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("username", "admin", "password", "Admin@123456"))))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        return response.path("data").path("accessToken").stringValue();
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
