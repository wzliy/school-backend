package com.zlwang.school.infrastructure.persistence.user;

import com.zlwang.school.modules.user.model.RoleOption;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SystemUserMapper {

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM sys_user
        WHERE deleted = 0
        <if test='username != null and username != ""'>
          AND username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test='status != null'>
          AND status = #{status}
        </if>
        </script>
        """)
    long countUsers(@Param("username") String username, @Param("status") Integer status);

    @Select("""
        <script>
        SELECT id, username, real_name, avatar_url, email, phone, status, remark, created_at, updated_at
        FROM sys_user
        WHERE deleted = 0
        <if test='username != null and username != ""'>
          AND username LIKE CONCAT('%', #{username}, '%')
        </if>
        <if test='status != null'>
          AND status = #{status}
        </if>
        ORDER BY id DESC
        LIMIT #{limit} OFFSET #{offset}
        </script>
        """)
    List<SystemUserRow> findUsers(
        @Param("username") String username,
        @Param("status") Integer status,
        @Param("offset") long offset,
        @Param("limit") long limit
    );

    @Select("""
        SELECT id, username, real_name, avatar_url, email, phone, status, remark, created_at, updated_at
        FROM sys_user
        WHERE id = #{id}
          AND deleted = 0
        """)
    SystemUserRow findById(@Param("id") long id);

    @Select("""
        SELECT COUNT(*)
        FROM sys_user
        WHERE username = #{username}
          AND deleted = 0
        """)
    long countByUsername(@Param("username") String username);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM sys_role
        WHERE status = 1
          AND deleted = 0
          AND id IN
          <foreach collection='roleIds' item='roleId' open='(' separator=',' close=')'>
            #{roleId}
          </foreach>
        </script>
        """)
    long countRoles(@Param("roleIds") List<Long> roleIds);

    @Select("""
        SELECT id, role_name AS name, role_code AS code
        FROM sys_role
        WHERE status = 1
          AND deleted = 0
        ORDER BY sort_no, id
        """)
    List<RoleOption> findRoleOptions();

    @Select("""
        SELECT role_id
        FROM sys_user_role
        WHERE user_id = #{userId}
          AND deleted = 0
        ORDER BY role_id
        """)
    List<Long> findRoleIdsByUserId(@Param("userId") long userId);

    @Insert("""
        INSERT INTO sys_user (
          username, password, real_name, email, phone, status, remark,
          created_by, updated_by, deleted
        ) VALUES (
          #{username}, #{passwordHash}, #{realName}, #{email}, #{phone}, #{status}, #{remark},
          #{operatorId}, #{operatorId}, 0
        )
        """)
    int insertUser(
        @Param("username") String username,
        @Param("passwordHash") String passwordHash,
        @Param("realName") String realName,
        @Param("email") String email,
        @Param("phone") String phone,
        @Param("status") int status,
        @Param("remark") String remark,
        @Param("operatorId") long operatorId
    );

    @Select("""
        SELECT id
        FROM sys_user
        WHERE username = #{username}
          AND deleted = 0
        """)
    Long findIdByUsername(@Param("username") String username);

    @Update("""
        UPDATE sys_user
        SET real_name = #{realName},
            email = #{email},
            phone = #{phone},
            remark = #{remark},
            updated_by = #{operatorId}
        WHERE id = #{id}
          AND deleted = 0
        """)
    int updateUser(
        @Param("id") long id,
        @Param("realName") String realName,
        @Param("email") String email,
        @Param("phone") String phone,
        @Param("remark") String remark,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE sys_user
        SET status = #{status},
            updated_by = #{operatorId}
        WHERE id = #{id}
          AND deleted = 0
        """)
    int updateStatus(
        @Param("id") long id,
        @Param("status") int status,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE sys_user
        SET password = #{passwordHash},
            updated_by = #{operatorId}
        WHERE id = #{id}
          AND deleted = 0
        """)
    int updatePassword(
        @Param("id") long id,
        @Param("passwordHash") String passwordHash,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE sys_user
        SET status = 0,
            deleted = 1,
            updated_by = #{operatorId}
        WHERE id = #{id}
          AND deleted = 0
        """)
    int deleteUser(@Param("id") long id, @Param("operatorId") long operatorId);

    @Delete("DELETE FROM sys_user_role WHERE user_id = #{userId}")
    int deleteUserRoles(@Param("userId") long userId);

    @Insert("""
        <script>
        INSERT INTO sys_user_role (user_id, role_id, created_by, updated_by, deleted)
        VALUES
        <foreach collection='roleIds' item='roleId' separator=','>
          (#{userId}, #{roleId}, #{operatorId}, #{operatorId}, 0)
        </foreach>
        </script>
        """)
    int insertUserRoles(
        @Param("userId") long userId,
        @Param("roleIds") List<Long> roleIds,
        @Param("operatorId") long operatorId
    );
}
