package com.zlwang.school.modules.banner;

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
class CmsBannerControllerIntegrationTests {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completeExternalBannerWorkflowSupportsFiltersStatusAndSorting() throws Exception {
        String token = login();
        String suffix = Integer.toString(SEQUENCE.incrementAndGet());
        long firstId = -1;
        long secondId = -1;
        try {
            Map<String, Object> first = bannerBody("迎新专题 " + suffix, "EXTERNAL", 20, true);
            first.put("subtitle", "2026 级新生");
            first.put("mobileImageUrl", "/uploads/welcome-mobile.jpg");
            first.put("linkUrl", "https://www.example.edu.cn/welcome");
            first.put("linkTarget", "_blank");
            first.put("startTime", "2026-08-01T08:00:00");
            first.put("endTime", "2026-08-31T18:00:00");
            firstId = createBanner(token, first);
            secondId = createBanner(token, bannerBody("校园开放日 " + suffix, "NONE", 10, true));

            mockMvc.perform(get("/api/admin/banners")
                    .headers(auth(token))
                    .param("keyword", "迎新")
                    .param("siteType", "MAIN_SITE")
                    .param("position", "HOME")
                    .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(firstId))
                .andExpect(jsonPath("$.data.records[0].linkTarget").value("_blank"));

            Map<String, Object> updated = bannerBody("迎新专题已更新 " + suffix, "EXTERNAL", 30, true);
            updated.put("linkUrl", "https://www.example.edu.cn/welcome/2026");
            updated.put("linkTarget", "_self");
            mockMvc.perform(put("/api/admin/banners/{id}", firstId)
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(updated)))
                .andExpect(status().isOk());

            mockMvc.perform(put("/api/admin/banners/sort")
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("items", List.of(
                        Map.of("id", firstId, "sortNo", 5),
                        Map.of("id", secondId, "sortNo", 15)
                    )))))
                .andExpect(status().isOk());

            mockMvc.perform(get("/api/admin/banners")
                    .headers(auth(token))
                    .param("siteType", "MAIN_SITE")
                    .param("position", "HOME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].id").value(firstId));

            mockMvc.perform(put("/api/admin/banners/{id}/status", firstId)
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("enabled", false))))
                .andExpect(status().isOk());
            mockMvc.perform(get("/api/admin/banners/{id}", firstId).headers(auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("迎新专题已更新 " + suffix))
                .andExpect(jsonPath("$.data.enabled").value(false));
        } finally {
            deleteBanner(token, firstId);
            deleteBanner(token, secondId);
        }
    }

    @Test
    void contentReferenceMustBeSameSiteAndPublishedBeforeEnabling() throws Exception {
        String token = login();
        long contentId = createContent(token, "Banner 引用内容 " + SEQUENCE.incrementAndGet());
        long bannerId = -1;
        try {
            Map<String, Object> body = bannerBody("内容跳转", "CONTENT", 10, false);
            body.put("linkRefId", contentId);
            bannerId = createBanner(token, body);

            mockMvc.perform(put("/api/admin/banners/{id}/status", bannerId)
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("enabled", true))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("引用内容未发布，不能启用 Banner"));

            mockMvc.perform(put("/api/admin/contents/{id}/publish", contentId)
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                .andExpect(status().isOk());
            mockMvc.perform(put("/api/admin/banners/{id}/status", bannerId)
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("enabled", true))))
                .andExpect(status().isOk());

            Map<String, Object> crossSite = recruitBannerBody("跨站点内容", "CONTENT", true);
            crossSite.put("linkRefId", contentId);
            mockMvc.perform(post("/api/admin/banners")
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(crossSite)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("引用内容与 Banner 必须属于同一站点"));

            mockMvc.perform(put("/api/admin/contents/{id}/offline", contentId).headers(auth(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("内容被启用 Banner 引用，不能下线"));
            mockMvc.perform(delete("/api/admin/contents/{id}", contentId).headers(auth(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("内容被 Banner 引用，不能删除"));

            mockMvc.perform(put("/api/admin/banners/{id}/status", bannerId)
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("enabled", false))))
                .andExpect(status().isOk());
            mockMvc.perform(put("/api/admin/contents/{id}/offline", contentId).headers(auth(token)))
                .andExpect(status().isOk());
            mockMvc.perform(delete("/api/admin/contents/{id}", contentId).headers(auth(token)))
                .andExpect(status().isConflict());
        } finally {
            deleteBanner(token, bannerId);
            deleteContent(token, contentId);
        }
    }

    @Test
    void columnReferencesProtectStatusAndDeletion() throws Exception {
        String token = login();
        Map<String, Object> body = bannerBody("教育教学入口", "COLUMN", 10, true);
        body.put("position", "COLUMN");
        body.put("linkRefId", 104L);
        long bannerId = createBanner(token, body);
        try {
            mockMvc.perform(put("/api/admin/columns/104/status")
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("enabled", false))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("栏目被启用 Banner 引用，不能停用"));

            mockMvc.perform(put("/api/admin/banners/{id}/status", bannerId)
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("enabled", false))))
                .andExpect(status().isOk());
            mockMvc.perform(delete("/api/admin/columns/104").headers(auth(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("栏目被 Banner 引用，不能删除"));
        } finally {
            deleteBanner(token, bannerId);
        }
    }

    @Test
    void rejectsInvalidPositionLinkAndTimeRange() throws Exception {
        String token = login();

        Map<String, Object> wrongPosition = recruitBannerBody("错误位置", "NONE", true);
        wrongPosition.put("position", "HOME");
        assertCreateRejected(token, wrongPosition, "HOME 位置只能属于主站");

        Map<String, Object> noneWithUrl = bannerBody("错误链接", "NONE", 10, true);
        noneWithUrl.put("linkUrl", "https://www.example.edu.cn");
        assertCreateRejected(token, noneWithUrl, "NONE 跳转不能设置引用 ID 或链接地址");

        Map<String, Object> unsafeUrl = bannerBody("危险链接", "EXTERNAL", 10, true);
        unsafeUrl.put("linkUrl", "javascript:alert(1)");
        assertCreateRejected(token, unsafeUrl, "外部链接必须是有效的 HTTP 或 HTTPS 地址");

        Map<String, Object> invalidTime = bannerBody("错误时间", "NONE", 10, true);
        invalidTime.put("startTime", "2026-08-01T08:00:00");
        invalidTime.put("endTime", "2026-08-01T08:00:00");
        assertCreateRejected(token, invalidTime, "startTime 必须早于 endTime");
    }

    @Test
    void endpointsRequireBannerAuthority() throws Exception {
        mockMvc.perform(get("/api/admin/banners"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "cms:content")
    void unrelatedAuthorityCannotReadBanners() throws Exception {
        mockMvc.perform(get("/api/admin/banners"))
            .andExpect(status().isForbidden());
    }

    private void assertCreateRejected(String token, Map<String, Object> body, String message) throws Exception {
        mockMvc.perform(post("/api/admin/banners")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value(message));
    }

    private long createBanner(String token, Map<String, Object> body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/banners")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data").longValue();
    }

    private long createContent(String token, String title) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("columnId", 104L);
        body.put("title", title);
        body.put("summary", "测试摘要");
        body.put("contentHtml", "<p>测试正文</p>");
        body.put("topFlag", false);
        body.put("recommendFlag", false);
        body.put("sortNo", 10);
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

    private Map<String, Object> bannerBody(String title, String linkType, int sortNo, boolean enabled) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("siteType", "MAIN_SITE");
        body.put("position", "HOME");
        body.put("title", title);
        body.put("imageUrl", "/uploads/banner.jpg");
        body.put("linkType", linkType);
        body.put("linkTarget", "_self");
        body.put("sortNo", sortNo);
        body.put("enabled", enabled);
        return body;
    }

    private Map<String, Object> recruitBannerBody(String title, String linkType, boolean enabled) {
        Map<String, Object> body = bannerBody(title, linkType, 10, enabled);
        body.put("siteType", "RECRUIT_SITE");
        body.put("position", "RECRUIT_HOME");
        return body;
    }

    private void deleteBanner(String token, long id) throws Exception {
        if (id > 0) {
            mockMvc.perform(delete("/api/admin/banners/{id}", id).headers(auth(token)));
        }
    }

    private void deleteContent(String token, long id) throws Exception {
        if (id > 0) {
            mockMvc.perform(delete("/api/admin/contents/{id}", id).headers(auth(token)));
        }
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
