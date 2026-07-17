package com.zlwang.school.modules.template;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PageTemplateControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/admin/page-templates"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    @WithMockUser(authorities = "cms:content")
    void unrelatedAuthorityIsRejected() throws Exception {
        mockMvc.perform(get("/api/admin/page-templates"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("A0403"));
    }

    @Test
    @WithMockUser(authorities = "cms:column")
    void columnReaderReceivesTemplateMetadataAndEditorSchemas() throws Exception {
        mockMvc.perform(get("/api/admin/page-templates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("000000"))
            .andExpect(jsonPath("$.data.length()").value(8))
            .andExpect(jsonPath("$.data[0].templateKey").value("HOME"))
            .andExpect(jsonPath("$.data[0].compatibleSiteTypes[0]").value("MAIN_SITE"))
            .andExpect(jsonPath("$.data[0].compatibleColumnTypes[0]").value("SPECIAL"))
            .andExpect(jsonPath("$.data[1].templateKey").value("ARTICLE_LIST"))
            .andExpect(jsonPath("$.data[1].defaultDetailTemplateKey").value("ARTICLE_DETAIL"))
            .andExpect(jsonPath("$.data[1].editorSchema.columnFields[0].fieldCode").value("coverUrl"))
            .andExpect(jsonPath("$.data[1].editorSchema.contentFields[0].fieldCode").value("title"))
            .andExpect(jsonPath("$.data[4].editorSchema.extensionFields[0].fieldCode").value("shortName"))
            .andExpect(jsonPath("$.data[7].compatibleColumnTypes").isEmpty())
            .andExpect(jsonPath("$.data[7].editorSchema.pageFields[0].fieldCode").value("searchScope"));
    }
}
