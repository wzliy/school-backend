package com.zlwang.school.infrastructure.persistence.local;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.log.model.LogResultStatus;
import com.zlwang.school.modules.log.model.LoginLog;
import com.zlwang.school.modules.log.model.LoginStatus;
import com.zlwang.school.modules.log.model.OperationLog;
import com.zlwang.school.modules.log.model.OperationModule;
import com.zlwang.school.modules.log.model.OperationType;
import com.zlwang.school.modules.log.repository.CreateLoginLog;
import com.zlwang.school.modules.log.repository.CreateOperationLog;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalLogStore {

    private final AtomicLong operationIdSequence = new AtomicLong();
    private final AtomicLong loginIdSequence = new AtomicLong();
    private final List<OperationLog> operationLogs = new ArrayList<>();
    private final List<LoginLog> loginLogs = new ArrayList<>();

    public synchronized PageResult<OperationLog> findOperationLogs(
        String username,
        OperationModule moduleName,
        OperationType operationType,
        LogResultStatus resultStatus,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long pageNo,
        long pageSize
    ) {
        String account = lower(username);
        List<OperationLog> matched = operationLogs.stream()
            .filter(log -> account == null || contains(log.username(), account))
            .filter(log -> moduleName == null || log.moduleName() == moduleName)
            .filter(log -> operationType == null || log.operationType() == operationType)
            .filter(log -> resultStatus == null || log.resultStatus() == resultStatus)
            .filter(log -> startTime == null || !log.createdAt().isBefore(startTime))
            .filter(log -> endTime == null || !log.createdAt().isAfter(endTime))
            .sorted(Comparator.comparing(OperationLog::createdAt).reversed()
                .thenComparing(OperationLog::id, Comparator.reverseOrder()))
            .toList();
        return page(matched, pageNo, pageSize);
    }

    public synchronized PageResult<LoginLog> findLoginLogs(
        String username,
        LoginStatus loginStatus,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long pageNo,
        long pageSize
    ) {
        String account = lower(username);
        List<LoginLog> matched = loginLogs.stream()
            .filter(log -> account == null || contains(log.username(), account))
            .filter(log -> loginStatus == null || log.loginStatus() == loginStatus)
            .filter(log -> startTime == null || !log.createdAt().isBefore(startTime))
            .filter(log -> endTime == null || !log.createdAt().isAfter(endTime))
            .sorted(Comparator.comparing(LoginLog::createdAt).reversed()
                .thenComparing(LoginLog::id, Comparator.reverseOrder()))
            .toList();
        return page(matched, pageNo, pageSize);
    }

    public synchronized void createOperationLog(CreateOperationLog command) {
        LocalDateTime now = LocalDateTime.now();
        operationLogs.add(new OperationLog(
            operationIdSequence.incrementAndGet(),
            command.userId(),
            command.username(),
            command.moduleName(),
            command.operationType(),
            command.requestMethod(),
            command.requestUri(),
            command.requestIp(),
            command.requestParams(),
            command.resultStatus(),
            command.errorMessage(),
            command.costMs(),
            now
        ));
    }

    public synchronized void createLoginLog(CreateLoginLog command) {
        loginLogs.add(new LoginLog(
            loginIdSequence.incrementAndGet(),
            command.userId(),
            command.username(),
            command.loginIp(),
            command.userAgent(),
            command.loginStatus(),
            command.failureReason(),
            LocalDateTime.now()
        ));
    }

    private <T> PageResult<T> page(List<T> values, long pageNo, long pageSize) {
        long offset = (pageNo - 1) * pageSize;
        if (offset >= values.size()) {
            return PageResult.empty(pageNo, pageSize);
        }
        int fromIndex = Math.toIntExact(offset);
        int toIndex = Math.min(values.size(), Math.toIntExact(offset + pageSize));
        return PageResult.of(values.subList(fromIndex, toIndex), values.size(), pageNo, pageSize);
    }

    private String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }
}
