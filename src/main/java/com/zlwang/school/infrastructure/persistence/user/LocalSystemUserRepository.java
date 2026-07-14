package com.zlwang.school.infrastructure.persistence.user;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.infrastructure.persistence.local.LocalUserStore;
import com.zlwang.school.modules.user.model.RoleOption;
import com.zlwang.school.modules.user.model.SystemUser;
import com.zlwang.school.modules.user.repository.CreateSystemUser;
import com.zlwang.school.modules.user.repository.SystemUserRepository;
import com.zlwang.school.modules.user.repository.UpdateSystemUser;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@Profile("local")
public class LocalSystemUserRepository implements SystemUserRepository {

    private final LocalUserStore localUserStore;

    public LocalSystemUserRepository(LocalUserStore localUserStore) {
        this.localUserStore = localUserStore;
    }

    @Override
    public PageResult<SystemUser> findPage(String username, Integer status, long pageNo, long pageSize) {
        String keyword = StringUtils.hasText(username) ? username.toLowerCase(Locale.ROOT) : null;
        List<SystemUser> matched = localUserStore.findAll().stream()
            .filter(user -> keyword == null || user.username().toLowerCase(Locale.ROOT).contains(keyword))
            .filter(user -> status == null || user.status() == status)
            .sorted(Comparator.comparingLong(LocalUserStore.LocalUser::id).reversed())
            .map(this::toSystemUser)
            .toList();
        long offset = (pageNo - 1) * pageSize;
        List<SystemUser> records = matched.stream()
            .skip(offset)
            .limit(pageSize)
            .toList();
        return PageResult.of(records, matched.size(), pageNo, pageSize);
    }

    @Override
    public Optional<SystemUser> findById(long id) {
        return localUserStore.findById(id).map(this::toSystemUser);
    }

    @Override
    public boolean existsByUsername(String username) {
        return localUserStore.usernameExists(username);
    }

    @Override
    public long countExistingRoles(List<Long> roleIds) {
        return localUserStore.countRoles(roleIds);
    }

    @Override
    public List<RoleOption> findRoleOptions() {
        return localUserStore.findRoles().stream()
            .map(role -> new RoleOption(role.id(), role.name(), role.code()))
            .toList();
    }

    @Override
    public long create(CreateSystemUser command) {
        return localUserStore.create(
            command.username(),
            command.passwordHash(),
            command.realName(),
            command.email(),
            command.phone(),
            command.status(),
            command.remark(),
            command.roleIds()
        );
    }

    @Override
    public boolean update(UpdateSystemUser command) {
        return localUserStore.update(
            command.id(),
            command.realName(),
            command.email(),
            command.phone(),
            command.remark()
        );
    }

    @Override
    public boolean updateStatus(long id, int status, long operatorId) {
        return localUserStore.updateStatus(id, status);
    }

    @Override
    public boolean updatePassword(long id, String passwordHash, long operatorId) {
        return localUserStore.updatePassword(id, passwordHash);
    }

    @Override
    public boolean replaceRoles(long id, List<Long> roleIds, long operatorId) {
        return localUserStore.replaceRoles(id, roleIds);
    }

    @Override
    public boolean delete(long id, long operatorId) {
        return localUserStore.delete(id);
    }

    private SystemUser toSystemUser(LocalUserStore.LocalUser user) {
        return new SystemUser(
            user.id(),
            user.username(),
            user.realName(),
            user.avatarUrl(),
            user.email(),
            user.phone(),
            user.status(),
            user.remark(),
            user.createdAt(),
            user.updatedAt(),
            localUserStore.findRoleIds(user.id())
        );
    }
}
