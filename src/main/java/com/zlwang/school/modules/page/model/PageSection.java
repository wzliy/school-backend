package com.zlwang.school.modules.page.model;

import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record PageSection(
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

    public PageSection {
        config = config == null
            ? Map.of()
            : Collections.unmodifiableMap(new LinkedHashMap<>(config));
    }
}
