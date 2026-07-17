package com.zlwang.school.modules.link.model;

import com.zlwang.school.modules.site.model.SiteScope;
import java.time.LocalDateTime;

public record CmsFriendLink(
    long id,
    SiteScope siteType,
    String name,
    String linkUrl,
    String logoUrl,
    int sortNo,
    boolean enabled,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
