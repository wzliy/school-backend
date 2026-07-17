package com.zlwang.school.modules.log.service;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.log.dto.LoginLogPageQuery;
import com.zlwang.school.modules.log.dto.OperationLogPageQuery;
import com.zlwang.school.modules.log.model.LoginLog;
import com.zlwang.school.modules.log.model.OperationLog;
import com.zlwang.school.modules.log.repository.LoginLogRepository;
import com.zlwang.school.modules.log.repository.OperationLogRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SystemLogService {

    private final OperationLogRepository operationLogRepository;
    private final LoginLogRepository loginLogRepository;

    public SystemLogService(
        OperationLogRepository operationLogRepository,
        LoginLogRepository loginLogRepository
    ) {
        this.operationLogRepository = operationLogRepository;
        this.loginLogRepository = loginLogRepository;
    }

    public PageResult<OperationLog> findOperationLogs(OperationLogPageQuery query) {
        validateRange(query.getStartTime(), query.getEndTime());
        return operationLogRepository.findPage(
            normalize(query.getUsername()),
            query.getModuleName(),
            query.getOperationType(),
            query.getResultStatus(),
            query.getStartTime(),
            query.getEndTime(),
            query.getPageNo(),
            query.getPageSize()
        );
    }

    public PageResult<LoginLog> findLoginLogs(LoginLogPageQuery query) {
        validateRange(query.getStartTime(), query.getEndTime());
        return loginLogRepository.findPage(
            normalize(query.getUsername()),
            query.getLoginStatus(),
            query.getStartTime(),
            query.getEndTime(),
            query.getPageNo(),
            query.getPageSize()
        );
    }

    private void validateRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, "startTime 不能晚于 endTime");
        }
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
