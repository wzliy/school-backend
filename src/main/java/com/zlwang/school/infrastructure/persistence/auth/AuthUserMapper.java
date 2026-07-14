package com.zlwang.school.infrastructure.persistence.auth;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthUserMapper {

    @Select("""
        SELECT id, username, password, real_name, avatar_url, status
        FROM sys_user
        WHERE username = #{username}
          AND deleted = 0
        LIMIT 1
        """)
    AuthUserRow findByUsername(@Param("username") String username);

    @Select("""
        SELECT r.role_code
        FROM sys_role r
        INNER JOIN sys_user_role ur ON ur.role_id = r.id
        WHERE ur.user_id = #{userId}
          AND ur.deleted = 0
          AND r.status = 1
          AND r.deleted = 0
        ORDER BY r.sort_no, r.id
        """)
    List<String> findRoleCodesByUserId(@Param("userId") long userId);

    @Select("""
        SELECT DISTINCT
          p.id,
          p.parent_id,
          p.permission_name,
          p.permission_code,
          p.permission_type,
          p.route_path,
          p.component_path,
          p.icon,
          p.sort_no,
          p.visible
        FROM sys_permission p
        INNER JOIN sys_role_permission rp ON rp.permission_id = p.id
        INNER JOIN sys_role r ON r.id = rp.role_id
        INNER JOIN sys_user_role ur ON ur.role_id = r.id
        WHERE ur.user_id = #{userId}
          AND ur.deleted = 0
          AND rp.deleted = 0
          AND r.status = 1
          AND r.deleted = 0
          AND p.status = 1
          AND p.deleted = 0
        ORDER BY p.sort_no, p.id
        """)
    List<AuthPermissionRow> findPermissionsByUserId(@Param("userId") long userId);
}
