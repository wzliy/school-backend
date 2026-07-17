package com.zlwang.school.modules.page.vo;

import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.page.model.PageSection;
import com.zlwang.school.modules.page.model.PageSectionType;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.Map;

public record PageSectionResponse(
    long id,
    SiteType siteType,
    PageCode pageCode,
    String sectionCode,
    String sectionName,
    PageSectionType sectionType,
    Long dataSourceColumnId,
    Integer displayCount,
    String displayStyle,
    Map<String, Object> config,
    int sortNo,
    boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static PageSectionResponse from(PageSection section) {
        return new PageSectionResponse(
            section.id(),
            section.siteType(),
            section.pageCode(),
            section.sectionCode(),
            section.sectionName(),
            section.sectionType(),
            section.dataSourceColumnId(),
            section.displayCount(),
            section.displayStyle(),
            section.config(),
            section.sortNo(),
            section.enabled(),
            section.createdAt(),
            section.updatedAt()
        );
    }
}
