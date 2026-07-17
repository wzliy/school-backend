package com.zlwang.school.infrastructure.persistence.permission;

import java.time.LocalDateTime;

public record SystemPermissionRow(
    long id,
    long parentId,
    String permissionName,
    String permissionCode,
    String permissionType,
    String routePath,
    String componentPath,
    String icon,
    String apiMethod,
    String apiPath,
    int sortNo,
    int visible,
    int status,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
