package com.zlwang.school.modules.auth.model;

import java.util.List;

public record AuthUserAccount(
    long id,
    String username,
    String password,
    String realName,
    String avatarUrl,
    boolean enabled,
    List<String> roleCodes,
    List<AuthPermission> permissions
) {

    public AuthUserAccount {
        roleCodes = List.copyOf(roleCodes);
        permissions = List.copyOf(permissions);
    }
}
