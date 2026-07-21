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
class PortalPageControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void homeAggregationIsPublicAndUsesStableSectionShape() throws Exception {
        mockMvc.perform(get("/api/portal/pages/HOME"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("000000"))
            .andExpect(jsonPath("$.data.pageCode").value("HOME"))
            .andExpect(jsonPath("$.data.templateKey").value("HOME"))
            .andExpect(jsonPath("$.data.siteType").value("MAIN_SITE"))
            .andExpect(jsonPath("$.data.siteConfig.siteName").value("高校官网"))
            .andExpect(jsonPath("$.data.seo.canonicalPath").value("/"))
            .andExpect(jsonPath("$.data.sections.length()").value(6))
            .andExpect(jsonPath("$.data.sections[0].sectionCode").value("HERO"))
            .andExpect(jsonPath("$.data.sections[0].banners").isArray())
            .andExpect(jsonPath("$.data.sections[1].sourceColumn.id").value(101))
            .andExpect(jsonPath("$.data.sections[1].contents").isArray())
            .andExpect(jsonPath("$.data.sections[3].links.length()").value(7))
            .andExpect(jsonPath("$.data.sections[5].friendLinks.length()").value(1));
    }

    @Test
    void recruitAliasReturnsTheSamePageContract() throws Exception {
        mockMvc.perform(get("/api/portal/recruit/home"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.pageCode").value("RECRUIT_HOME"))
            .andExpect(jsonPath("$.data.templateKey").value("RECRUIT_HOME"))
            .andExpect(jsonPath("$.data.siteType").value("RECRUIT_SITE"))
            .andExpect(jsonPath("$.data.seo.canonicalPath").value("/recruit"))
            .andExpect(jsonPath("$.data.sections.length()").value(7))
            .andExpect(jsonPath("$.data.sections[6].sectionCode").value("CONTACT"))
            .andExpect(jsonPath("$.data.sections[6].contact.phone").value(""))
            .andExpect(jsonPath("$.data.sections[6].contact.address").value(""));
    }

    @Test
    void unsupportedPageCodeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/portal/pages/ARTICLE_LIST"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("A0400"));
    }
}
