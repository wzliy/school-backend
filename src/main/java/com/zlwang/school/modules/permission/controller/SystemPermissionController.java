package com.zlwang.school.modules.permission.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.modules.permission.dto.CreatePermissionRequest;
import com.zlwang.school.modules.permission.dto.UpdatePermissionRequest;
import com.zlwang.school.modules.permission.service.SystemPermissionService;
import com.zlwang.school.modules.permission.vo.PermissionTreeNode;
import com.zlwang.school.modules.permission.vo.SystemPermissionResponse;
import com.zlwang.school.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/permissions")
public class SystemPermissionController {

    private final SystemPermissionService systemPermissionService;

    public SystemPermissionController(SystemPermissionService systemPermissionService) {
        this.systemPermissionService = systemPermissionService;
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:permission')")
    public ApiResult<List<PermissionTreeNode>> findTree() {
        return ApiResult.success(systemPermissionService.findTree());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:permission')")
    public ApiResult<SystemPermissionResponse> findById(@Positive @PathVariable long id) {
        return ApiResult.success(systemPermissionService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:permission:manage')")
    public ApiResult<Long> create(
        @Valid @RequestBody CreatePermissionRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        return ApiResult.success(systemPermissionService.create(request, operator.id()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:permission:manage')")
    public ApiResult<Void> update(
        @Positive @PathVariable long id,
        @Valid @RequestBody UpdatePermissionRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        systemPermissionService.update(id, request, operator.id());
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:permission:manage')")
    public ApiResult<Void> delete(
        @Positive @PathVariable long id,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        systemPermissionService.delete(id, operator.id());
        return ApiResult.success();
    }
}
