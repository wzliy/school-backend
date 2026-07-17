package com.zlwang.school.modules.permission.repository;

import com.zlwang.school.modules.permission.model.SystemPermission;
import java.util.List;
import java.util.Optional;

public interface SystemPermissionRepository {

    List<SystemPermission> findAll();

    Optional<SystemPermission> findById(long id);

    boolean existsByCode(String permissionCode);

    long countChildren(long permissionId);

    long countAssignedRoles(long permissionId);

    long create(CreateSystemPermission command);

    boolean update(UpdateSystemPermission command);

    boolean delete(long permissionId, long operatorId);
}
