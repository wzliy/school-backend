package com.zlwang.school.modules.banner.repository;

import com.zlwang.school.modules.banner.model.BannerLinkTarget;
import com.zlwang.school.modules.banner.model.BannerLinkType;
import com.zlwang.school.modules.banner.model.BannerPosition;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;

public record CreateCmsBanner(
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
    long operatorId
) {
}
