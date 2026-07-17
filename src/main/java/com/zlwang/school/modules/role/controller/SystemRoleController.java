package com.zlwang.school.modules.role.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.role.dto.AssignRolePermissionsRequest;
import com.zlwang.school.modules.role.dto.CreateRoleRequest;
import com.zlwang.school.modules.role.dto.RolePageQuery;
import com.zlwang.school.modules.role.dto.UpdateRoleRequest;
import com.zlwang.school.modules.role.service.SystemRoleService;
import com.zlwang.school.modules.role.vo.SystemRoleResponse;
import com.zlwang.school.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
@RequestMapping("/api/admin/roles")
public class SystemRoleController {

    private final SystemRoleService systemRoleService;

    public SystemRoleController(SystemRoleService systemRoleService) {
        this.systemRoleService = systemRoleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:role')")
    public ApiResult<PageResult<SystemRoleResponse>> findPage(@Valid RolePageQuery query) {
        return ApiResult.success(systemRoleService.findPage(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role')")
    public ApiResult<SystemRoleResponse> findById(@Positive @PathVariable long id) {
        return ApiResult.success(systemRoleService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:role:manage')")
    public ApiResult<Long> create(
        @Valid @RequestBody CreateRoleRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        return ApiResult.success(systemRoleService.create(request, operator.id()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:manage')")
    public ApiResult<Void> update(
        @Positive @PathVariable long id,
        @Valid @RequestBody UpdateRoleRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        systemRoleService.update(id, request, operator.id());
        return ApiResult.success();
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:manage')")
    public ApiResult<Void> assignPermissions(
        @Positive @PathVariable long id,
        @Valid @RequestBody AssignRolePermissionsRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        systemRoleService.assignPermissions(id, request, operator.id());
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:manage')")
    public ApiResult<Void> delete(
        @Positive @PathVariable long id,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        systemRoleService.delete(id, operator.id());
        return ApiResult.success();
    }
}
