package com.zlwang.school.modules.user.service;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.user.dto.AssignUserRolesRequest;
import com.zlwang.school.modules.user.dto.CreateUserRequest;
import com.zlwang.school.modules.user.dto.ResetUserPasswordRequest;
import com.zlwang.school.modules.user.dto.UpdateUserRequest;
import com.zlwang.school.modules.user.dto.UpdateUserStatusRequest;
import com.zlwang.school.modules.user.dto.UserPageQuery;
import com.zlwang.school.modules.user.model.RoleOption;
import com.zlwang.school.modules.user.repository.CreateSystemUser;
import com.zlwang.school.modules.user.repository.SystemUserRepository;
import com.zlwang.school.modules.user.repository.UpdateSystemUser;
import com.zlwang.school.modules.user.vo.SystemUserResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SystemUserService {

    private final SystemUserRepository systemUserRepository;
    private final PasswordEncoder passwordEncoder;

    public SystemUserService(SystemUserRepository systemUserRepository, PasswordEncoder passwordEncoder) {
        this.systemUserRepository = systemUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public PageResult<SystemUserResponse> findPage(UserPageQuery query) {
        PageResult<com.zlwang.school.modules.user.model.SystemUser> page = systemUserRepository.findPage(
            normalize(query.getUsername()),
            query.getStatus(),
            query.getPageNo(),
            query.getPageSize()
        );
        return PageResult.of(
            page.records().stream().map(SystemUserResponse::from).toList(),
            page.total(),
            page.pageNo(),
            page.pageSize()
        );
    }

    public SystemUserResponse findById(long id) {
        return systemUserRepository.findById(id)
            .map(SystemUserResponse::from)
            .orElseThrow(() -> notFound(id));
    }

    public List<RoleOption> findRoleOptions() {
        return systemUserRepository.findRoleOptions();
    }

    public long create(CreateUserRequest request, long operatorId) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        if (systemUserRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        List<Long> roleIds = normalizeRoleIds(request.roleIds());
        validateRoleIds(roleIds);
        try {
            return systemUserRepository.create(new CreateSystemUser(
                username,
                passwordEncoder.encode(request.password()),
                request.realName().trim(),
                normalize(request.email()),
                normalize(request.phone()),
                request.status(),
                normalize(request.remark()),
                roleIds,
                operatorId
            ));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
    }

    public void update(long id, UpdateUserRequest request, long operatorId) {
        boolean updated = systemUserRepository.update(new UpdateSystemUser(
            id,
            request.realName().trim(),
            normalize(request.email()),
            normalize(request.phone()),
            normalize(request.remark()),
            operatorId
        ));
        if (!updated) {
            throw notFound(id);
        }
    }

    public void updateStatus(long id, UpdateUserStatusRequest request, long operatorId) {
        if (id == operatorId && request.status() == 0) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "不能禁用当前登录账号");
        }
        if (!systemUserRepository.updateStatus(id, request.status(), operatorId)) {
            throw notFound(id);
        }
    }

    public void resetPassword(long id, ResetUserPasswordRequest request, long operatorId) {
        if (!systemUserRepository.updatePassword(id, passwordEncoder.encode(request.password()), operatorId)) {
            throw notFound(id);
        }
    }

    public void assignRoles(long id, AssignUserRolesRequest request, long operatorId) {
        List<Long> roleIds = normalizeRoleIds(request.roleIds());
        validateRoleIds(roleIds);
        if (id == operatorId && !containsSuperAdminRole(roleIds)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "不能移除当前账号的超级管理员角色");
        }
        if (!systemUserRepository.replaceRoles(id, roleIds, operatorId)) {
            throw notFound(id);
        }
    }

    public void delete(long id, long operatorId) {
        if (id == operatorId) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "不能删除当前登录账号");
        }
        if (!systemUserRepository.delete(id, operatorId)) {
            throw notFound(id);
        }
    }

    private void validateRoleIds(List<Long> roleIds) {
        if (roleIds.isEmpty()) {
            return;
        }
        if (systemUserRepository.countExistingRoles(roleIds) != roleIds.size()) {
            throw new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, "包含不存在或已禁用的角色");
        }
    }

    private List<Long> normalizeRoleIds(List<Long> roleIds) {
        return roleIds.stream().distinct().sorted().toList();
    }

    private boolean containsSuperAdminRole(List<Long> roleIds) {
        return systemUserRepository.findRoleOptions().stream()
            .anyMatch(role -> "SUPER_ADMIN".equals(role.code()) && roleIds.contains(role.id()));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private BusinessException notFound(long id) {
        return new BusinessException(ErrorCode.NOT_FOUND, "用户不存在：" + id);
    }
}
