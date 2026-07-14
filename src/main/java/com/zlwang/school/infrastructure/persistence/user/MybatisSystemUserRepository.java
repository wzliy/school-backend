package com.zlwang.school.infrastructure.persistence.user;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.user.model.RoleOption;
import com.zlwang.school.modules.user.model.SystemUser;
import com.zlwang.school.modules.user.repository.CreateSystemUser;
import com.zlwang.school.modules.user.repository.SystemUserRepository;
import com.zlwang.school.modules.user.repository.UpdateSystemUser;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("!local")
public class MybatisSystemUserRepository implements SystemUserRepository {

    private final SystemUserMapper systemUserMapper;

    public MybatisSystemUserRepository(SystemUserMapper systemUserMapper) {
        this.systemUserMapper = systemUserMapper;
    }

    @Override
    public PageResult<SystemUser> findPage(String username, Integer status, long pageNo, long pageSize) {
        long total = systemUserMapper.countUsers(username, status);
        if (total == 0) {
            return PageResult.empty(pageNo, pageSize);
        }
        long offset = (pageNo - 1) * pageSize;
        List<SystemUser> users = systemUserMapper.findUsers(username, status, offset, pageSize)
            .stream()
            .map(this::toSystemUser)
            .toList();
        return PageResult.of(users, total, pageNo, pageSize);
    }

    @Override
    public Optional<SystemUser> findById(long id) {
        return Optional.ofNullable(systemUserMapper.findById(id)).map(this::toSystemUser);
    }

    @Override
    public boolean existsByUsername(String username) {
        return systemUserMapper.countByUsername(username) > 0;
    }

    @Override
    public long countExistingRoles(List<Long> roleIds) {
        return roleIds.isEmpty() ? 0 : systemUserMapper.countRoles(roleIds);
    }

    @Override
    public List<RoleOption> findRoleOptions() {
        return systemUserMapper.findRoleOptions();
    }

    @Override
    @Transactional
    public long create(CreateSystemUser command) {
        systemUserMapper.insertUser(
            command.username(),
            command.passwordHash(),
            command.realName(),
            command.email(),
            command.phone(),
            command.status(),
            command.remark(),
            command.operatorId()
        );
        Long userId = systemUserMapper.findIdByUsername(command.username());
        if (userId == null) {
            throw new IllegalStateException("新增用户后未查询到用户 ID");
        }
        insertRoles(userId, command.roleIds(), command.operatorId());
        return userId;
    }

    @Override
    public boolean update(UpdateSystemUser command) {
        return systemUserMapper.updateUser(
            command.id(),
            command.realName(),
            command.email(),
            command.phone(),
            command.remark(),
            command.operatorId()
        ) > 0;
    }

    @Override
    public boolean updateStatus(long id, int status, long operatorId) {
        return systemUserMapper.updateStatus(id, status, operatorId) > 0;
    }

    @Override
    public boolean updatePassword(long id, String passwordHash, long operatorId) {
        return systemUserMapper.updatePassword(id, passwordHash, operatorId) > 0;
    }

    @Override
    @Transactional
    public boolean replaceRoles(long id, List<Long> roleIds, long operatorId) {
        if (systemUserMapper.findById(id) == null) {
            return false;
        }
        systemUserMapper.deleteUserRoles(id);
        insertRoles(id, roleIds, operatorId);
        return true;
    }

    @Override
    @Transactional
    public boolean delete(long id, long operatorId) {
        if (systemUserMapper.deleteUser(id, operatorId) == 0) {
            return false;
        }
        systemUserMapper.deleteUserRoles(id);
        return true;
    }

    private SystemUser toSystemUser(SystemUserRow row) {
        return new SystemUser(
            row.id(),
            row.username(),
            row.realName(),
            row.avatarUrl(),
            row.email(),
            row.phone(),
            row.status(),
            row.remark(),
            row.createdAt(),
            row.updatedAt(),
            systemUserMapper.findRoleIdsByUserId(row.id())
        );
    }

    private void insertRoles(long userId, List<Long> roleIds, long operatorId) {
        if (!roleIds.isEmpty()) {
            systemUserMapper.insertUserRoles(userId, roleIds, operatorId);
        }
    }
}
