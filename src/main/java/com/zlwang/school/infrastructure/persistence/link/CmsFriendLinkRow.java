package com.zlwang.school.infrastructure.persistence.link;

import java.time.LocalDateTime;

public record CmsFriendLinkRow(
    long id,
    String siteType,
    String name,
    String linkUrl,
    String logoUrl,
    int sortNo,
    int enabled,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
