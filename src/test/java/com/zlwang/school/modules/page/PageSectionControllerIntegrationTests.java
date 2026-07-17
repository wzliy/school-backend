package com.zlwang.school.modules.page;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
class PageSectionControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void seededHomeAndRecruitSectionsAreAvailable() throws Exception {
        String token = login();

        mockMvc.perform(get("/api/admin/pages/HOME/sections").headers(auth(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(6))
            .andExpect(jsonPath("$.data[0].sectionCode").value("HERO"))
            .andExpect(jsonPath("$.data[0].config.bannerPosition").value("HOME"))
            .andExpect(jsonPath("$.data[1].sectionCode").value("SCHOOL_NEWS"))
            .andExpect(jsonPath("$.data[1].dataSourceColumnId").value(101));

        mockMvc.perform(get("/api/admin/pages/RECRUIT_HOME/sections").headers(auth(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(7))
            .andExpect(jsonPath("$.data[0].sectionCode").value("HERO"))
            .andExpect(jsonPath("$.data[1].dataSourceColumnId").value(200))
            .andExpect(jsonPath("$.data[6].sectionCode").value("CONTACT"));
    }

    @Test
    void replacesCompleteHomeConfigurationAndRestoresBaseline() throws Exception {
        String token = login();
        List<Map<String, Object>> baseline = homeSections();
        List<Map<String, Object>> updated = homeSections();
        Map<String, Object> hero = find(updated, "HERO");
        hero.put("sortNo", 70);
        hero.put("config", Map.of("bannerPosition", "HOME", "autoplay", false, "intervalSeconds", 6));
        Map<String, Object> news = find(updated, "SCHOOL_NEWS");
        news.put("sortNo", 10);
        news.put("displayCount", 9);
        news.put("displayStyle", "CARD");
        news.put("config", Map.of("showSummary", false, "moreLinkText", "更多新闻"));

        try {
            replace(token, "HOME", updated).andExpect(status().isOk());

            mockMvc.perform(get("/api/admin/pages/HOME/sections").headers(auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sectionCode").value("SCHOOL_NEWS"))
                .andExpect(jsonPath("$.data[0].displayCount").value(9))
                .andExpect(jsonPath("$.data[0].displayStyle").value("CARD"))
                .andExpect(jsonPath("$.data[0].config.showSummary").value(false))
                .andExpect(jsonPath("$.data[5].sectionCode").value("HERO"))
                .andExpect(jsonPath("$.data[5].config.intervalSeconds").value(6));
        } finally {
            replace(token, "HOME", baseline).andExpect(status().isOk());
        }
    }

    @Test
    void rejectsCrossSiteSourcesUnknownConfigAndIncompleteDefinitions() throws Exception {
        String token = login();

        List<Map<String, Object>> crossSite = homeSections();
        find(crossSite, "SCHOOL_NEWS").put("dataSourceColumnId", 200);
        replace(token, "HOME", crossSite)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("数据源栏目与页面必须属于同一站点"));

        List<Map<String, Object>> unknownConfig = homeSections();
        find(unknownConfig, "NOTICE").put("config", Map.of("script", "alert(1)"));
        replace(token, "HOME", unknownConfig)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("config 包含未定义字段：script"));

        List<Map<String, Object>> missing = homeSections();
        missing.removeIf(section -> "FRIEND_LINKS".equals(section.get("sectionCode")));
        replace(token, "HOME", missing)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("页面缺少预定义区块：FRIEND_LINKS"));

        List<Map<String, Object>> changedType = homeSections();
        find(changedType, "HERO").put("sectionType", "CONTENT_FEED");
        replace(token, "HOME", changedType)
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("区块 HERO 的类型不能修改"));
    }

    @Test
    void referencedSourceColumnCannotBeDisabledOrDeleted() throws Exception {
        String token = login();

        mockMvc.perform(put("/api/admin/columns/101/status")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("enabled", false))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.msg").value("栏目被页面区块引用，不能停用"));

        Map<String, Object> update = new LinkedHashMap<>();
        update.put("parentId", 0);
        update.put("columnName", "新闻中心");
        update.put("columnCode", "news");
        update.put("columnType", "LIST");
        update.put("routePath", "/news");
        update.put("templateKey", "ARTICLE_LIST");
        update.put("detailTemplateKey", "ARTICLE_DETAIL");
        update.put("templateConfig", Map.of("page", Map.of()));
        update.put("sortNo", 20);
        update.put("navVisible", true);
        update.put("enabled", false);
        mockMvc.perform(put("/api/admin/columns/101")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(update)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.msg").value("栏目被页面区块引用，不能停用"));

        mockMvc.perform(delete("/api/admin/columns/101").headers(auth(token)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.msg").value("栏目被页面区块引用，不能删除"));
    }

    @Test
    void endpointsRequireColumnAuthoritiesAndKnownPageCode() throws Exception {
        mockMvc.perform(get("/api/admin/pages/HOME/sections"))
            .andExpect(status().isUnauthorized());

        String token = login();
        mockMvc.perform(get("/api/admin/pages/ARTICLE_LIST/sections").headers(auth(token)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "cms:content")
    void unrelatedAuthorityCannotReadPageSections() throws Exception {
        mockMvc.perform(get("/api/admin/pages/HOME/sections"))
            .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.ResultActions replace(
        String token,
        String pageCode,
        List<Map<String, Object>> sections
    ) throws Exception {
        return mockMvc.perform(put("/api/admin/pages/{pageCode}/sections", pageCode)
            .headers(auth(token))
            .contentType(MediaType.APPLICATION_JSON)
            .content(json(Map.of("sections", sections))));
    }

    private List<Map<String, Object>> homeSections() {
        List<Map<String, Object>> sections = new ArrayList<>();
        sections.add(section(
            "HERO", "首页轮播", "HERO_BANNER", null, null, "FULL_WIDTH",
            Map.of("bannerPosition", "HOME"), 10
        ));
        sections.add(section(
            "SCHOOL_NEWS", "学校新闻", "CONTENT_FEED", 101L, 6, "IMAGE_TEXT", Map.of(), 20
        ));
        sections.add(section(
            "NOTICE", "通知公告", "CONTENT_FEED", 102L, 8, "TEXT_LIST", Map.of(), 30
        ));
        sections.add(section(
            "QUICK_LINKS", "快捷入口", "QUICK_LINKS", null, null, "ICON_GRID", Map.of(), 40
        ));
        sections.add(section(
            "CAMPUS_GALLERY", "校园风采", "IMAGE_GALLERY", null, 8, "GRID", Map.of(), 50
        ));
        sections.add(section(
            "FRIEND_LINKS", "友情链接", "FRIEND_LINKS", null, null, "TEXT_LINKS", Map.of(), 60
        ));
        return sections;
    }

    private Map<String, Object> section(
        String code,
        String name,
        String type,
        Long columnId,
        Integer count,
        String style,
        Map<String, Object> config,
        int sortNo
    ) {
        Map<String, Object> section = new LinkedHashMap<>();
        section.put("sectionCode", code);
        section.put("sectionName", name);
        section.put("sectionType", type);
        section.put("dataSourceColumnId", columnId);
        section.put("displayCount", count);
        section.put("displayStyle", style);
        section.put("config", config);
        section.put("sortNo", sortNo);
        section.put("enabled", true);
        return section;
    }

    private Map<String, Object> find(List<Map<String, Object>> sections, String code) {
        return sections.stream()
            .filter(section -> code.equals(section.get("sectionCode")))
            .findFirst()
            .orElseThrow();
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
