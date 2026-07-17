package com.zlwang.school.infrastructure.persistence.permission;

import com.zlwang.school.modules.permission.model.SystemPermission;
import com.zlwang.school.modules.permission.repository.CreateSystemPermission;
import com.zlwang.school.modules.permission.repository.SystemPermissionRepository;
import com.zlwang.school.modules.permission.repository.UpdateSystemPermission;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("!local")
public class MybatisSystemPermissionRepository implements SystemPermissionRepository {

    private final SystemPermissionMapper systemPermissionMapper;

    public MybatisSystemPermissionRepository(SystemPermissionMapper systemPermissionMapper) {
        this.systemPermissionMapper = systemPermissionMapper;
    }

    @Override
    public List<SystemPermission> findAll() {
        return systemPermissionMapper.findAll().stream().map(this::toSystemPermission).toList();
    }

    @Override
    public Optional<SystemPermission> findById(long id) {
        return Optional.ofNullable(systemPermissionMapper.findById(id)).map(this::toSystemPermission);
    }

    @Override
    public boolean existsByCode(String permissionCode) {
        return systemPermissionMapper.countByCode(permissionCode) > 0;
    }

    @Override
    public long countChildren(long permissionId) {
        return systemPermissionMapper.countChildren(permissionId);
    }

    @Override
    public long countAssignedRoles(long permissionId) {
        return systemPermissionMapper.countAssignedRoles(permissionId);
    }

    @Override
    @Transactional
    public long create(CreateSystemPermission command) {
        systemPermissionMapper.insertPermission(
            command.parentId(),
            command.permissionName(),
            command.permissionCode(),
            command.permissionType(),
            command.routePath(),
            command.componentPath(),
            command.icon(),
            command.apiMethod(),
            command.apiPath(),
            command.sortNo(),
            command.visible(),
            command.status(),
            command.remark(),
            command.operatorId()
        );
        Long permissionId = systemPermissionMapper.findIdByCode(command.permissionCode());
        if (permissionId == null) {
            throw new IllegalStateException("新增权限后未查询到权限 ID");
        }
        return permissionId;
    }

    @Override
    public boolean update(UpdateSystemPermission command) {
        return systemPermissionMapper.updatePermission(
            command.id(),
            command.parentId(),
            command.permissionName(),
            command.routePath(),
            command.componentPath(),
            command.icon(),
            command.apiMethod(),
            command.apiPath(),
            command.sortNo(),
            command.visible(),
            command.status(),
            command.remark(),
            command.operatorId()
        ) > 0;
    }

    @Override
    @Transactional
    public boolean delete(long permissionId, long operatorId) {
        if (systemPermissionMapper.deletePermission(permissionId, operatorId) == 0) {
            return false;
        }
        systemPermissionMapper.deleteRolePermissions(permissionId);
        return true;
    }

    private SystemPermission toSystemPermission(SystemPermissionRow row) {
        return new SystemPermission(
            row.id(),
            row.parentId(),
            row.permissionName(),
            row.permissionCode(),
            row.permissionType(),
            row.routePath(),
            row.componentPath(),
            row.icon(),
            row.apiMethod(),
            row.apiPath(),
            row.sortNo(),
            row.visible() == 1,
            row.status(),
            row.remark(),
            row.createdAt(),
            row.updatedAt()
        );
    }
}
