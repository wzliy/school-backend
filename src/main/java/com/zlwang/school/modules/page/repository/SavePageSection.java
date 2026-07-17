package com.zlwang.school.modules.page.repository;

import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.page.model.PageSectionType;
import com.zlwang.school.modules.template.model.SiteType;
import java.util.Map;

public record SavePageSection(
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
    boolean enabled
) {
}
