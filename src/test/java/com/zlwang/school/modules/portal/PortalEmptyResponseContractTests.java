package com.zlwang.school.modules.portal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PortalEmptyResponseContractTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void emptyListsAndPagesKeepStableCollectionShapes() throws Exception {
        mockMvc.perform(get("/api/portal/banners")
                .param("siteType", "MAIN_SITE")
                .param("position", "HOME"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, containsString("no-store")))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(0));

        mockMvc.perform(get("/api/portal/columns/{id}/contents", 101))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records").isArray())
            .andExpect(jsonPath("$.data.records.length()").value(0))
            .andExpect(jsonPath("$.data.total").value(0))
            .andExpect(jsonPath("$.data.pageNo").value(1))
            .andExpect(jsonPath("$.data.pageSize").value(10));

        mockMvc.perform(get("/api/portal/search")
                .param("keyword", "__portal_no_result__")
                .param("siteType", "MAIN_SITE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records").isArray())
            .andExpect(jsonPath("$.data.records.length()").value(0))
            .andExpect(jsonPath("$.data.total").value(0))
            .andExpect(jsonPath("$.data.seo").isMap());
    }

    @Test
    void pageAndTreeDtosNeverOmitCollectionOrObjectSlots() throws Exception {
        JsonNode home = responseData("/api/portal/pages/HOME");
        assertThat(home.path("siteConfig").isObject()).isTrue();
        assertThat(home.path("seo").isObject()).isTrue();
        assertThat(home.path("sections").isArray()).isTrue();
        home.path("sections").forEach(section -> {
            assertThat(section.path("config").isObject()).isTrue();
            assertThat(section.path("banners").isArray()).isTrue();
            assertThat(section.path("contents").isArray()).isTrue();
            assertThat(section.path("links").isArray()).isTrue();
            assertThat(section.path("friendLinks").isArray()).isTrue();
            assertThat(section.path("contact").isObject()).isTrue();
        });

        JsonNode columns = responseData("/api/portal/columns?siteType=MAIN_SITE");
        assertThat(columns.isArray()).isTrue();
        columns.forEach(column -> assertThat(column.path("children").isArray()).isTrue());
    }

    private JsonNode responseData(String path) throws Exception {
        byte[] body = mockMvc.perform(get(path))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
        return objectMapper.readTree(body).path("data");
    }
}
