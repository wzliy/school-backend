package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.content.model.CmsContent;
import java.time.LocalDateTime;

public record PortalContentSummaryResponse(
    long id,
    long columnId,
    String columnName,
    String title,
    String subtitle,
    String summary,
    String coverUrl,
    String source,
    String author,
    LocalDateTime publishAt,
    boolean topFlag,
    boolean recommendFlag,
    long viewCount
) {

    public static PortalContentSummaryResponse from(CmsContent content) {
        return new PortalContentSummaryResponse(
            content.id(),
            content.columnId(),
            content.columnName(),
            content.title(),
            content.subtitle(),
            content.summary(),
            content.coverUrl(),
            content.source(),
            content.author(),
            content.publishAt(),
            content.topFlag(),
            content.recommendFlag(),
            content.viewCount()
        );
    }
}
