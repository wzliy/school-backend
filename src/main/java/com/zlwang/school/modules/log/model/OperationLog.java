package com.zlwang.school.modules.log.model;

import java.time.LocalDateTime;

public record OperationLog(
    long id,
    Long userId,
    String username,
    OperationModule moduleName,
    OperationType operationType,
    String requestMethod,
    String requestUri,
    String requestIp,
    String requestParams,
    LogResultStatus resultStatus,
    String errorMessage,
    long costMs,
    LocalDateTime createdAt
) {
}
