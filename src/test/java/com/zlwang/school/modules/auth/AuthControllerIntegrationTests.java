package com.zlwang.school.modules.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.zlwang.school.common.api.ApiResult;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AuthControllerIntegrationTests.PermissionTestController.class)
class AuthControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void loginReturnsJwtAndCurrentUser() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("ADMIN", "Admin@123456")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("000000"))
            .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
            .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
            .andExpect(jsonPath("$.data.expiresIn").value(7200))
            .andExpect(jsonPath("$.data.user.username").value("admin"))
            .andExpect(jsonPath("$.data.user.roles[0]").value("SUPER_ADMIN"))
            .andExpect(jsonPath("$.data.user.permissions").isArray())
            .andExpect(jsonPath("$.data.user.menus").isArray());
    }

    @Test
    void loginTokenContainsRequiredClaims() throws Exception {
        Jwt jwt = jwtDecoder.decode(loginAndGetToken());

        assertThat(jwt.getIssuer()).hasToString("https://school-backend.local");
        assertThat(jwt.getSubject()).isEqualTo("admin");
        assertThat(((Number) jwt.getClaims().get("uid")).longValue()).isEqualTo(1L);
        assertThat(authorities(jwt)).contains("ROLE_SUPER_ADMIN", "system:user:create");
        assertThat(jwt.getIssuedAt()).isNotNull();
        assertThat(jwt.getExpiresAt()).isAfter(jwt.getIssuedAt());
    }

    @Test
    void loginRejectsInvalidPassword() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("admin", "wrong-password")))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("A0401"))
            .andExpect(jsonPath("$.msg").value("用户名或密码错误"));
    }

    @Test
    void loginValidatesRequestBody() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("", "")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void currentUserRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void invalidJwtReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.jwt.token"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("A0401"));
    }

    @Test
    void jwtCanReadCurrentUserAndLogout() throws Exception {
        String token = loginAndGetToken();

        mockMvc.perform(get("/api/admin/auth/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("admin"))
            .andExpect(jsonPath("$.data.menus[0].permissionCode").value("system"));

        mockMvc.perform(post("/api/admin/auth/logout")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("000000"));
    }

    @Test
    @WithMockUser(username = "editor", authorities = "cms:content")
    void methodPermissionFailureReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/test/permission"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("A0403"));
    }

    private String loginAndGetToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("admin", "Admin@123456")))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        return response.path("data").path("accessToken").stringValue();
    }

    private String loginBody(String username, String password) throws Exception {
        return objectMapper.writeValueAsString(new LoginBody(username, password));
    }

    @SuppressWarnings("unchecked")
    private List<String> authorities(Jwt jwt) {
        return (List<String>) jwt.getClaims().get("authorities");
    }

    private record LoginBody(String username, String password) {
    }

    @RestController
    static class PermissionTestController {

        @GetMapping("/api/admin/test/permission")
        @PreAuthorize("hasAuthority('system:user:create')")
        ApiResult<Void> permissionRequired() {
            return ApiResult.success();
        }
    }
}
