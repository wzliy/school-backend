package com.zlwang.school.modules.role.vo;

import com.zlwang.school.modules.role.model.SystemRole;
import java.time.LocalDateTime;
import java.util.List;

public record SystemRoleResponse(
    long id,
    String roleName,
    String roleCode,
    int status,
    int sortNo,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    List<Long> permissionIds
) {

    public SystemRoleResponse {
        permissionIds = List.copyOf(permissionIds);
    }

    public static SystemRoleResponse from(SystemRole role) {
        return new SystemRoleResponse(
            role.id(),
            role.roleName(),
            role.roleCode(),
            role.status(),
            role.sortNo(),
            role.remark(),
            role.createdAt(),
            role.updatedAt(),
            role.permissionIds()
        );
    }
}
