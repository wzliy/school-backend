package com.zlwang.school.infrastructure.persistence.link;

public record CmsFriendLinkWriteRow(
    String siteType,
    String name,
    String linkUrl,
    String logoUrl,
    int sortNo,
    int enabled,
    String remark,
    long operatorId
) {
}
