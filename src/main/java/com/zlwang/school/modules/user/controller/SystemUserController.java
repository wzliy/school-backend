package com.zlwang.school.modules.user.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.user.dto.AssignUserRolesRequest;
import com.zlwang.school.modules.user.dto.CreateUserRequest;
import com.zlwang.school.modules.user.dto.ResetUserPasswordRequest;
import com.zlwang.school.modules.user.dto.UpdateUserRequest;
import com.zlwang.school.modules.user.dto.UpdateUserStatusRequest;
import com.zlwang.school.modules.user.dto.UserPageQuery;
import com.zlwang.school.modules.user.model.RoleOption;
import com.zlwang.school.modules.user.service.SystemUserService;
import com.zlwang.school.modules.user.vo.SystemUserResponse;
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
@RequestMapping("/api/admin/users")
public class SystemUserController {

    private final SystemUserService systemUserService;

    public SystemUserController(SystemUserService systemUserService) {
        this.systemUserService = systemUserService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system:user')")
    public ApiResult<PageResult<SystemUserResponse>> findPage(@Valid UserPageQuery query) {
        return ApiResult.success(systemUserService.findPage(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user')")
    public ApiResult<SystemUserResponse> findById(@Positive @PathVariable long id) {
        return ApiResult.success(systemUserService.findById(id));
    }

    @GetMapping("/role-options")
    @PreAuthorize("hasAuthority('system:user')")
    public ApiResult<List<RoleOption>> findRoleOptions() {
        return ApiResult.success(systemUserService.findRoleOptions());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:user:create')")
    public ApiResult<Long> create(
        @Valid @RequestBody CreateUserRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        return ApiResult.success(systemUserService.create(request, operator.id()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:update')")
    public ApiResult<Void> update(
        @Positive @PathVariable long id,
        @Valid @RequestBody UpdateUserRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        systemUserService.update(id, request, operator.id());
        return ApiResult.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:user:update')")
    public ApiResult<Void> updateStatus(
        @Positive @PathVariable long id,
        @Valid @RequestBody UpdateUserStatusRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        systemUserService.updateStatus(id, request, operator.id());
        return ApiResult.success();
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('system:user:update')")
    public ApiResult<Void> resetPassword(
        @Positive @PathVariable long id,
        @Valid @RequestBody ResetUserPasswordRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        systemUserService.resetPassword(id, request, operator.id());
        return ApiResult.success();
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:update')")
    public ApiResult<Void> assignRoles(
        @Positive @PathVariable long id,
        @Valid @RequestBody AssignUserRolesRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        systemUserService.assignRoles(id, request, operator.id());
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public ApiResult<Void> delete(
        @Positive @PathVariable long id,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        systemUserService.delete(id, operator.id());
        return ApiResult.success();
    }
}
