package com.zlwang.school.modules.role.repository;

public record CreateSystemRole(
    String roleName,
    String roleCode,
    int status,
    int sortNo,
    String remark,
    long operatorId
) {
}
