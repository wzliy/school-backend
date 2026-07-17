package com.zlwang.school.infrastructure.persistence.page;

import java.time.LocalDateTime;

public record PageSectionRow(
    long id,
    String siteType,
    String pageCode,
    String sectionCode,
    String sectionName,
    String sectionType,
    Long dataSourceColumnId,
    Integer displayCount,
    String displayStyle,
    String configJson,
    int sortNo,
    int enabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
