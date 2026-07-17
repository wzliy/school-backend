package com.zlwang.school.modules.role.service;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.role.dto.AssignRolePermissionsRequest;
import com.zlwang.school.modules.role.dto.CreateRoleRequest;
import com.zlwang.school.modules.role.dto.RolePageQuery;
import com.zlwang.school.modules.role.dto.UpdateRoleRequest;
import com.zlwang.school.modules.role.model.SystemRole;
import com.zlwang.school.modules.role.repository.CreateSystemRole;
import com.zlwang.school.modules.role.repository.SystemRoleRepository;
import com.zlwang.school.modules.role.repository.UpdateSystemRole;
import com.zlwang.school.modules.role.vo.SystemRoleResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SystemRoleService {

    private static final String SUPER_ADMIN_CODE = "SUPER_ADMIN";

    private final SystemRoleRepository systemRoleRepository;

    public SystemRoleService(SystemRoleRepository systemRoleRepository) {
        this.systemRoleRepository = systemRoleRepository;
    }

    public PageResult<SystemRoleResponse> findPage(RolePageQuery query) {
        PageResult<SystemRole> page = systemRoleRepository.findPage(
            normalize(query.getRoleName()),
            normalizeCodeFilter(query.getRoleCode()),
            query.getStatus(),
            query.getPageNo(),
            query.getPageSize()
        );
        return PageResult.of(
            page.records().stream().map(SystemRoleResponse::from).toList(),
            page.total(),
            page.pageNo(),
            page.pageSize()
        );
    }

    public SystemRoleResponse findById(long id) {
        return SystemRoleResponse.from(requireRole(id));
    }

    public long create(CreateRoleRequest request, long operatorId) {
        String roleCode = request.roleCode().trim().toUpperCase(Locale.ROOT);
        if (systemRoleRepository.existsByCode(roleCode)) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在");
        }
        try {
            return systemRoleRepository.create(new CreateSystemRole(
                request.roleName().trim(),
                roleCode,
                request.status(),
                request.sortNo(),
                normalize(request.remark()),
                operatorId
            ));
        } catch (DataIntegrityViolationException | IllegalStateException exception) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色编码已存在");
        }
    }

    public void update(long id, UpdateRoleRequest request, long operatorId) {
        SystemRole role = requireRole(id);
        if (isSuperAdmin(role) && request.status() == 0) {
            throw protectedRole("不能禁用超级管理员角色");
        }
        if (!systemRoleRepository.update(new UpdateSystemRole(
            id,
            request.roleName().trim(),
            request.status(),
            request.sortNo(),
            normalize(request.remark()),
            operatorId
        ))) {
            throw notFound(id);
        }
    }

    public void assignPermissions(
        long id,
        AssignRolePermissionsRequest request,
        long operatorId
    ) {
        SystemRole role = requireRole(id);
        if (isSuperAdmin(role)) {
            throw protectedRole("不能修改超级管理员角色权限");
        }
        List<Long> permissionIds = request.permissionIds().stream().distinct().sorted().toList();
        if (!permissionIds.isEmpty()
            && systemRoleRepository.countExistingPermissions(permissionIds) != permissionIds.size()) {
            throw new BusinessException(
                ErrorCode.PARAM_VALIDATION_FAILED,
                "包含不存在或已禁用的权限"
            );
        }
        if (!systemRoleRepository.replacePermissions(id, permissionIds, operatorId)) {
            throw notFound(id);
        }
    }

    public void delete(long id, long operatorId) {
        SystemRole role = requireRole(id);
        if (isSuperAdmin(role)) {
            throw protectedRole("不能删除超级管理员角色");
        }
        if (systemRoleRepository.countAssignedUsers(id) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "角色仍被用户使用，不能删除");
        }
        if (!systemRoleRepository.delete(id, operatorId)) {
            throw notFound(id);
        }
    }

    private SystemRole requireRole(long id) {
        return systemRoleRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private boolean isSuperAdmin(SystemRole role) {
        return SUPER_ADMIN_CODE.equals(role.roleCode());
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String normalizeCodeFilter(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private BusinessException protectedRole(String message) {
        return new BusinessException(ErrorCode.BUSINESS_ERROR, message);
    }

    private BusinessException notFound(long id) {
        return new BusinessException(ErrorCode.NOT_FOUND, "角色不存在：" + id);
    }
}
