package com.zlwang.school.modules.media;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class CmsMediaControllerIntegrationTests {

    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    private static final Path STORAGE_ROOT = createStorageRoot();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void fileProperties(DynamicPropertyRegistry registry) {
        registry.add("app.file.local-path", () -> STORAGE_ROOT.toString());
        registry.add("app.file.max-size", () -> "32B");
    }

    @AfterAll
    static void deleteStorageRoot() throws IOException {
        if (!Files.exists(STORAGE_ROOT)) {
            return;
        }
        try (var paths = Files.walk(STORAGE_ROOT)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    @Test
    void uploadCanBeFilteredReadPubliclyAndDeleted() throws Exception {
        String token = login();
        byte[] bytes = new byte[] {1, 2, 3, 4, 5};
        long mediaId = upload(token, "校园风光.jpg", "image/jpeg", bytes, "首页图片");
        String accessUrl = null;
        try {
            MvcResult detail = mockMvc.perform(get("/api/admin/media/{id}", mediaId).headers(auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storageType").value("LOCAL"))
                .andExpect(jsonPath("$.data.fileType").value("IMAGE"))
                .andExpect(jsonPath("$.data.originalName").value("校园风光.jpg"))
                .andExpect(jsonPath("$.data.extension").value("jpg"))
                .andExpect(jsonPath("$.data.fileSize").value(bytes.length))
                .andExpect(jsonPath("$.data.uploaderId").value(1))
                .andExpect(jsonPath("$.data.remark").value("首页图片"))
                .andReturn();
            accessUrl = objectMapper.readTree(detail.getResponse().getContentAsByteArray())
                .path("data")
                .path("accessUrl")
                .stringValue();

            mockMvc.perform(get("/api/admin/media")
                    .headers(auth(token))
                    .param("keyword", "校园风光")
                    .param("fileType", "IMAGE")
                    .param("storageType", "LOCAL")
                    .param("uploaderId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].id").value(mediaId));

            mockMvc.perform(get(accessUrl))
                .andExpect(status().isOk())
                .andExpect(content().bytes(bytes));

            mockMvc.perform(delete("/api/admin/media/{id}", mediaId).headers(auth(token)))
                .andExpect(status().isOk());
            mediaId = -1;

            mockMvc.perform(get(accessUrl)).andExpect(status().isNotFound());
        } finally {
            deleteMedia(token, mediaId);
        }
    }

    @Test
    void classifiesDocumentVideoAndOtherFiles() throws Exception {
        String token = login();
        List<Long> ids = new ArrayList<>();
        try {
            ids.add(upload(token, "招生简章.pdf", "application/pdf", new byte[] {1}, null));
            ids.add(upload(token, "校园视频.mp4", "video/mp4", new byte[] {2}, null));
            ids.add(upload(token, "资料包.zip", "application/zip", new byte[] {3}, null));

            mockMvc.perform(get("/api/admin/media/{id}", ids.get(0)).headers(auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileType").value("DOCUMENT"));
            mockMvc.perform(get("/api/admin/media/{id}", ids.get(1)).headers(auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileType").value("VIDEO"));
            mockMvc.perform(get("/api/admin/media/{id}", ids.get(2)).headers(auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileType").value("OTHER"));
        } finally {
            for (long id : ids) {
                deleteMedia(token, id);
            }
        }
    }

    @Test
    void rejectsEmptyOversizedDisallowedAndMismatchedFiles() throws Exception {
        String token = login();

        assertUploadRejected(token, "empty.png", "image/png", new byte[0], "上传文件不能为空");
        assertUploadRejected(token, "payload.exe", "application/octet-stream", new byte[] {1},
            "不允许上传该文件后缀：exe");
        assertUploadRejected(token, "fake.jpg", "text/plain", new byte[] {1},
            "文件 MIME 类型与后缀不匹配");
        assertUploadRejected(token, "large.pdf", "application/pdf", new byte[33],
            "上传文件大小不能超过 32B");
    }

    @Test
    void referencedMediaCannotBeDeletedAndOverridesAttachmentMetadata() throws Exception {
        String token = login();
        long mediaId = upload(token, "校历.pdf", "application/pdf", new byte[] {7, 8, 9}, null);
        long contentId = -1;
        try {
            JsonNode media = media(token, mediaId);
            Map<String, Object> attachment = new LinkedHashMap<>();
            attachment.put("mediaId", mediaId);
            attachment.put("fileName", "校历下载.pdf");
            attachment.put("fileUrl", "/tampered/file.exe");
            attachment.put("fileSize", 1);
            attachment.put("fileType", "OTHER");
            attachment.put("sortNo", 10);
            contentId = createContent(token, attachment);

            mockMvc.perform(get("/api/admin/contents/{id}", contentId).headers(auth(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attachments[0].mediaId").value(mediaId))
                .andExpect(jsonPath("$.data.attachments[0].fileUrl").value(media.path("accessUrl").stringValue()))
                .andExpect(jsonPath("$.data.attachments[0].fileSize").value(3))
                .andExpect(jsonPath("$.data.attachments[0].fileType").value("DOCUMENT"));

            mockMvc.perform(delete("/api/admin/media/{id}", mediaId).headers(auth(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.msg").value("媒体文件已被内容附件引用，不能删除"));
        } finally {
            deleteContent(token, contentId);
            deleteMedia(token, mediaId);
        }
    }

    @Test
    void endpointRequiresMediaAuthority() throws Exception {
        mockMvc.perform(get("/api/admin/media"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "cms:content")
    void unrelatedAuthorityCannotReadMedia() throws Exception {
        mockMvc.perform(get("/api/admin/media"))
            .andExpect(status().isForbidden());
    }

    private long upload(
        String token,
        String originalName,
        String mimeType,
        byte[] bytes,
        String remark
    ) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", originalName, mimeType, bytes);
        var request = multipart("/api/admin/media/upload")
            .file(file)
            .headers(auth(token));
        if (remark != null) {
            request.param("remark", remark);
        }
        MvcResult result = mockMvc.perform(request)
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data").longValue();
    }

    private void assertUploadRejected(
        String token,
        String originalName,
        String mimeType,
        byte[] bytes,
        String message
    ) throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", originalName, mimeType, bytes);
        mockMvc.perform(multipart("/api/admin/media/upload")
                .file(file)
                .headers(auth(token)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.msg").value(message));
    }

    private JsonNode media(String token, long id) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/admin/media/{id}", id).headers(auth(token)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data");
    }

    private long createContent(String token, Map<String, Object> attachment) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("columnId", 104L);
        body.put("title", "媒体引用内容 " + SEQUENCE.incrementAndGet());
        body.put("summary", "测试摘要");
        body.put("contentHtml", "<p>测试正文</p>");
        body.put("topFlag", false);
        body.put("recommendFlag", false);
        body.put("sortNo", 10);
        body.put("extensionData", Map.of());
        body.put("attachments", List.of(attachment));
        MvcResult result = mockMvc.perform(post("/api/admin/contents")
                .headers(auth(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
            .andExpect(status().isOk())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray()).path("data").longValue();
    }

    private void deleteMedia(String token, long id) throws Exception {
        if (id > 0) {
            mockMvc.perform(delete("/api/admin/media/{id}", id).headers(auth(token)));
        }
    }

    private void deleteContent(String token, long id) throws Exception {
        if (id > 0) {
            mockMvc.perform(delete("/api/admin/contents/{id}", id).headers(auth(token)));
        }
    }

    private String login() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/admin/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "username", "admin",
                    "password", "Admin@123456"
                ))))
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

    private static Path createStorageRoot() {
        try {
            return Files.createTempDirectory("school-backend-media-");
        } catch (IOException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
}
