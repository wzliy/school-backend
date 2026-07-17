package com.zlwang.school.modules.content.vo;

import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;

public record ContentSummaryResponse(
    long id,
    long columnId,
    String columnName,
    SiteType siteType,
    String title,
    String subtitle,
    String summary,
    String coverUrl,
    LocalDateTime publishAt,
    ContentStatus status,
    boolean topFlag,
    boolean recommendFlag,
    int sortNo,
    long viewCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static ContentSummaryResponse from(CmsContent content) {
        return new ContentSummaryResponse(
            content.id(),
            content.columnId(),
            content.columnName(),
            content.siteType(),
            content.title(),
            content.subtitle(),
            content.summary(),
            content.coverUrl(),
            content.publishAt(),
            content.status(),
            content.topFlag(),
            content.recommendFlag(),
            content.sortNo(),
            content.viewCount(),
            content.createdAt(),
            content.updatedAt()
        );
    }
}
