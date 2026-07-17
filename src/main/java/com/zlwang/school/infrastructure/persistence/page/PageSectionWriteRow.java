package com.zlwang.school.infrastructure.persistence.page;

public record PageSectionWriteRow(
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
    long operatorId
) {
}
