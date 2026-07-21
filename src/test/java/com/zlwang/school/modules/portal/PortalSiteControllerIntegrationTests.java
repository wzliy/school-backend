package com.zlwang.school.modules.portal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PortalSiteControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

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
}
