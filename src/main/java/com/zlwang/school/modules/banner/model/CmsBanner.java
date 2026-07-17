package com.zlwang.school.modules.banner.model;

import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;

public record CmsBanner(
    long id,
    SiteType siteType,
    BannerPosition position,
    String title,
    String subtitle,
    String imageUrl,
    String mobileImageUrl,
    BannerLinkType linkType,
    Long linkRefId,
    String linkUrl,
    BannerLinkTarget linkTarget,
    int sortNo,
    boolean enabled,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
