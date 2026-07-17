package com.zlwang.school.modules.permission;

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
class SystemPermissionControllerIntegrationTests {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void permissionTreeCrudRejectsCyclesAndChildDeletion() throws Exception {
        String adminToken = login();
        String suffix = nextSuffix();
        String rootCode = "test:root:" + suffix;
        long rootId = createPermission(adminToken, menuBody(0L, "测试根菜单" + suffix, rootCode));
        long childId = createPermission(
            adminToken,
            menuBody(rootId, "测试子菜单" + suffix, "test:child:" + suffix)
        );
        long buttonId = createPermission(
            adminToken,
            buttonBody(childId, "测试按钮" + suffix, "test:button:" + suffix)
        );

        JsonNode tree = getTree(adminToken);
        assertThat(findByCode(tree, rootCode)).isNotNull();
        assertThat(findByCode(tree, "test:button:" + suffix)).isNotNull();

        mockMvc.perform(put("/api/admin/permissions/{id}", rootId)
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(updateMenuBody(childId, "循环菜单", 1))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("不能将权限移动到自身或子节点下"));

        mockMvc.perform(delete("/api/admin/permissions/{id}", rootId).headers(auth(adminToken)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.msg").value("权限存在子节点，不能删除"));

        mockMvc.perform(put("/api/admin/permissions/{id}", childId)
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(updateMenuBody(rootId, "已更新子菜单" + suffix, 1))))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/permissions/{id}", childId).headers(auth(adminToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.permissionName").value("已更新子菜单" + suffix));

        mockMvc.perform(delete("/api/admin/permissions/{id}", buttonId).headers(auth(adminToken)))
            .andExpect(status().isOk());
        mockMvc.perform(delete("/api/admin/permissions/{id}", childId).headers(auth(adminToken)))
            .andExpect(status().isOk());
        mockMvc.perform(delete("/api/admin/permissions/{id}", rootId).headers(auth(adminToken)))
            .andExpect(status().isOk());
    }

    @Test
    void assignedPermissionCannotBeDeletedUntilRoleReleasesIt() throws Exception {
        String adminToken = login();
        String suffix = nextSuffix();
        long permissionId = createPermission(
            adminToken,
            menuBody(0L, "角色占用菜单" + suffix, "test:assigned:" + suffix)
        );
        long roleId = createRole(adminToken, "权限占用角色" + suffix, "PERMISSION_OWNER_" + suffix);

        assignPermissions(adminToken, roleId, List.of(permissionId));

        mockMvc.perform(delete("/api/admin/permissions/{id}", permissionId).headers(auth(adminToken)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.msg").value("权限仍被角色使用，不能删除"));

        assignPermissions(adminToken, roleId, List.of());

        mockMvc.perform(delete("/api/admin/permissions/{id}", permissionId).headers(auth(adminToken)))
            .andExpect(status().isOk());
        mockMvc.perform(delete("/api/admin/roles/{id}", roleId).headers(auth(adminToken)))
            .andExpect(status().isOk());
    }

    @Test
    void newPermissionIsImmediatelyVisibleToSuperAdminAndBuiltInsAreProtected() throws Exception {
        String adminToken = login();
        String suffix = nextSuffix();
        String permissionCode = "test:dynamic:" + suffix;
        long permissionId = createPermission(
            adminToken,
            menuBody(0L, "动态权限" + suffix, permissionCode)
        );

        assertThat(currentPermissions(adminToken)).contains(permissionCode);

        mockMvc.perform(post("/api/admin/permissions")
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(menuBody(0L, "重复权限", permissionCode))))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value("B0409"));

        mockMvc.perform(post("/api/admin/permissions")
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(buttonBody(0L, "非法根按钮", "test:invalid:" + suffix))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("BUTTON 和 API 权限必须挂在菜单下"));

        mockMvc.perform(put("/api/admin/permissions/1")
                .headers(auth(adminToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(updateMenuBody(0L, "系统管理", 0))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("不能禁用内置权限"));

        mockMvc.perform(delete("/api/admin/permissions/1").headers(auth(adminToken)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("不能删除内置权限"));

        mockMvc.perform(delete("/api/admin/permissions/{id}", permissionId).headers(auth(adminToken)))
            .andExpect(status().isOk());
    }

    private long createPermission(String token, Map<String, Object> body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/permissions")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data").longValue();
    }

    private Map<String, Object> menuBody(long parentId, String name, String code) {
        Map<String, Object> body = baseBody(parentId, name);
        body.put("permissionCode", code);
        body.put("permissionType", "MENU");
        body.put("routePath", "/test/" + code.replace(':', '-'));
        body.put("componentPath", "test/index");
        body.put("icon", "Settings");
        return body;
    }

    private Map<String, Object> buttonBody(long parentId, String name, String code) {
        Map<String, Object> body = baseBody(parentId, name);
        body.put("permissionCode", code);
        body.put("permissionType", "BUTTON");
        body.put("apiMethod", "POST");
        body.put("apiPath", "/api/admin/test");
        return body;
    }

    private Map<String, Object> updateMenuBody(long parentId, String name, int status) {
        Map<String, Object> body = baseBody(parentId, name);
        body.put("routePath", "/test/updated");
        body.put("componentPath", "test/index");
        body.put("icon", "Settings");
        body.put("status", status);
        return body;
    }

    private Map<String, Object> baseBody(long parentId, String name) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("parentId", parentId);
        body.put("permissionName", name);
        body.put("sortNo", 500);
        body.put("visible", true);
        body.put("status", 1);
        body.put("remark", "权限集成测试");
        return body;
    }

    private long createRole(String token, String roleName, String roleCode) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/roles")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of(
                    "roleName", roleName,
                    "roleCode", roleCode,
                    "status", 1,
                    "sortNo", 60,
                    "remark", "权限占用测试"
                ))))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data").longValue();
    }

    private void assignPermissions(String token, long roleId, List<Long> permissionIds) throws Exception {
        mockMvc.perform(put("/api/admin/roles/{id}/permissions", roleId)
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("permissionIds", permissionIds))))
            .andExpect(status().isOk());
    }

    private JsonNode getTree(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/permissions/tree").headers(auth(token)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data");
    }

    private JsonNode findByCode(JsonNode nodes, String code) {
        for (JsonNode node : nodes) {
            if (code.equals(node.path("permissionCode").stringValue())) {
                return node;
            }
            JsonNode found = findByCode(node.path("children"), code);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private List<String> currentPermissions(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/auth/me").headers(auth(token)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray())
            .path("data")
            .path("permissions")
            .valueStream()
            .map(JsonNode::stringValue)
            .toList();
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("username", "admin", "password", "Admin@123456"))))
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
