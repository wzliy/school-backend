package com.zlwang.school.infrastructure.persistence.log;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.log.model.LogResultStatus;
import com.zlwang.school.modules.log.model.OperationLog;
import com.zlwang.school.modules.log.model.OperationModule;
import com.zlwang.school.modules.log.model.OperationType;
import com.zlwang.school.modules.log.repository.CreateOperationLog;
import com.zlwang.school.modules.log.repository.OperationLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!local")
public class MybatisOperationLogRepository implements OperationLogRepository {

    private final OperationLogMapper operationLogMapper;

    public MybatisOperationLogRepository(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
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
        String module = name(moduleName);
        String operation = name(operationType);
        String result = name(resultStatus);
        long total = operationLogMapper.countLogs(
            username,
            module,
            operation,
            result,
            startTime,
            endTime
        );
        if (total == 0) {
            return PageResult.empty(pageNo, pageSize);
        }
        List<OperationLog> records = operationLogMapper.findLogs(
            username,
            module,
            operation,
            result,
            startTime,
            endTime,
            (pageNo - 1) * pageSize,
            pageSize
        ).stream().map(this::toLog).toList();
        return PageResult.of(records, total, pageNo, pageSize);
    }

    @Override
    public void create(CreateOperationLog command) {
        operationLogMapper.insert(
            command,
            command.moduleName().name(),
            command.operationType().name(),
            command.resultStatus().name()
        );
    }

    private OperationLog toLog(OperationLogRow row) {
        return new OperationLog(
            row.id(),
            row.userId(),
            row.username(),
            OperationModule.valueOf(row.moduleName()),
            OperationType.valueOf(row.operationType()),
            row.requestMethod(),
            row.requestUri(),
            row.requestIp(),
            row.requestParams(),
            LogResultStatus.valueOf(row.resultStatus()),
            row.errorMessage(),
            row.costMs(),
            row.createdAt()
        );
    }

    private String name(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
