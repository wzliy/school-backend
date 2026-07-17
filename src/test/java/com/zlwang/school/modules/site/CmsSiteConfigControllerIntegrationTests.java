package com.zlwang.school.modules.site;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
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
class CmsSiteConfigControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void readsByScopeAndUpdatesKnownValues() throws Exception {
        String token = login();
        String originalTitle = configValue(token, "MAIN_SITE", "defaultSeoTitle");
        String originalLimit = configValue(token, "MAIN_SITE", "homeNewsLimit");
        try {
            update(token, "MAIN_SITE", List.of(
                item("defaultSeoTitle", "  联调主站标题  "),
                item("homeNewsLimit", "12")
            ));

            mockMvc.perform(get("/api/admin/site-config")
                    .headers(auth(token))
                    .param("siteType", "MAIN_SITE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[?(@.configKey == 'defaultSeoTitle')].configValue")
                    .value("联调主站标题"))
                .andExpect(jsonPath("$.data[?(@.configKey == 'homeNewsLimit')].configValue")
                    .value("12"))
                .andExpect(jsonPath("$.data[?(@.configKey == 'homeNewsLimit')].configType")
                    .value("NUMBER"));
        } finally {
            update(token, "MAIN_SITE", List.of(
                item("defaultSeoTitle", originalTitle),
                item("homeNewsLimit", originalLimit)
            ));
        }
    }

    @Test
    void rejectsDuplicateUnknownAndInvalidTypedValues() throws Exception {
        String token = login();

        assertUpdateRejected(token, "MAIN_SITE", List.of(
            item("homeNewsLimit", "8"),
            item("homeNewsLimit", "9")
        ), "配置项包含重复配置键");
        assertUpdateRejected(token, "MAIN_SITE", List.of(
            item("unknownConfig", "value")
        ), "站点配置项不存在：unknownConfig");
        assertUpdateRejected(token, "MAIN_SITE", List.of(
            item("homeNewsLimit", "many")
        ), "数字配置值格式不正确");
        assertUpdateRejected(token, "MAIN_SITE", List.of(
            item("homeNewsLimit", "0")
        ), "首页展示数量必须是 1-100 的整数");
        assertUpdateRejected(token, "GLOBAL", List.of(
            item("siteLogo", "javascript:alert(1)")
        ), "图片配置值必须是站内相对地址或有效的 HTTP/HTTPS 地址");
    }

    @Test
    void endpointRequiresSiteConfigAuthority() throws Exception {
        mockMvc.perform(get("/api/admin/site-config"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "cms:content")
    void unrelatedAuthorityCannotReadSiteConfig() throws Exception {
        mockMvc.perform(get("/api/admin/site-config"))
            .andExpect(status().isForbidden());
    }

    private String configValue(String token, String siteType, String configKey) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/site-config")
                .headers(auth(token))
                .param("siteType", siteType))
            .andExpect(status().isOk())
            .andReturn();
        for (JsonNode config : objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data")) {
            if (configKey.equals(config.path("configKey").stringValue())) {
                return config.path("configValue").stringValue();
            }
        }
        throw new AssertionError("Missing config: " + siteType + "/" + configKey);
    }

    private void assertUpdateRejected(
        String token,
        String siteType,
        List<Map<String, String>> items,
        String message
    ) throws Exception {
        mockMvc.perform(put("/api/admin/site-config/{siteType}", siteType)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("items", items))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value(message));
    }

    private void update(String token, String siteType, List<Map<String, String>> items) throws Exception {
        mockMvc.perform(put("/api/admin/site-config/{siteType}", siteType)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("items", items))))
            .andExpect(status().isOk());
    }

    private Map<String, String> item(String configKey, String configValue) {
        return Map.of("configKey", configKey, "configValue", configValue);
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
