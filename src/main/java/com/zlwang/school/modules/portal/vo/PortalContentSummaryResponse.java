package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.content.model.CmsContent;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(
    description = "公开内容摘要",
    example = "{\"id\":1001,\"columnId\":101,\"columnName\":\"新闻中心\",\"title\":\"校园新闻\",\"summary\":\"新闻摘要\",\"publishAt\":\"2026-07-22T09:00:00\",\"topFlag\":false,\"recommendFlag\":true,\"viewCount\":120}"
)
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
