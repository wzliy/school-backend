package com.zlwang.school.modules.permission.model;

import java.time.LocalDateTime;

public record SystemPermission(
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
    boolean visible,
    int status,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
