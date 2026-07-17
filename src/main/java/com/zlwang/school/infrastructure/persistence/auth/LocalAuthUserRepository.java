package com.zlwang.school.infrastructure.persistence.auth;

import com.zlwang.school.modules.auth.model.AuthUserAccount;
import com.zlwang.school.modules.auth.repository.AuthUserRepository;
import com.zlwang.school.infrastructure.persistence.local.LocalPermissionCatalog;
import com.zlwang.school.infrastructure.persistence.local.LocalUserStore;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class LocalAuthUserRepository implements AuthUserRepository {

    private final LocalUserStore localUserStore;
    private final LocalPermissionCatalog permissionCatalog;

    public LocalAuthUserRepository(
        LocalUserStore localUserStore,
        LocalPermissionCatalog permissionCatalog
    ) {
        this.localUserStore = localUserStore;
        this.permissionCatalog = permissionCatalog;
    }

    @Override
    public Optional<AuthUserAccount> findByUsername(String username) {
        return localUserStore.findByUsername(username)
            .map(user -> {
                var roleCodes = localUserStore.findRoleCodes(user.id());
                var permissions = roleCodes.contains("SUPER_ADMIN")
                    ? permissionCatalog.findAllActive()
                    : permissionCatalog.findByIds(localUserStore.findPermissionIdsByUserId(user.id()));
                return new AuthUserAccount(
                    user.id(),
                    user.username(),
                    user.passwordHash(),
                    user.realName(),
                    user.avatarUrl(),
                    user.status() == 1,
                    roleCodes,
                    permissions
                );
            });
    }
}
