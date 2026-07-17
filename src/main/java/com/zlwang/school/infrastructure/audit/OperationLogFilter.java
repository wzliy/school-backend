package com.zlwang.school.infrastructure.audit;

import com.zlwang.school.modules.log.model.LogResultStatus;
import com.zlwang.school.modules.log.model.OperationModule;
import com.zlwang.school.modules.log.model.OperationType;
import com.zlwang.school.modules.log.repository.CreateOperationLog;
import com.zlwang.school.modules.log.repository.OperationLogRepository;
import com.zlwang.school.security.AuthenticatedUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import tools.jackson.databind.ObjectMapper;

public class OperationLogFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationLogFilter.class);
    private static final int REQUEST_CACHE_LIMIT = 8_192;

    private final OperationLogRepository operationLogRepository;
    private final OperationRequestSanitizer sanitizer;

    public OperationLogFilter(
        OperationLogRepository operationLogRepository,
        ObjectMapper objectMapper
    ) {
        this.operationLogRepository = operationLogRepository;
        this.sanitizer = new OperationRequestSanitizer(objectMapper);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        return !uri.startsWith("/api/admin/")
            || "GET".equals(method)
            || "HEAD".equals(method)
            || "OPTIONS".equals(method)
            || "/api/admin/auth/login".equals(uri);
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(
            request,
            REQUEST_CACHE_LIMIT
        );
        long startedAt = System.nanoTime();
        Throwable failure = null;
        try {
            filterChain.doFilter(wrapped, response);
        } catch (ServletException | IOException | RuntimeException ex) {
            failure = ex;
            throw ex;
        } finally {
            record(wrapped, response, failure, startedAt);
        }
    }

    private void record(
        ContentCachingRequestWrapper request,
        HttpServletResponse response,
        Throwable failure,
        long startedAt
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        String username = null;
        if (authentication != null && authentication.isAuthenticated()) {
            username = authentication.getName();
            if (authentication.getPrincipal() instanceof AuthenticatedUser user) {
                userId = user.id();
                username = user.getUsername();
            }
        }
        int status = response.getStatus();
        boolean successful = failure == null && status >= 200 && status < 300;
        try {
            operationLogRepository.create(new CreateOperationLog(
                userId,
                truncate(username, 64),
                module(request.getRequestURI()),
                operationType(request.getMethod(), request.getRequestURI()),
                request.getMethod(),
                truncate(request.getRequestURI(), 255),
                truncate(request.getRemoteAddr(), 64),
                sanitizer.summarize(request),
                successful ? LogResultStatus.SUCCESS : LogResultStatus.FAIL,
                successful ? null : errorMessage(failure, status),
                (System.nanoTime() - startedAt) / 1_000_000
            ));
        } catch (RuntimeException ex) {
            LOGGER.warn("Failed to persist operation audit log for {}", request.getRequestURI(), ex);
        }
    }

    private OperationModule module(String uri) {
        String segment = uri.substring("/api/admin/".length()).split("/", 2)[0];
        return switch (segment) {
            case "auth" -> OperationModule.AUTH;
            case "users" -> OperationModule.USER;
            case "roles" -> OperationModule.ROLE;
            case "permissions" -> OperationModule.PERMISSION;
            case "columns" -> OperationModule.COLUMN;
            case "contents" -> OperationModule.CONTENT;
            case "pages" -> OperationModule.PAGE;
            case "banners" -> OperationModule.BANNER;
            case "media" -> OperationModule.MEDIA;
            case "friend-links" -> OperationModule.FRIEND_LINK;
            case "site-config" -> OperationModule.SITE_CONFIG;
            case "seo" -> OperationModule.SEO;
            default -> OperationModule.UNKNOWN;
        };
    }

    private OperationType operationType(String method, String uri) {
        String path = uri.toLowerCase(Locale.ROOT);
        if (path.endsWith("/logout")) {
            return OperationType.LOGOUT;
        }
        if (path.endsWith("/upload")) {
            return OperationType.UPLOAD;
        }
        if (path.endsWith("/publish")) {
            return OperationType.PUBLISH;
        }
        if (path.endsWith("/offline")) {
            return OperationType.OFFLINE;
        }
        if (path.endsWith("/top")) {
            return OperationType.TOP;
        }
        if (path.endsWith("/recommend")) {
            return OperationType.RECOMMEND;
        }
        if (path.endsWith("/status")) {
            return OperationType.STATUS;
        }
        if (path.endsWith("/sort")) {
            return OperationType.SORT;
        }
        if (path.endsWith("/password")) {
            return OperationType.RESET_PASSWORD;
        }
        if (path.endsWith("/roles")) {
            return OperationType.ASSIGN_ROLES;
        }
        if (path.endsWith("/permissions")) {
            return OperationType.ASSIGN_PERMISSIONS;
        }
        return switch (method) {
            case "POST" -> OperationType.CREATE;
            case "PUT", "PATCH" -> OperationType.UPDATE;
            case "DELETE" -> OperationType.DELETE;
            default -> OperationType.OTHER;
        };
    }

    private String errorMessage(Throwable failure, int status) {
        if (failure == null) {
            return "HTTP " + status;
        }
        String message = failure.getMessage();
        return truncate(message == null ? failure.getClass().getSimpleName() : message, 1_000);
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
