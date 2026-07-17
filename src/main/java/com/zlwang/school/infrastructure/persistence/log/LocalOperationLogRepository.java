package com.zlwang.school.infrastructure.persistence.log;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.infrastructure.persistence.local.LocalLogStore;
import com.zlwang.school.modules.log.model.LogResultStatus;
import com.zlwang.school.modules.log.model.OperationLog;
import com.zlwang.school.modules.log.model.OperationModule;
import com.zlwang.school.modules.log.model.OperationType;
import com.zlwang.school.modules.log.repository.CreateOperationLog;
import com.zlwang.school.modules.log.repository.OperationLogRepository;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class LocalOperationLogRepository implements OperationLogRepository {

    private final LocalLogStore localLogStore;

    public LocalOperationLogRepository(LocalLogStore localLogStore) {
        this.localLogStore = localLogStore;
    }

    @Override
    public PageResult<OperationLog> findPage(
        String username,
        OperationModule moduleName,
        OperationType operationType,
        LogResultStatus resultStatus,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long pageNo,
        long pageSize
    ) {
        return localLogStore.findOperationLogs(
            username,
            moduleName,
            operationType,
            resultStatus,
            startTime,
            endTime,
            pageNo,
            pageSize
        );
    }

    @Override
    public void create(CreateOperationLog command) {
        localLogStore.createOperationLog(command);
    }
}
