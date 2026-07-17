package com.zlwang.school.infrastructure.persistence.role;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.infrastructure.persistence.local.LocalPermissionCatalog;
import com.zlwang.school.infrastructure.persistence.local.LocalUserStore;
import com.zlwang.school.modules.role.model.SystemRole;
import com.zlwang.school.modules.role.repository.CreateSystemRole;
import com.zlwang.school.modules.role.repository.SystemRoleRepository;
import com.zlwang.school.modules.role.repository.UpdateSystemRole;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@Profile("local")
public class LocalSystemRoleRepository implements SystemRoleRepository {

    private final LocalUserStore localUserStore;
    private final LocalPermissionCatalog permissionCatalog;

    public LocalSystemRoleRepository(
        LocalUserStore localUserStore,
        LocalPermissionCatalog permissionCatalog
    ) {
        this.localUserStore = localUserStore;
        this.permissionCatalog = permissionCatalog;
    }

    @Override
    public PageResult<SystemRole> findPage(
        String roleName,
        String roleCode,
        Integer status,
        long pageNo,
        long pageSize
    ) {
        String nameKeyword = normalizeKeyword(roleName);
        String codeKeyword = normalizeKeyword(roleCode);
        List<SystemRole> matched = localUserStore.findAllRoles().stream()
            .filter(role -> nameKeyword == null
                || role.name().toLowerCase(Locale.ROOT).contains(nameKeyword))
            .filter(role -> codeKeyword == null
                || role.code().toLowerCase(Locale.ROOT).contains(codeKeyword))
            .filter(role -> status == null || role.status() == status)
            .sorted(Comparator.comparingInt(LocalUserStore.LocalRole::sortNo)
                .thenComparingLong(LocalUserStore.LocalRole::id))
            .map(this::toSystemRole)
            .toList();
        long offset = (pageNo - 1) * pageSize;
        List<SystemRole> records = matched.stream().skip(offset).limit(pageSize).toList();
        return PageResult.of(records, matched.size(), pageNo, pageSize);
    }

    @Override
    public Optional<SystemRole> findById(long id) {
        return localUserStore.findRoleById(id).map(this::toSystemRole);
    }

    @Override
    public boolean existsByCode(String roleCode) {
        return localUserStore.roleCodeExists(roleCode);
    }

    @Override
    public long countExistingPermissions(List<Long> permissionIds) {
        return permissionCatalog.countExisting(permissionIds);
    }

    @Override
    public long countAssignedUsers(long roleId) {
        return localUserStore.countUsersByRoleId(roleId);
    }

    @Override
    public long create(CreateSystemRole command) {
        return localUserStore.createRole(
            command.roleName(),
            command.roleCode(),
            command.status(),
            command.sortNo(),
            command.remark()
        );
    }

    @Override
    public boolean update(UpdateSystemRole command) {
        return localUserStore.updateRole(
            command.id(),
            command.roleName(),
            command.status(),
            command.sortNo(),
            command.remark()
        );
    }

    @Override
    public boolean replacePermissions(long roleId, List<Long> permissionIds, long operatorId) {
        return localUserStore.replaceRolePermissions(roleId, permissionIds);
    }

    @Override
    public boolean delete(long roleId, long operatorId) {
        return localUserStore.deleteRole(roleId);
    }

    private SystemRole toSystemRole(LocalUserStore.LocalRole role) {
        List<Long> permissionIds = "SUPER_ADMIN".equals(role.code())
            ? permissionCatalog.allIds()
            : localUserStore.findPermissionIdsByRoleId(role.id());
        return new SystemRole(
            role.id(),
            role.name(),
            role.code(),
            role.status(),
            role.sortNo(),
            role.remark(),
            role.createdAt(),
            role.updatedAt(),
            permissionIds
        );
    }

    private String normalizeKeyword(String value) {
        return StringUtils.hasText(value) ? value.trim().toLowerCase(Locale.ROOT) : null;
    }
}
