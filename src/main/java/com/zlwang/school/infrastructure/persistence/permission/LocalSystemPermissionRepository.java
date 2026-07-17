package com.zlwang.school.infrastructure.persistence.permission;

import com.zlwang.school.infrastructure.persistence.local.LocalPermissionCatalog;
import com.zlwang.school.infrastructure.persistence.local.LocalUserStore;
import com.zlwang.school.modules.permission.model.SystemPermission;
import com.zlwang.school.modules.permission.repository.CreateSystemPermission;
import com.zlwang.school.modules.permission.repository.SystemPermissionRepository;
import com.zlwang.school.modules.permission.repository.UpdateSystemPermission;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class LocalSystemPermissionRepository implements SystemPermissionRepository {

    private final LocalPermissionCatalog permissionCatalog;
    private final LocalUserStore localUserStore;

    public LocalSystemPermissionRepository(
        LocalPermissionCatalog permissionCatalog,
        LocalUserStore localUserStore
    ) {
        this.permissionCatalog = permissionCatalog;
        this.localUserStore = localUserStore;
    }

    @Override
    public List<SystemPermission> findAll() {
        return permissionCatalog.findAll().stream().map(this::toSystemPermission).toList();
    }

    @Override
    public Optional<SystemPermission> findById(long id) {
        return permissionCatalog.findById(id).map(this::toSystemPermission);
    }

    @Override
    public boolean existsByCode(String permissionCode) {
        return permissionCatalog.codeExists(permissionCode);
    }

    @Override
    public long countChildren(long permissionId) {
        return permissionCatalog.countChildren(permissionId);
    }

    @Override
    public long countAssignedRoles(long permissionId) {
        return localUserStore.countNonSuperRolesByPermissionId(permissionId);
    }

    @Override
    public long create(CreateSystemPermission command) {
        return permissionCatalog.create(
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
            command.remark()
        );
    }

    @Override
    public boolean update(UpdateSystemPermission command) {
        return permissionCatalog.update(
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
            command.remark()
        );
    }

    @Override
    public boolean delete(long permissionId, long operatorId) {
        if (!permissionCatalog.delete(permissionId)) {
            return false;
        }
        localUserStore.removePermissionFromRoles(permissionId);
        return true;
    }

    private SystemPermission toSystemPermission(LocalPermissionCatalog.LocalPermission permission) {
        return new SystemPermission(
            permission.id(),
            permission.parentId(),
            permission.name(),
            permission.code(),
            permission.type(),
            permission.routePath(),
            permission.componentPath(),
            permission.icon(),
            permission.apiMethod(),
            permission.apiPath(),
            permission.sortNo(),
            permission.visible(),
            permission.status(),
            permission.remark(),
            permission.createdAt(),
            permission.updatedAt()
        );
    }
}
