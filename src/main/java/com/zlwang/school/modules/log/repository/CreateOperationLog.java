package com.zlwang.school.modules.log.repository;

import com.zlwang.school.modules.log.model.LogResultStatus;
import com.zlwang.school.modules.log.model.OperationModule;
import com.zlwang.school.modules.log.model.OperationType;

public record CreateOperationLog(
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
    long costMs
) {
}
