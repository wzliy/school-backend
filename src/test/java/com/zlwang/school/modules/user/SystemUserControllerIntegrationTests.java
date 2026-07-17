package com.zlwang.school.modules.user;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class SystemUserControllerIntegrationTests {

    private static final AtomicInteger USER_SEQUENCE = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void userManagementWorkflowUpdatesAuthenticationState() throws Exception {
        String adminToken = login("admin", "Admin@123456");
        String username = nextUsername();
        long userId = createUser(adminToken, username, "Initial@123", List.of(3L));

        mockMvc.perform(get("/api/admin/users/{id}", userId).headers(auth(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value(username))
            .andExpect(jsonPath("$.data.roleIds[0]").value(3));

        mockMvc.perform(put("/api/admin/users/{id}", userId)
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "realName", "内容编辑二号",
                    "email", username + "@example.com",
                    "phone", "13800000000",
                    "remark", "集成测试用户"
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(put("/api/admin/users/{id}/roles", userId)
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("roleIds", List.of(2L, 5L)))))
            .andExpect(status().isOk());

        mockMvc.perform(put("/api/admin/users/{id}/password", userId)
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("password", "Changed@123"))))
            .andExpect(status().isOk());

        String userToken = login(username, "Changed@123");

        mockMvc.perform(put("/api/admin/users/{id}/status", userId)
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("status", 0))))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/auth/me").headers(auth(userToken)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("A0401"));

        mockMvc.perform(delete("/api/admin/users/{id}", userId).headers(auth(adminToken)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/users/{id}", userId).headers(auth(adminToken)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("A0404"));
    }

    @Test
    void userPageAndRoleOptionsAreAvailable() throws Exception {
        String adminToken = login("admin", "Admin@123456");

        mockMvc.perform(get("/api/admin/users")
                .headers(auth(adminToken))
                .param("pageNo", "1")
                .param("pageSize", "10")
                .param("username", "admin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.records[0].username").value("admin"));

        mockMvc.perform(get("/api/admin/users/role-options").headers(auth(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data[0].code").value("SUPER_ADMIN"));
    }

    @Test
    void duplicateUsernameAndUnknownRoleAreRejected() throws Exception {
        String adminToken = login("admin", "Admin@123456");

        mockMvc.perform(post("/api/admin/users")
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody("admin", "Another@123", List.of(1L))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("B0409"));

        mockMvc.perform(post("/api/admin/users")
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody(nextUsername(), "Another@123", List.of(999L))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("A0400"));
    }

    @Test
    void currentUserCannotDisableOrDeleteItself() throws Exception {
        String adminToken = login("admin", "Admin@123456");

        mockMvc.perform(put("/api/admin/users/1/status")
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("status", 0))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("不能禁用当前登录账号"));

        mockMvc.perform(delete("/api/admin/users/1").headers(auth(adminToken)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("不能删除当前登录账号"));

        mockMvc.perform(put("/api/admin/users/1/roles")
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("roleIds", List.of(2L)))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("不能移除当前账号的超级管理员角色"));
    }

    private long createUser(String token, String username, String password, List<Long> roleIds) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/users")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createBody(username, password, roleIds)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data").asLong();
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("username", username, "password", password))))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsByteArray());
        return response.path("data").path("accessToken").stringValue();
    }

    private String createBody(String username, String password, List<Long> roleIds) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("password", password);
        body.put("realName", "测试用户");
        body.put("email", username + "@example.com");
        body.put("phone", "13800000000");
        body.put("status", 1);
        body.put("remark", "集成测试创建");
        body.put("roleIds", roleIds);
        return json(body);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private String nextUsername() {
        return "editor_" + USER_SEQUENCE.incrementAndGet();
    }

    private HttpHeaders auth(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }
}
