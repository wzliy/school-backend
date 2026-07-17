package com.zlwang.school.modules.column;

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
class CmsColumnControllerIntegrationTests {

    private static final AtomicInteger COLUMN_SEQUENCE = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void seededTreeAndEditorSchemaAreAvailable() throws Exception {
        String token = login();

        mockMvc.perform(get("/api/admin/columns/tree")
                .headers(auth(token))
                .param("siteType", "MAIN_SITE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(7))
            .andExpect(jsonPath("$.data[0].columnCode").value("about"))
            .andExpect(jsonPath("$.data[1].columnCode").value("news"));

        mockMvc.perform(get("/api/admin/columns/101/editor-schema").headers(auth(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.templateKey").value("ARTICLE_LIST"))
            .andExpect(jsonPath("$.data.detailTemplateKey").value("ARTICLE_DETAIL"))
            .andExpect(jsonPath("$.data.templateConfig.page.pageSize").value(10))
            .andExpect(jsonPath("$.data.pageConfigFields[0].fieldCode").value("coverUrl"))
            .andExpect(jsonPath("$.data.contentFields[0].fieldCode").value("title"));
    }

    @Test
    void columnCrudSortAndStatusWorkflow() throws Exception {
        String token = login();
        String suffix = nextSuffix();
        long parentId = create(token, pageBody(0L, "测试父栏目" + suffix, "test-parent-" + suffix));
        long childId = create(token, articleBody(0L, "测试子栏目" + suffix, "test-child-" + suffix));

        mockMvc.perform(put("/api/admin/columns/sort")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("items", List.of(
                    Map.of("id", parentId, "parentId", 0, "sortNo", 501),
                    Map.of("id", childId, "parentId", parentId, "sortNo", 10)
                )))))
            .andExpect(status().isOk());

        MvcResult treeResult = mockMvc.perform(get("/api/admin/columns/tree")
                .headers(auth(token))
                .param("siteType", "MAIN_SITE"))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode parent = findById(
            objectMapper.readTree(treeResult.getResponse().getContentAsByteArray()).path("data"),
            parentId
        );
        assertThat(parent).isNotNull();
        assertThat(parent.path("children").get(0).path("id").longValue()).isEqualTo(childId);

        Map<String, Object> update = pageBody(0L, "已更新父栏目" + suffix, "test-parent-updated-" + suffix);
        update.put("sortNo", 502);
        mockMvc.perform(put("/api/admin/columns/{id}", parentId)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(update)))
            .andExpect(status().isOk());

        mockMvc.perform(put("/api/admin/columns/{id}/status", parentId)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("enabled", false))))
            .andExpect(status().isOk());

        mockMvc.perform(delete("/api/admin/columns/{id}", parentId).headers(auth(token)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.msg").value("栏目包含子栏目，不能删除"));

        mockMvc.perform(delete("/api/admin/columns/{id}", childId).headers(auth(token)))
            .andExpect(status().isOk());
        mockMvc.perform(delete("/api/admin/columns/{id}", parentId).headers(auth(token)))
            .andExpect(status().isOk());
    }

    @Test
    void invalidTemplatesParentsCyclesAndConfigurationAreRejected() throws Exception {
        String token = login();
        String suffix = nextSuffix();

        Map<String, Object> incompatible = articleBody(0L, "错误模板", "invalid-template-" + suffix);
        incompatible.put("templateKey", "SINGLE_PAGE");
        incompatible.put("detailTemplateKey", null);
        mockMvc.perform(post("/api/admin/columns")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(incompatible)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("页面模板与站点或栏目类型不兼容"));

        Map<String, Object> crossSite = articleBody(200L, "跨站栏目", "cross-site-" + suffix);
        mockMvc.perform(post("/api/admin/columns")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(crossSite)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("父栏目与当前栏目必须属于同一站点"));

        Map<String, Object> unknownConfig = articleBody(0L, "非法配置", "bad-config-" + suffix);
        unknownConfig.put("templateConfig", Map.of("page", Map.of("script", "alert(1)")));
        mockMvc.perform(post("/api/admin/columns")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(unknownConfig)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("templateConfig.page 包含未定义字段：script"));

        long parentId = create(token, pageBody(0L, "循环父栏目" + suffix, "cycle-parent-" + suffix));
        long childId = create(token, pageBody(parentId, "循环子栏目" + suffix, "cycle-child-" + suffix));
        Map<String, Object> cycleUpdate = pageBody(childId, "循环父栏目" + suffix, "cycle-parent-" + suffix);
        mockMvc.perform(put("/api/admin/columns/{id}", parentId)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(cycleUpdate)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("栏目父级关系不能形成循环"));

        mockMvc.perform(delete("/api/admin/columns/{id}", childId).headers(auth(token))).andExpect(status().isOk());
        mockMvc.perform(delete("/api/admin/columns/{id}", parentId).headers(auth(token))).andExpect(status().isOk());
    }

    @Test
    void endpointRequiresColumnAuthority() throws Exception {
        mockMvc.perform(get("/api/admin/columns/tree"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "cms:content")
    void unrelatedAuthorityCannotReadColumns() throws Exception {
        mockMvc.perform(get("/api/admin/columns/tree"))
            .andExpect(status().isForbidden());
    }

    private long create(String token, Map<String, Object> body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/columns")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data").longValue();
    }

    private Map<String, Object> pageBody(long parentId, String name, String code) {
        return baseBody(parentId, name, code, "PAGE", "/" + code, "SINGLE_PAGE", null, Map.of("page", Map.of()));
    }

    private Map<String, Object> articleBody(long parentId, String name, String code) {
        return baseBody(
            parentId,
            name,
            code,
            "LIST",
            "/" + code,
            "ARTICLE_LIST",
            "ARTICLE_DETAIL",
            Map.of("page", Map.of("pageSize", 15))
        );
    }

    private Map<String, Object> baseBody(
        long parentId,
        String name,
        String code,
        String type,
        String route,
        String templateKey,
        String detailTemplateKey,
        Map<String, Object> templateConfig
    ) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("parentId", parentId);
        body.put("siteType", "MAIN_SITE");
        body.put("columnName", name);
        body.put("columnCode", code);
        body.put("columnType", type);
        body.put("routePath", route);
        body.put("templateKey", templateKey);
        body.put("detailTemplateKey", detailTemplateKey);
        body.put("templateConfig", templateConfig);
        body.put("sortNo", 500);
        body.put("navVisible", true);
        body.put("enabled", true);
        body.put("remark", "栏目集成测试");
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

    private String nextSuffix() {
        return Integer.toString(COLUMN_SEQUENCE.incrementAndGet());
    }

    private JsonNode findById(JsonNode nodes, long id) {
        for (JsonNode node : nodes) {
            if (node.path("id").longValue() == id) {
                return node;
            }
            JsonNode found = findById(node.path("children"), id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
