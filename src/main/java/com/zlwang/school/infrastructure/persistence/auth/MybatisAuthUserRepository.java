package com.zlwang.school.infrastructure.persistence.auth;

import com.zlwang.school.modules.auth.model.AuthPermission;
import com.zlwang.school.modules.auth.model.AuthUserAccount;
import com.zlwang.school.modules.auth.repository.AuthUserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!local")
public class MybatisAuthUserRepository implements AuthUserRepository {

    private final AuthUserMapper authUserMapper;

    public MybatisAuthUserRepository(AuthUserMapper authUserMapper) {
        this.authUserMapper = authUserMapper;
    }

    @Override
    public Optional<AuthUserAccount> findByUsername(String username) {
        AuthUserRow user = authUserMapper.findByUsername(username);
        if (user == null) {
            return Optional.empty();
        }

        List<String> roleCodes = authUserMapper.findRoleCodesByUserId(user.id());
        List<AuthPermission> permissions = authUserMapper.findPermissionsByUserId(user.id())
            .stream()
            .map(this::toPermission)
            .toList();

        return Optional.of(new AuthUserAccount(
            user.id(),
            user.username(),
            user.password(),
            user.realName(),
            user.avatarUrl(),
            user.status() == 1,
            roleCodes,
            permissions
        ));
    }

    private AuthPermission toPermission(AuthPermissionRow permission) {
        return new AuthPermission(
            permission.id(),
            permission.parentId(),
            permission.permissionName(),
            permission.permissionCode(),
            permission.permissionType(),
            permission.routePath(),
            permission.componentPath(),
            permission.icon(),
            permission.sortNo(),
            permission.visible() == 1
        );
    }
}
