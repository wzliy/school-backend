package com.zlwang.school.modules.role.repository;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.role.model.SystemRole;
import java.util.List;
import java.util.Optional;

public interface SystemRoleRepository {

    PageResult<SystemRole> findPage(
        String roleName,
        String roleCode,
        Integer status,
        long pageNo,
        long pageSize
    );

    Optional<SystemRole> findById(long id);

    boolean existsByCode(String roleCode);

    long countExistingPermissions(List<Long> permissionIds);

    long countAssignedUsers(long roleId);

    long create(CreateSystemRole command);

    boolean update(UpdateSystemRole command);

    boolean replacePermissions(long roleId, List<Long> permissionIds, long operatorId);

    boolean delete(long roleId, long operatorId);
}
