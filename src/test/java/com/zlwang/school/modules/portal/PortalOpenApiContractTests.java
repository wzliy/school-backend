package com.zlwang.school.modules.portal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PortalOpenApiContractTests {

    private static final Set<String> PORTAL_PATHS = Set.of(
        "/api/portal/pages/{pageCode}",
        "/api/portal/recruit/home",
        "/api/portal/site-config",
        "/api/portal/navigation",
        "/api/portal/banners",
        "/api/portal/friend-links",
        "/api/portal/columns",
        "/api/portal/columns/{id}",
        "/api/portal/columns/{id}/contents",
        "/api/portal/contents/{id}",
        "/api/portal/search",
        "/api/portal/contents/{id}/view-count"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generatedOpenApiContainsPortalPathsParametersAndDtoExamples() throws Exception {
        JsonNode document = apiDocument();
        JsonNode paths = document.path("paths");

        assertThat(PORTAL_PATHS).allMatch(paths::has);
        assertThat(paths.path("/api/portal/contents/{id}/view-count").has("put")).isTrue();

        Map<String, JsonNode> searchParameters = parameters(
            paths.path("/api/portal/search").path("get")
        );
        assertThat(searchParameters.keySet())
            .containsExactlyInAnyOrder("keyword", "siteType", "columnId", "pageNo", "pageSize");
        assertThat(searchParameters.get("keyword").path("required").booleanValue()).isTrue();
        assertThat(searchParameters.get("keyword").path("schema").path("example").stringValue())
            .isEqualTo("校园新闻");
        assertThat(searchParameters.get("siteType").path("schema").path("example").stringValue())
            .isEqualTo("MAIN_SITE");
        assertThat(searchParameters.get("pageNo").path("schema").path("default").longValue())
            .isEqualTo(1);
        assertThat(searchParameters.get("pageSize").path("schema").path("maximum").longValue())
            .isEqualTo(100);

        JsonNode schemas = document.path("components").path("schemas");
        assertThat(Set.of(
            "PortalPageResponse",
            "PortalColumnDetailResponse",
            "PortalContentDetailResponse",
            "PortalSearchResponse",
            "PortalViewCountResponse"
        )).allSatisfy(name -> assertThat(schemas.path(name).has("example"))
            .as("%s should define an OpenAPI example", name)
            .isTrue());
    }

    private JsonNode apiDocument() throws Exception {
        byte[] body = mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
        return objectMapper.readTree(body);
    }

    private Map<String, JsonNode> parameters(JsonNode operation) {
        return StreamSupport.stream(operation.path("parameters").spliterator(), false)
            .collect(Collectors.toMap(node -> node.path("name").stringValue(), Function.identity()));
    }
}
