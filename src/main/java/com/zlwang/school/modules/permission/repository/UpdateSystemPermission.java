package com.zlwang.school.modules.permission.repository;

public record UpdateSystemPermission(
    long id,
    long parentId,
    String permissionName,
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
