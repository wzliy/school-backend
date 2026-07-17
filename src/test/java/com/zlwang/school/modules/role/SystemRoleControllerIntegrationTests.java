package com.zlwang.school.modules.role;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class SystemRoleControllerIntegrationTests {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void roleCrudSupportsPagingUpdateAndDelete() throws Exception {
        String adminToken = login("admin", "Admin@123456");
        String suffix = nextSuffix();
        String roleCode = "TEST_ROLE_" + suffix;
        long roleId = createRole(adminToken, "测试角色" + suffix, roleCode);

        mockMvc.perform(get("/api/admin/roles")
                .headers(auth(adminToken))
                .param("roleCode", roleCode)
                .param("pageNo", "1")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.records[0].roleCode").value(roleCode));

        mockMvc.perform(put("/api/admin/roles/{id}", roleId)
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "roleName", "已更新角色" + suffix,
                    "status", 1,
                    "sortNo", 88,
                    "remark", "角色更新测试"
                ))))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/roles/{id}", roleId).headers(auth(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.roleName").value("已更新角色" + suffix))
            .andExpect(jsonPath("$.data.sortNo").value(88));

        mockMvc.perform(delete("/api/admin/roles/{id}", roleId).headers(auth(adminToken)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/roles/{id}", roleId).headers(auth(adminToken)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("A0404"));
    }

    @Test
    void permissionChangesApplyToExistingJwtAndAssignedRoleCannotBeDeleted() throws Exception {
        String adminToken = login("admin", "Admin@123456");
        String suffix = nextSuffix();
        String roleCode = "CONTENT_TEST_" + suffix;
        long roleId = createRole(adminToken, "内容测试角色" + suffix, roleCode);

        assignPermissions(adminToken, roleId, List.of(5L, 7L, 202L));

        String username = "role_user_" + suffix.toLowerCase();
        createUser(adminToken, username, roleId);
        String userToken = login(username, "Initial@123");

        assertThat(currentPermissions(userToken))
            .contains("cms:content:manage")
            .doesNotContain("cms:column:manage");

        mockMvc.perform(get("/api/admin/roles").headers(auth(userToken)))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("A0403"));

        assignPermissions(adminToken, roleId, List.of(5L, 6L, 201L));

        assertThat(currentPermissions(userToken))
            .contains("cms:column:manage")
            .doesNotContain("cms:content:manage");

        mockMvc.perform(delete("/api/admin/roles/{id}", roleId).headers(auth(adminToken)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("B0409"))
            .andExpect(jsonPath("$.msg").value("角色仍被用户使用，不能删除"));
    }

    @Test
    void duplicateInvalidPermissionsAndSuperAdminChangesAreRejected() throws Exception {
        String adminToken = login("admin", "Admin@123456");
        String suffix = nextSuffix();
        String roleCode = "PROTECTED_TEST_" + suffix;
        createRole(adminToken, "保护测试角色" + suffix, roleCode);

        mockMvc.perform(post("/api/admin/roles")
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleBody("重复角色", roleCode)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("B0409"));

        long roleId = createRole(
            adminToken,
            "权限校验角色" + suffix,
            "PERMISSION_TEST_" + suffix
        );
        mockMvc.perform(put("/api/admin/roles/{id}/permissions", roleId)
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("permissionIds", List.of(99999L)))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("A0400"));

        mockMvc.perform(put("/api/admin/roles/1")
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "roleName", "超级管理员",
                    "status", 0,
                    "sortNo", 1,
                    "remark", "不可禁用"
                ))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("不能禁用超级管理员角色"));

        mockMvc.perform(put("/api/admin/roles/1/permissions")
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("permissionIds", List.of(1L)))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("不能修改超级管理员角色权限"));

        mockMvc.perform(delete("/api/admin/roles/1").headers(auth(adminToken)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("不能删除超级管理员角色"));
    }

    private long createRole(String token, String roleName, String roleCode) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/roles")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(roleBody(roleName, roleCode)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data").longValue();
    }

    private String roleBody(String roleName, String roleCode) throws Exception {
        return json(Map.of(
            "roleName", roleName,
            "roleCode", roleCode,
            "status", 1,
            "sortNo", 50,
            "remark", "集成测试角色"
        ));
    }

    private void assignPermissions(String token, long roleId, List<Long> permissionIds) throws Exception {
        mockMvc.perform(put("/api/admin/roles/{id}/permissions", roleId)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("permissionIds", permissionIds))))
            .andExpect(status().isOk());
    }

    private void createUser(String token, String username, long roleId) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", username);
        body.put("password", "Initial@123");
        body.put("realName", "角色测试用户");
        body.put("email", username + "@example.com");
        body.put("phone", "13800000000");
        body.put("status", 1);
        body.put("remark", "角色权限集成测试");
        body.put("roleIds", List.of(roleId));
        mockMvc.perform(post("/api/admin/users")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)))
            .andExpect(status().isOk());
    }

    private List<String> currentPermissions(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/auth/me").headers(auth(token)))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode permissions = objectMapper.readTree(result.getResponse().getContentAsByteArray())
            .path("data")
            .path("permissions");
        return permissions.valueStream().map(JsonNode::stringValue).toList();
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("username", username, "password", password))))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray())
            .path("data")
            .path("accessToken")
            .stringValue();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private HttpHeaders auth(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return headers;
    }

    private String nextSuffix() {
        return Integer.toString(SEQUENCE.incrementAndGet());
    }
}
