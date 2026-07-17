package com.zlwang.school.modules.log.repository;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.log.model.LogResultStatus;
import com.zlwang.school.modules.log.model.OperationLog;
import com.zlwang.school.modules.log.model.OperationModule;
import com.zlwang.school.modules.log.model.OperationType;
import java.time.LocalDateTime;

public interface OperationLogRepository {

    PageResult<OperationLog> findPage(
        String username,
        OperationModule moduleName,
        OperationType operationType,
        LogResultStatus resultStatus,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long pageNo,
        long pageSize
    );

    void create(CreateOperationLog command);
}
