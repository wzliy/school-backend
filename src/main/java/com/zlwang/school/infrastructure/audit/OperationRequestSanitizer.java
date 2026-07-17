package com.zlwang.school.infrastructure.audit;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;
import tools.jackson.databind.ObjectMapper;

public class OperationRequestSanitizer {

    private static final String REDACTED = "***";
    private static final int MAX_SUMMARY_LENGTH = 2_000;

    private final ObjectMapper objectMapper;

    public OperationRequestSanitizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String summarize(ContentCachingRequestWrapper request) {
        Map<String, Object> summary = new LinkedHashMap<>();
        Map<String, Object> parameters = sanitizeParameters(request);
        if (!parameters.isEmpty()) {
            summary.put("parameters", parameters);
        }
        Object body = sanitizeBody(request);
        if (body != null) {
            summary.put("body", body);
        }
        if (summary.isEmpty()) {
            return null;
        }
        try {
            return truncate(objectMapper.writeValueAsString(summary));
        } catch (Exception ex) {
            return "{\"summary\":\"unavailable\"}";
        }
    }

    private Map<String, Object> sanitizeParameters(HttpServletRequest request) {
        Map<String, Object> values = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, value) -> values.put(
            key,
            isSensitive(key) ? REDACTED : List.of(value)
        ));
        return values;
    }

    private Object sanitizeBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        String contentType = request.getContentType();
        if (!StringUtils.hasText(contentType)
            || !contentType.toLowerCase(Locale.ROOT).startsWith(MediaType.APPLICATION_JSON_VALUE)) {
            return Map.of("contentType", contentType == null ? "unknown" : contentType);
        }
        try {
            Charset charset = request.getCharacterEncoding() == null
                ? StandardCharsets.UTF_8
                : Charset.forName(request.getCharacterEncoding());
            Object value = objectMapper.readValue(new String(content, charset), Object.class);
            return sanitizeValue(value, null);
        } catch (Exception ex) {
            return Map.of("contentType", MediaType.APPLICATION_JSON_VALUE, "parseStatus", "INVALID");
        }
    }

    private Object sanitizeValue(Object value, String key) {
        if (key != null && isSensitive(key)) {
            return REDACTED;
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> sanitized = new LinkedHashMap<>();
            map.forEach((entryKey, entryValue) -> {
                String name = String.valueOf(entryKey);
                sanitized.put(name, sanitizeValue(entryValue, name));
            });
            return sanitized;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> sanitized = new ArrayList<>();
            iterable.forEach(item -> sanitized.add(sanitizeValue(item, key)));
            return sanitized;
        }
        return value;
    }

    private boolean isSensitive(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        return normalized.contains("password")
            || normalized.contains("secret")
            || normalized.contains("token")
            || normalized.contains("authorization")
            || normalized.contains("credential");
    }

    private String truncate(String value) {
        if (value.length() <= MAX_SUMMARY_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_SUMMARY_LENGTH);
    }
}
