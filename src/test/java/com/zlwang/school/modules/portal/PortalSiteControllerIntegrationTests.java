package com.zlwang.school.modules.portal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PortalSiteControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicSiteConfigMergesGlobalAndMainSiteValues() throws Exception {
        mockMvc.perform(get("/api/portal/site-config")
                .param("siteType", "MAIN_SITE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("000000"))
            .andExpect(jsonPath("$.data.siteType").value("MAIN_SITE"))
            .andExpect(jsonPath("$.data.configs.siteName").value("高校官网"))
            .andExpect(jsonPath("$.data.configs.defaultSeoTitle").value("高校官网"))
            .andExpect(jsonPath("$.data.configs.defaultSeoDescription").value("高校官网门户网站"));
    }

    @Test
    void publicNavigationOnlyReturnsEnabledVisibleSiteColumns() throws Exception {
        mockMvc.perform(get("/api/portal/navigation")
                .param("siteType", "RECRUIT_SITE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(4))
            .andExpect(jsonPath("$.data[0].id").value(200))
            .andExpect(jsonPath("$.data[0].name").value("招生信息"))
            .andExpect(jsonPath("$.data[0].children").isArray())
            .andExpect(jsonPath("$.data[0].siteType").doesNotExist())
            .andExpect(jsonPath("$.data[0].remark").doesNotExist());
    }

    @Test
    void publicBannersAndFriendLinksUseFilteredResponseFields() throws Exception {
        mockMvc.perform(get("/api/portal/banners")
                .param("siteType", "MAIN_SITE")
                .param("position", "HOME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/api/portal/friend-links")
                .param("siteType", "MAIN_SITE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].name").value("教育部"))
            .andExpect(jsonPath("$.data[0].remark").doesNotExist())
            .andExpect(jsonPath("$.data[0].enabled").doesNotExist());
    }

    @Test
    void publicSiteQueriesValidateRequiredParametersAndPositionCompatibility() throws Exception {
        mockMvc.perform(get("/api/portal/site-config"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("A0400"));

        mockMvc.perform(get("/api/portal/banners")
                .param("siteType", "RECRUIT_SITE")
                .param("position", "HOME"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("Banner 位置与站点不匹配"));
    }

    @Test
    void publicColumnTreeDetailAndEmptyPageUseStableContract() throws Exception {
        mockMvc.perform(get("/api/portal/columns")
                .param("siteType", "MAIN_SITE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(7))
            .andExpect(jsonPath("$.data[0].id").value(100))
            .andExpect(jsonPath("$.data[0].children").isArray());

        mockMvc.perform(get("/api/portal/columns/{id}", 101))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("新闻中心"))
            .andExpect(jsonPath("$.data.siteType").value("MAIN_SITE"))
            .andExpect(jsonPath("$.data.templateKey").value("ARTICLE_LIST"))
            .andExpect(jsonPath("$.data.seo.canonicalPath").value("/news"))
            .andExpect(jsonPath("$.data.enabled").doesNotExist())
            .andExpect(jsonPath("$.data.remark").doesNotExist());

        mockMvc.perform(get("/api/portal/columns/{id}/contents", 101)
                .param("pageNo", "1")
                .param("pageSize", "5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records").isArray())
            .andExpect(jsonPath("$.data.pageNo").value(1))
            .andExpect(jsonPath("$.data.pageSize").value(5));

        mockMvc.perform(get("/api/portal/columns/{id}/contents", 101)
                .param("pageSize", "101"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void publicContentLifecycleFiltersDraftAndOfflineAndReturnsAttachments() throws Exception {
        String token = login();
        long contentId = createContent(token);
        try {
            mockMvc.perform(get("/api/portal/contents/{id}", contentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.msg").value("内容不存在或不可访问：" + contentId));

            mockMvc.perform(put("/api/admin/contents/{id}/publish", contentId)
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk());

            mockMvc.perform(get("/api/portal/columns/{id}/contents", 101)
                    .param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(contentId))
                .andExpect(jsonPath("$.data.records[0].title").value("Portal 公开新闻"));

            mockMvc.perform(get("/api/portal/columns/{id}/contents", 101)
                    .param("pageNo", "2")
                    .param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records.length()").value(0));

            mockMvc.perform(get("/api/portal/contents/{id}", contentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contentHtml").value("<p>公开正文</p>"))
                .andExpect(jsonPath("$.data.attachments.length()").value(1))
                .andExpect(jsonPath("$.data.attachments[0].fileName").value("公开附件.pdf"))
                .andExpect(jsonPath("$.data.attachments[0].mediaId").doesNotExist())
                .andExpect(jsonPath("$.data.seo.canonicalPath").value("/news/" + contentId))
                .andExpect(jsonPath("$.data.status").doesNotExist())
                .andExpect(jsonPath("$.data.createdAt").doesNotExist());

            mockMvc.perform(put("/api/admin/contents/{id}/offline", contentId)
                    .headers(auth(token)))
                .andExpect(status().isOk());

            mockMvc.perform(get("/api/portal/contents/{id}", contentId))
                .andExpect(status().isNotFound());
        } finally {
            mockMvc.perform(delete("/api/admin/contents/{id}", contentId)
                .headers(auth(token)));
        }
    }

    private long createContent(String token) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("columnId", 101);
        body.put("title", "Portal 公开新闻");
        body.put("summary", "公开摘要");
        body.put("contentHtml", "<p>公开正文</p>");
        body.put("topFlag", false);
        body.put("recommendFlag", false);
        body.put("sortNo", 10);
        body.put("extensionData", Map.of());
        body.put("attachments", List.of(Map.of(
            "fileName", "公开附件.pdf",
            "fileUrl", "/uploads/public.pdf",
            "fileSize", 1024,
            "fileType", "DOCUMENT",
            "sortNo", 10
        )));
        MvcResult result = mockMvc.perform(post("/api/admin/contents")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray())
            .path("data")
            .longValue();
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "username", "admin",
                    "password", "Admin@123456"
                ))))
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
}
