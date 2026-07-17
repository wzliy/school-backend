package com.zlwang.school.modules.link.repository;

import com.zlwang.school.modules.site.model.SiteScope;

public record UpdateCmsFriendLink(
    long id,
    SiteScope siteType,
    String name,
    String linkUrl,
    String logoUrl,
    int sortNo,
    boolean enabled,
    String remark,
    long operatorId
) {
}
