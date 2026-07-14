package com.zlwang.school.modules.user.repository;

public record UpdateSystemUser(
    long id,
    String realName,
    String email,
    String phone,
    String remark,
    long operatorId
) {
}
