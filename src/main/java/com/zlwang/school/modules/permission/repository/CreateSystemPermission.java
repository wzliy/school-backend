package com.zlwang.school.modules.permission.repository;

public record CreateSystemPermission(
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
    long operatorId
) {
}
