package com.zlwang.school.modules.link.dto;

import com.zlwang.school.common.pagination.PageQuery;
import com.zlwang.school.modules.site.model.SiteScope;
import jakarta.validation.constraints.Size;

public class FriendLinkPageQuery extends PageQuery {

    @Size(max = 128, message = "关键词长度不能超过 128 个字符")
    private String keyword;

    private SiteScope siteType;

    private Boolean enabled;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public SiteScope getSiteType() {
        return siteType;
    }

    public void setSiteType(SiteScope siteType) {
        this.siteType = siteType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
