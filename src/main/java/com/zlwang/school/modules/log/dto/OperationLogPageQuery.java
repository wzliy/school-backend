package com.zlwang.school.modules.log.dto;

import com.zlwang.school.common.pagination.PageQuery;
import com.zlwang.school.modules.log.model.LogResultStatus;
import com.zlwang.school.modules.log.model.OperationModule;
import com.zlwang.school.modules.log.model.OperationType;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class OperationLogPageQuery extends PageQuery {

    @Size(max = 64, message = "账号长度不能超过 64 个字符")
    private String username;

    private OperationModule moduleName;

    private OperationType operationType;

    private LogResultStatus resultStatus;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public OperationModule getModuleName() {
        return moduleName;
    }

    public void setModuleName(OperationModule moduleName) {
        this.moduleName = moduleName;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public LogResultStatus getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(LogResultStatus resultStatus) {
        this.resultStatus = resultStatus;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
