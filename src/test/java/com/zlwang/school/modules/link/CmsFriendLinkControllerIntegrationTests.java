package com.zlwang.school.modules.link;

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
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class CmsFriendLinkControllerIntegrationTests {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void completeWorkflowSupportsFiltersUpdateStatusAndSorting() throws Exception {
        String token = login();
        String suffix = Integer.toString(SEQUENCE.incrementAndGet());
        long firstId = -1;
        long secondId = -1;
        try {
            firstId = create(token, body(
                "省教育厅 " + suffix,
                "MAIN_SITE",
                "https://jyt.example.gov.cn/",
                20,
                true
            ));
            secondId = create(token, body(
                "招生考试院 " + suffix,
                "MAIN_SITE",
                "https://zsks.example.gov.cn/",
                10,
                true
            ));

            mockMvc.perform(get("/api/admin/friend-links")
                    .headers(auth(token))
                    .param("keyword", "省教育厅 " + suffix)
                    .param("siteType", "MAIN_SITE")
                    .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(firstId));

            Map<String, Object> updated = body(
                "省教育厅已更新 " + suffix,
                "RECRUIT_SITE",
                "https://jyt.example.gov.cn/recruit",
                30,
                true
            );
            updated.put("logoUrl", "/uploads/logos/jyt.png");
            updated.put("remark", "专题站链接");
            mockMvc.perform(put("/api/admin/friend-links/{id}", firstId)
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(updated)))
                .andExpect(status().isOk());

            mockMvc.perform(put("/api/admin/friend-links/sort")
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("items", List.of(
                        Map.of("id", firstId, "sortNo", 5),
                        Map.of("id", secondId, "sortNo", 15)
                    )))))
                .andExpect(status().isOk());
            mockMvc.perform(put("/api/admin/friend-links/{id}/status", firstId)
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("enabled", false))))
                .andExpect(status().isOk());

            mockMvc.perform(get("/api/admin/friend-links/{id}", firstId).headers(auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("省教育厅已更新 " + suffix))
                .andExpect(jsonPath("$.data.siteType").value("RECRUIT_SITE"))
                .andExpect(jsonPath("$.data.logoUrl").value("/uploads/logos/jyt.png"))
                .andExpect(jsonPath("$.data.sortNo").value(5))
                .andExpect(jsonPath("$.data.enabled").value(false));

            mockMvc.perform(delete("/api/admin/friend-links/{id}", firstId).headers(auth(token)))
                .andExpect(status().isOk());
            mockMvc.perform(get("/api/admin/friend-links/{id}", firstId).headers(auth(token)))
                .andExpect(status().isNotFound());
            firstId = -1;
        } finally {
            deleteLink(token, firstId);
            deleteLink(token, secondId);
        }
    }

    @Test
    void supportsGlobalScopeAndRejectsUnsafeUrls() throws Exception {
        String token = login();
        long id = -1;
        try {
            Map<String, Object> global = body(
                "全国公共服务 " + SEQUENCE.incrementAndGet(),
                "GLOBAL",
                "https://service.example.gov.cn/",
                10,
                true
            );
            global.put("logoUrl", "https://service.example.gov.cn/logo.png");
            id = create(token, global);
            mockMvc.perform(get("/api/admin/friend-links/{id}", id).headers(auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.siteType").value("GLOBAL"));

            Map<String, Object> unsafeLink = body(
                "危险链接",
                "GLOBAL",
                "javascript:alert(1)",
                10,
                true
            );
            assertCreateRejected(token, unsafeLink, "链接地址必须是有效的 HTTP 或 HTTPS 地址");

            Map<String, Object> unsafeLogo = body(
                "危险 Logo",
                "GLOBAL",
                "https://www.example.edu.cn/",
                10,
                true
            );
            unsafeLogo.put("logoUrl", "../secret.png");
            assertCreateRejected(token, unsafeLogo, "Logo 地址必须是有效的 HTTP 或 HTTPS 地址");
        } finally {
            deleteLink(token, id);
        }
    }

    @Test
    void rejectsDuplicateSortItems() throws Exception {
        String token = login();
        long id = create(token, body(
            "排序测试 " + SEQUENCE.incrementAndGet(),
            "MAIN_SITE",
            "https://www.example.edu.cn/",
            10,
            true
        ));
        try {
            mockMvc.perform(put("/api/admin/friend-links/sort")
                    .headers(auth(token))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json(Map.of("items", List.of(
                        Map.of("id", id, "sortNo", 1),
                        Map.of("id", id, "sortNo", 2)
                    )))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.msg").value("排序列表包含重复友情链接"));
        } finally {
            deleteLink(token, id);
        }
    }

    @Test
    void endpointRequiresFriendLinkAuthority() throws Exception {
        mockMvc.perform(get("/api/admin/friend-links"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "cms:content")
    void unrelatedAuthorityCannotReadFriendLinks() throws Exception {
        mockMvc.perform(get("/api/admin/friend-links"))
            .andExpect(status().isForbidden());
    }

    private void assertCreateRejected(String token, Map<String, Object> body, String message) throws Exception {
        mockMvc.perform(post("/api/admin/friend-links")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value(message));
    }

    private long create(String token, Map<String, Object> body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/friend-links")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data").longValue();
    }

    private Map<String, Object> body(
        String name,
        String siteType,
        String linkUrl,
        int sortNo,
        boolean enabled
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("siteType", siteType);
        body.put("name", name);
        body.put("linkUrl", linkUrl);
        body.put("sortNo", sortNo);
        body.put("enabled", enabled);
        return body;
    }

    private void deleteLink(String token, long id) throws Exception {
        if (id > 0) {
            mockMvc.perform(delete("/api/admin/friend-links/{id}", id).headers(auth(token)));
        }
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
