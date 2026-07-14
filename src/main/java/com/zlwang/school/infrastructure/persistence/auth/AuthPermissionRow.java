package com.zlwang.school.infrastructure.persistence.auth;

public record AuthPermissionRow(
    long id,
    long parentId,
    String permissionName,
    String permissionCode,
    String permissionType,
    String routePath,
    String componentPath,
    String icon,
    int sortNo,
    int visible
) {
}
