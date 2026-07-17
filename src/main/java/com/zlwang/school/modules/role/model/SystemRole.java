package com.zlwang.school.modules.role.model;

import java.time.LocalDateTime;
import java.util.List;

public record SystemRole(
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

    public SystemRole {
        permissionIds = List.copyOf(permissionIds);
    }
}
