package com.zlwang.school.infrastructure.persistence.banner;

import java.time.LocalDateTime;

public record CmsBannerWriteRow(
    String siteType,
    String position,
    String title,
    String subtitle,
    String imageUrl,
    String mobileImageUrl,
    String linkType,
    Long linkRefId,
    String linkUrl,
    String linkTarget,
    int sortNo,
    int enabled,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String remark,
    long operatorId
) {
}
