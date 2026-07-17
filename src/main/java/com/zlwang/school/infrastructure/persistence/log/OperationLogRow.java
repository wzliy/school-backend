package com.zlwang.school.infrastructure.persistence.log;

import java.time.LocalDateTime;

public record OperationLogRow(
    long id,
    Long userId,
    String username,
    String moduleName,
    String operationType,
    String requestMethod,
    String requestUri,
    String requestIp,
    String requestParams,
    String resultStatus,
    String errorMessage,
    long costMs,
    LocalDateTime createdAt
) {
}
