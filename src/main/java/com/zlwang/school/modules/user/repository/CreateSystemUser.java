package com.zlwang.school.modules.user.repository;

import java.util.List;

public record CreateSystemUser(
    String username,
    String passwordHash,
    String realName,
    String email,
    String phone,
    int status,
    String remark,
    List<Long> roleIds,
    long operatorId
) {

    public CreateSystemUser {
        roleIds = List.copyOf(roleIds);
    }
}
