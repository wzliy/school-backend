package com.zlwang.school.modules.portal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PortalImmediateConsistencyIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void adminChangesAreVisibleOnTheNextPortalRequestWithoutStaleCache() throws Exception {
        String token = login();
        String originalTitle = portalSiteConfig().path("configs").path("defaultSeoTitle").stringValue();
        String suffix = Long.toString(System.nanoTime());
        long columnId = 0;
        long bannerId = 0;
        long friendLinkId = 0;
        try {
            updateSiteTitle(token, "即时站点标题 " + suffix);
            assertThat(portalSiteConfig().path("configs").path("defaultSeoTitle").stringValue())
                .isEqualTo("即时站点标题 " + suffix);

            columnId = create(token, "/api/admin/columns", columnBody(suffix));
            bannerId = create(token, "/api/admin/banners", bannerBody(suffix));
            friendLinkId = create(token, "/api/admin/friend-links", friendLinkBody(suffix));

            assertThat(containsId(portal("/api/portal/navigation", "siteType", "MAIN_SITE"), columnId))
                .isTrue();
            assertThat(containsId(portal(
                "/api/portal/banners",
                "siteType", "MAIN_SITE",
                "position", "HOME"
            ), bannerId)).isTrue();
            assertThat(containsId(portal(
                "/api/portal/friend-links",
                "siteType", "MAIN_SITE"
            ), friendLinkId)).isTrue();

            updateStatus(token, "/api/admin/columns", columnId, false);
            updateStatus(token, "/api/admin/banners", bannerId, false);
            updateStatus(token, "/api/admin/friend-links", friendLinkId, false);

            assertThat(containsId(portal("/api/portal/navigation", "siteType", "MAIN_SITE"), columnId))
                .isFalse();
            assertThat(containsId(portal(
                "/api/portal/banners",
                "siteType", "MAIN_SITE",
                "position", "HOME"
            ), bannerId)).isFalse();
            assertThat(containsId(portal(
                "/api/portal/friend-links",
                "siteType", "MAIN_SITE"
            ), friendLinkId)).isFalse();
        } finally {
            updateSiteTitle(token, originalTitle);
            deleteResource(token, "/api/admin/friend-links", friendLinkId);
            deleteResource(token, "/api/admin/banners", bannerId);
            deleteResource(token, "/api/admin/columns", columnId);
        }
    }

    private Map<String, Object> columnBody(String suffix) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("parentId", 0);
        body.put("siteType", "MAIN_SITE");
        body.put("columnName", "即时导航 " + suffix);
        body.put("columnCode", "immediate-nav-" + suffix);
        body.put("columnType", "LINK");
        body.put("externalUrl", "https://www.example.edu.cn/immediate");
        body.put("templateConfig", Map.of());
        body.put("sortNo", 900);
        body.put("navVisible", true);
        body.put("enabled", true);
        return body;
    }

    private Map<String, Object> bannerBody(String suffix) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("siteType", "MAIN_SITE");
        body.put("position", "HOME");
        body.put("title", "即时 Banner " + suffix);
        body.put("imageUrl", "/uploads/immediate-banner.jpg");
        body.put("linkType", "NONE");
        body.put("linkTarget", "_self");
        body.put("sortNo", 900);
        body.put("enabled", true);
        return body;
    }

    private Map<String, Object> friendLinkBody(String suffix) {
        return Map.of(
            "siteType", "MAIN_SITE",
            "name", "即时友情链接 " + suffix,
            "linkUrl", "https://www.example.edu.cn/friend",
            "sortNo", 900,
            "enabled", true
        );
    }

    private long create(String token, String path, Map<String, Object> body) throws Exception {
        MvcResult result = mockMvc.perform(post(path)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray())
            .path("data")
            .longValue();
    }

    private void updateStatus(String token, String path, long id, boolean enabled) throws Exception {
        mockMvc.perform(put(path + "/{id}/status", id)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("enabled", enabled))))
            .andExpect(status().isOk());
    }

    private void updateSiteTitle(String token, String value) throws Exception {
        mockMvc.perform(put("/api/admin/site-config/MAIN_SITE")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "items", new Object[] {Map.of(
                        "configKey", "defaultSeoTitle",
                        "configValue", value
                    )}
                ))))
            .andExpect(status().isOk());
    }

    private JsonNode portalSiteConfig() throws Exception {
        return portal("/api/portal/site-config", "siteType", "MAIN_SITE");
    }

    private JsonNode portal(String path, String... parameters) throws Exception {
        MockHttpServletRequestBuilder request = get(path);
        for (int index = 0; index < parameters.length; index += 2) {
            request.param(parameters[index], parameters[index + 1]);
        }
        byte[] body = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
        return objectMapper.readTree(body).path("data");
    }

    private boolean containsId(JsonNode nodes, long id) {
        for (JsonNode node : nodes) {
            if (node.path("id").longValue() == id || containsId(node.path("children"), id)) {
                return true;
            }
        }
        return false;
    }

    private void deleteResource(String token, String path, long id) throws Exception {
        if (id > 0) {
            mockMvc.perform(delete(path + "/{id}", id).headers(auth(token)));
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
