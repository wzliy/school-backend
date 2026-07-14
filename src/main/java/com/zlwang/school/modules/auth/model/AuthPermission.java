package com.zlwang.school.modules.auth.model;

public record AuthPermission(
    long id,
    long parentId,
    String name,
    String code,
    String type,
    String routePath,
    String componentPath,
    String icon,
    int sortNo,
    boolean visible
) {
}
