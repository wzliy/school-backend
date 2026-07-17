package com.zlwang.school.modules.content.dto;

import com.zlwang.school.common.pagination.PageQuery;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.template.model.SiteType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ContentPageQuery extends PageQuery {

    @Size(max = 255, message = "长度不能超过 255 个字符")
    private String keyword;

    @Positive(message = "栏目 ID 必须大于 0")
    private Long columnId;

    private SiteType siteType;

    private ContentStatus status;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getColumnId() {
        return columnId;
    }

    public void setColumnId(Long columnId) {
        this.columnId = columnId;
    }

    public SiteType getSiteType() {
        return siteType;
    }

    public void setSiteType(SiteType siteType) {
        this.siteType = siteType;
    }

    public ContentStatus getStatus() {
        return status;
    }

    public void setStatus(ContentStatus status) {
        this.status = status;
    }
}
