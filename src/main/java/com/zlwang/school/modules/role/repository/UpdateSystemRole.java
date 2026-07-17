package com.zlwang.school.modules.role.repository;

public record UpdateSystemRole(
    long id,
    String roleName,
    int status,
    int sortNo,
    String remark,
    long operatorId
) {
}
