package com.zlwang.school.infrastructure.persistence.permission;

import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SystemPermissionMapper {

    @Select("""
        SELECT
          id, parent_id, permission_name, permission_code, permission_type,
          route_path, component_path, icon, api_method, api_path,
          sort_no, visible, status, remark, created_at, updated_at
        FROM sys_permission
        WHERE deleted = 0
        ORDER BY sort_no, id
        """)
    List<SystemPermissionRow> findAll();

    @Select("""
        SELECT
          id, parent_id, permission_name, permission_code, permission_type,
          route_path, component_path, icon, api_method, api_path,
          sort_no, visible, status, remark, created_at, updated_at
        FROM sys_permission
        WHERE id = #{id}
          AND deleted = 0
        """)
    SystemPermissionRow findById(@Param("id") long id);

    @Select("SELECT COUNT(*) FROM sys_permission WHERE permission_code = #{permissionCode}")
    long countByCode(@Param("permissionCode") String permissionCode);

    @Select("""
        SELECT COUNT(*)
        FROM sys_permission
        WHERE parent_id = #{permissionId}
          AND deleted = 0
        """)
    long countChildren(@Param("permissionId") long permissionId);

    @Select("""
        SELECT COUNT(DISTINCT rp.role_id)
        FROM sys_role_permission rp
        INNER JOIN sys_role r ON r.id = rp.role_id
        WHERE rp.permission_id = #{permissionId}
          AND rp.deleted = 0
          AND r.deleted = 0
          AND r.role_code <> 'SUPER_ADMIN'
        """)
    long countAssignedRoles(@Param("permissionId") long permissionId);

    @Insert("""
        INSERT INTO sys_permission (
          parent_id, permission_name, permission_code, permission_type,
          route_path, component_path, icon, api_method, api_path,
          sort_no, visible, status, remark, created_by, updated_by, deleted
        ) VALUES (
          #{parentId}, #{permissionName}, #{permissionCode}, #{permissionType},
          #{routePath}, #{componentPath}, #{icon}, #{apiMethod}, #{apiPath},
          #{sortNo}, #{visible}, #{status}, #{remark}, #{operatorId}, #{operatorId}, 0
        )
        """)
    int insertPermission(
        @Param("parentId") long parentId,
        @Param("permissionName") String permissionName,
        @Param("permissionCode") String permissionCode,
        @Param("permissionType") String permissionType,
        @Param("routePath") String routePath,
        @Param("componentPath") String componentPath,
        @Param("icon") String icon,
        @Param("apiMethod") String apiMethod,
        @Param("apiPath") String apiPath,
        @Param("sortNo") int sortNo,
        @Param("visible") boolean visible,
        @Param("status") int status,
        @Param("remark") String remark,
        @Param("operatorId") long operatorId
    );

    @Select("""
        SELECT id
        FROM sys_permission
        WHERE permission_code = #{permissionCode}
          AND deleted = 0
        """)
    Long findIdByCode(@Param("permissionCode") String permissionCode);

    @Update("""
        UPDATE sys_permission
        SET parent_id = #{parentId},
            permission_name = #{permissionName},
            route_path = #{routePath},
            component_path = #{componentPath},
            icon = #{icon},
            api_method = #{apiMethod},
            api_path = #{apiPath},
            sort_no = #{sortNo},
            visible = #{visible},
            status = #{status},
            remark = #{remark},
            updated_by = #{operatorId}
        WHERE id = #{id}
          AND deleted = 0
        """)
    int updatePermission(
        @Param("id") long id,
        @Param("parentId") long parentId,
        @Param("permissionName") String permissionName,
        @Param("routePath") String routePath,
        @Param("componentPath") String componentPath,
        @Param("icon") String icon,
        @Param("apiMethod") String apiMethod,
        @Param("apiPath") String apiPath,
        @Param("sortNo") int sortNo,
        @Param("visible") boolean visible,
        @Param("status") int status,
        @Param("remark") String remark,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE sys_permission
        SET status = 0,
            deleted = 1,
            updated_by = #{operatorId}
        WHERE id = #{id}
          AND deleted = 0
        """)
    int deletePermission(@Param("id") long id, @Param("operatorId") long operatorId);

    @Delete("DELETE FROM sys_role_permission WHERE permission_id = #{permissionId}")
    int deleteRolePermissions(@Param("permissionId") long permissionId);
}
