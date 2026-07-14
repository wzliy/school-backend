package com.zlwang.school.modules.user.repository;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.user.model.RoleOption;
import com.zlwang.school.modules.user.model.SystemUser;
import java.util.List;
import java.util.Optional;

public interface SystemUserRepository {

    PageResult<SystemUser> findPage(String username, Integer status, long pageNo, long pageSize);

    Optional<SystemUser> findById(long id);

    boolean existsByUsername(String username);

    long countExistingRoles(List<Long> roleIds);

    List<RoleOption> findRoleOptions();

    long create(CreateSystemUser command);

    boolean update(UpdateSystemUser command);

    boolean updateStatus(long id, int status, long operatorId);

    boolean updatePassword(long id, String passwordHash, long operatorId);

    boolean replaceRoles(long id, List<Long> roleIds, long operatorId);

    boolean delete(long id, long operatorId);
}
