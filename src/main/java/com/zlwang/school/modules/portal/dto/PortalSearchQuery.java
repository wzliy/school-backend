package com.zlwang.school.modules.portal.dto;

import com.zlwang.school.common.pagination.PageQuery;
import com.zlwang.school.modules.template.model.SiteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class PortalSearchQuery extends PageQuery {

    @NotBlank(message = "不能为空")
    @Size(max = 100, message = "长度不能超过 100 个字符")
    private String keyword;

    @NotNull(message = "不能为空")
    private SiteType siteType;

    @Positive(message = "栏目 ID 必须大于 0")
    private Long columnId;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public SiteType getSiteType() {
        return siteType;
    }

    public void setSiteType(SiteType siteType) {
        this.siteType = siteType;
    }

    public Long getColumnId() {
        return columnId;
    }

    public void setColumnId(Long columnId) {
        this.columnId = columnId;
    }
}
