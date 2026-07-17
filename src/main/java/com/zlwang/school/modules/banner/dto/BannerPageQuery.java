package com.zlwang.school.modules.banner.dto;

import com.zlwang.school.common.pagination.PageQuery;
import com.zlwang.school.modules.banner.model.BannerPosition;
import com.zlwang.school.modules.template.model.SiteType;
import jakarta.validation.constraints.Size;

public class BannerPageQuery extends PageQuery {

    @Size(max = 255, message = "长度不能超过 255 个字符")
    private String keyword;

    private SiteType siteType;

    private BannerPosition position;

    private Boolean enabled;

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

    public BannerPosition getPosition() {
        return position;
    }

    public void setPosition(BannerPosition position) {
        this.position = position;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
