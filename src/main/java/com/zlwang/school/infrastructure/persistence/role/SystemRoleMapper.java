package com.zlwang.school.infrastructure.persistence.role;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SystemRoleMapper {

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM sys_role
        WHERE deleted = 0
        <if test='roleName != null and roleName != ""'>
          AND role_name LIKE CONCAT('%', #{roleName}, '%')
        </if>
        <if test='roleCode != null and roleCode != ""'>
          AND role_code LIKE CONCAT('%', #{roleCode}, '%')
        </if>
        <if test='status != null'>
          AND status = #{status}
        </if>
        </script>
        """)
    long countRoles(
        @Param("roleName") String roleName,
        @Param("roleCode") String roleCode,
        @Param("status") Integer status
    );

    @Select("""
        <script>
        SELECT id, role_name, role_code, status, sort_no, remark, created_at, updated_at
        FROM sys_role
        WHERE deleted = 0
        <if test='roleName != null and roleName != ""'>
          AND role_name LIKE CONCAT('%', #{roleName}, '%')
        </if>
        <if test='roleCode != null and roleCode != ""'>
          AND role_code LIKE CONCAT('%', #{roleCode}, '%')
        </if>
        <if test='status != null'>
          AND status = #{status}
        </if>
        ORDER BY sort_no, id
        LIMIT #{limit} OFFSET #{offset}
        </script>
        """)
    List<SystemRoleRow> findRoles(
        @Param("roleName") String roleName,
        @Param("roleCode") String roleCode,
        @Param("status") Integer status,
        @Param("offset") long offset,
        @Param("limit") long limit
    );

    @Select("""
        SELECT id, role_name, role_code, status, sort_no, remark, created_at, updated_at
        FROM sys_role
        WHERE id = #{id}
          AND deleted = 0
        """)
    SystemRoleRow findById(@Param("id") long id);

    @Select("SELECT COUNT(*) FROM sys_role WHERE role_code = #{roleCode}")
    long countByCode(@Param("roleCode") String roleCode);

    @Select("""
        SELECT permission_id
        FROM sys_role_permission
        WHERE role_id = #{roleId}
          AND deleted = 0
        ORDER BY permission_id
        """)
    List<Long> findPermissionIdsByRoleId(@Param("roleId") long roleId);

    @Select("""
        SELECT id
        FROM sys_permission
        WHERE status = 1
          AND deleted = 0
        ORDER BY id
        """)
    List<Long> findAllPermissionIds();

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM sys_permission
        WHERE status = 1
          AND deleted = 0
          AND id IN
          <foreach collection='permissionIds' item='permissionId' open='(' separator=',' close=')'>
            #{permissionId}
          </foreach>
        </script>
        """)
    long countPermissions(@Param("permissionIds") List<Long> permissionIds);

    @Select("""
        SELECT COUNT(DISTINCT ur.user_id)
        FROM sys_user_role ur
        INNER JOIN sys_user u ON u.id = ur.user_id
        WHERE ur.role_id = #{roleId}
          AND ur.deleted = 0
          AND u.deleted = 0
        """)
    long countAssignedUsers(@Param("roleId") long roleId);

    @Insert("""
        INSERT INTO sys_role (
          role_name, role_code, status, sort_no, remark,
          created_by, updated_by, deleted
        ) VALUES (
          #{roleName}, #{roleCode}, #{status}, #{sortNo}, #{remark},
          #{operatorId}, #{operatorId}, 0
        )
        """)
    int insertRole(
        @Param("roleName") String roleName,
        @Param("roleCode") String roleCode,
        @Param("status") int status,
        @Param("sortNo") int sortNo,
        @Param("remark") String remark,
        @Param("operatorId") long operatorId
    );

    @Select("SELECT id FROM sys_role WHERE role_code = #{roleCode} AND deleted = 0")
    Long findIdByCode(@Param("roleCode") String roleCode);

    @Update("""
        UPDATE sys_role
        SET role_name = #{roleName},
            status = #{status},
            sort_no = #{sortNo},
            remark = #{remark},
            updated_by = #{operatorId}
        WHERE id = #{id}
          AND deleted = 0
        """)
    int updateRole(
        @Param("id") long id,
        @Param("roleName") String roleName,
        @Param("status") int status,
        @Param("sortNo") int sortNo,
        @Param("remark") String remark,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE sys_role
        SET status = 0,
            deleted = 1,
            updated_by = #{operatorId}
        WHERE id = #{id}
          AND deleted = 0
        """)
    int deleteRole(@Param("id") long id, @Param("operatorId") long operatorId);

    @Delete("DELETE FROM sys_role_permission WHERE role_id = #{roleId}")
    int deleteRolePermissions(@Param("roleId") long roleId);

    @Insert("""
        <script>
        INSERT INTO sys_role_permission (
          role_id, permission_id, created_by, updated_by, deleted
        ) VALUES
        <foreach collection='permissionIds' item='permissionId' separator=','>
          (#{roleId}, #{permissionId}, #{operatorId}, #{operatorId}, 0)
        </foreach>
        </script>
        """)
    int insertRolePermissions(
        @Param("roleId") long roleId,
        @Param("permissionIds") List<Long> permissionIds,
        @Param("operatorId") long operatorId
    );
}
