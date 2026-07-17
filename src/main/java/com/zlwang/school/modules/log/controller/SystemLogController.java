package com.zlwang.school.modules.log.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.log.dto.LoginLogPageQuery;
import com.zlwang.school.modules.log.dto.OperationLogPageQuery;
import com.zlwang.school.modules.log.model.LoginLog;
import com.zlwang.school.modules.log.model.OperationLog;
import com.zlwang.school.modules.log.service.SystemLogService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/logs")
public class SystemLogController {

    private final SystemLogService systemLogService;

    public SystemLogController(SystemLogService systemLogService) {
        this.systemLogService = systemLogService;
    }

    @GetMapping("/operations")
    @PreAuthorize("hasAuthority('log:operation')")
    public ApiResult<PageResult<OperationLog>> findOperationLogs(
        @Valid OperationLogPageQuery query
    ) {
        return ApiResult.success(systemLogService.findOperationLogs(query));
    }

    @GetMapping("/login")
    @PreAuthorize("hasAuthority('log:login')")
    public ApiResult<PageResult<LoginLog>> findLoginLogs(@Valid LoginLogPageQuery query) {
        return ApiResult.success(systemLogService.findLoginLogs(query));
    }
}
