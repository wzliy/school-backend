package com.zlwang.school.modules.role.dto;

import com.zlwang.school.common.pagination.PageQuery;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class RolePageQuery extends PageQuery {

    @Size(max = 64, message = "长度不能超过 64 个字符")
    private String roleName;

    @Size(max = 64, message = "长度不能超过 64 个字符")
    private String roleCode;

    @Min(value = 0, message = "只能为 0 或 1")
    @Max(value = 1, message = "只能为 0 或 1")
    private Integer status;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
