package com.zlwang.school.infrastructure.persistence.content;

import java.time.LocalDateTime;

public record CmsContentWriteRow(
    long columnId,
    String siteType,
    String title,
    String subtitle,
    String summary,
    String contentHtml,
    String coverUrl,
    String source,
    String author,
    LocalDateTime publishAt,
    String status,
    int topFlag,
    int recommendFlag,
    int sortNo,
    String seoTitle,
    String seoKeywords,
    String seoDescription,
    String extensionData,
    long operatorId
) {
}
