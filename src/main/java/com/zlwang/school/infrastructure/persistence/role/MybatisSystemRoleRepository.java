package com.zlwang.school.infrastructure.persistence.role;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.role.model.SystemRole;
import com.zlwang.school.modules.role.repository.CreateSystemRole;
import com.zlwang.school.modules.role.repository.SystemRoleRepository;
import com.zlwang.school.modules.role.repository.UpdateSystemRole;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("!local")
public class MybatisSystemRoleRepository implements SystemRoleRepository {

    private final SystemRoleMapper systemRoleMapper;

    public MybatisSystemRoleRepository(SystemRoleMapper systemRoleMapper) {
        this.systemRoleMapper = systemRoleMapper;
    }

    @Override
    public PageResult<SystemRole> findPage(
        String roleName,
        String roleCode,
        Integer status,
        long pageNo,
        long pageSize
    ) {
        long total = systemRoleMapper.countRoles(roleName, roleCode, status);
        if (total == 0) {
            return PageResult.empty(pageNo, pageSize);
        }
        long offset = (pageNo - 1) * pageSize;
        List<SystemRole> roles = systemRoleMapper.findRoles(
                roleName,
                roleCode,
                status,
                offset,
                pageSize
            ).stream()
            .map(this::toSystemRole)
            .toList();
        return PageResult.of(roles, total, pageNo, pageSize);
    }

    @Override
    public Optional<SystemRole> findById(long id) {
        return Optional.ofNullable(systemRoleMapper.findById(id)).map(this::toSystemRole);
    }

    @Override
    public boolean existsByCode(String roleCode) {
        return systemRoleMapper.countByCode(roleCode) > 0;
    }

    @Override
    public long countExistingPermissions(List<Long> permissionIds) {
        return permissionIds.isEmpty() ? 0 : systemRoleMapper.countPermissions(permissionIds);
    }

    @Override
    public long countAssignedUsers(long roleId) {
        return systemRoleMapper.countAssignedUsers(roleId);
    }

    @Override
    @Transactional
    public long create(CreateSystemRole command) {
        systemRoleMapper.insertRole(
            command.roleName(),
            command.roleCode(),
            command.status(),
            command.sortNo(),
            command.remark(),
            command.operatorId()
        );
        Long roleId = systemRoleMapper.findIdByCode(command.roleCode());
        if (roleId == null) {
            throw new IllegalStateException("新增角色后未查询到角色 ID");
        }
        return roleId;
    }

    @Override
    public boolean update(UpdateSystemRole command) {
        return systemRoleMapper.updateRole(
            command.id(),
            command.roleName(),
            command.status(),
            command.sortNo(),
            command.remark(),
            command.operatorId()
        ) > 0;
    }

    @Override
    @Transactional
    public boolean replacePermissions(long roleId, List<Long> permissionIds, long operatorId) {
        if (systemRoleMapper.findById(roleId) == null) {
            return false;
        }
        systemRoleMapper.deleteRolePermissions(roleId);
        if (!permissionIds.isEmpty()) {
            systemRoleMapper.insertRolePermissions(roleId, permissionIds, operatorId);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean delete(long roleId, long operatorId) {
        if (systemRoleMapper.deleteRole(roleId, operatorId) == 0) {
            return false;
        }
        systemRoleMapper.deleteRolePermissions(roleId);
        return true;
    }

    private SystemRole toSystemRole(SystemRoleRow row) {
        List<Long> permissionIds = "SUPER_ADMIN".equals(row.roleCode())
            ? systemRoleMapper.findAllPermissionIds()
            : systemRoleMapper.findPermissionIdsByRoleId(row.id());
        return new SystemRole(
            row.id(),
            row.roleName(),
            row.roleCode(),
            row.status(),
            row.sortNo(),
            row.remark(),
            row.createdAt(),
            row.updatedAt(),
            permissionIds
        );
    }
}
