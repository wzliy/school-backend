package com.zlwang.school.modules.log;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class SystemLogControllerIntegrationTests {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void recordsSuccessfulAndFailedLoginAttempts() throws Exception {
        String missingAccount = "missing-log-" + SEQUENCE.incrementAndGet();
        mockMvc.perform(post("/api/admin/auth/login")
                .header(HttpHeaders.USER_AGENT, "FailedLoginAgent")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("username", missingAccount, "password", "Wrong@123"))))
            .andExpect(status().isUnauthorized());

        String token = login("SuccessfulLoginAgent");

        mockMvc.perform(get("/api/admin/logs/login")
                .headers(auth(token))
                .param("username", missingAccount)
                .param("loginStatus", "FAIL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.total").value(1))
            .andExpect(jsonPath("$.data.records[0].userId").isEmpty())
            .andExpect(jsonPath("$.data.records[0].username").value(missingAccount))
            .andExpect(jsonPath("$.data.records[0].userAgent").value("FailedLoginAgent"))
            .andExpect(jsonPath("$.data.records[0].loginStatus").value("FAIL"));

        mockMvc.perform(get("/api/admin/logs/login")
                .headers(auth(token))
                .param("username", "admin")
                .param("loginStatus", "SUCCESS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].userId").value(1))
            .andExpect(jsonPath("$.data.records[0].userAgent").value("SuccessfulLoginAgent"));
    }

    @Test
    void recordsSuccessfulAndFailedWriteOperations() throws Exception {
        String token = login("OperationLogAgent");

        mockMvc.perform(post("/api/admin/auth/logout").headers(auth(token)))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/logs/operations")
                .headers(auth(token))
                .param("username", "admin")
                .param("moduleName", "AUTH")
                .param("operationType", "LOGOUT")
                .param("resultStatus", "SUCCESS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].requestUri").value("/api/admin/auth/logout"))
            .andExpect(jsonPath("$.data.records[0].requestMethod").value("POST"))
            .andExpect(jsonPath("$.data.records[0].resultStatus").value("SUCCESS"));

        Map<String, Object> unsafeLink = new LinkedHashMap<>();
        unsafeLink.put("siteType", "GLOBAL");
        unsafeLink.put("name", "审计失败链接 " + SEQUENCE.incrementAndGet());
        unsafeLink.put("linkUrl", "javascript:alert(1)");
        unsafeLink.put("sortNo", 10);
        unsafeLink.put("enabled", true);
        mockMvc.perform(post("/api/admin/friend-links")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(unsafeLink)))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/admin/logs/operations")
                .headers(auth(token))
                .param("username", "admin")
                .param("moduleName", "FRIEND_LINK")
                .param("operationType", "CREATE")
                .param("resultStatus", "FAIL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.records[0].requestUri").value("/api/admin/friend-links"))
            .andExpect(jsonPath("$.data.records[0].errorMessage").value("HTTP 400"));
    }

    @Test
    void rejectsInvalidTimeRange() throws Exception {
        String token = login("TimeRangeAgent");

        mockMvc.perform(get("/api/admin/logs/operations")
                .headers(auth(token))
                .param("startTime", "2026-07-18T00:00:00")
                .param("endTime", "2026-07-17T00:00:00"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value("startTime 不能晚于 endTime"));
    }

    @Test
    void endpointsRequireLogAuthority() throws Exception {
        mockMvc.perform(get("/api/admin/logs/operations"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "cms:content")
    void unrelatedAuthorityCannotReadLogs() throws Exception {
        mockMvc.perform(get("/api/admin/logs/login"))
            .andExpect(status().isForbidden());
    }

    private String login(String userAgent) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .header(HttpHeaders.USER_AGENT, userAgent)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("username", "admin", "password", "Admin@123456"))))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray())
            .path("data")
            .path("accessToken")
            .stringValue();
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
