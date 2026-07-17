package com.zlwang.school.modules.page.model;

import com.zlwang.school.modules.template.model.SiteType;
import java.util.List;

public record PageDefinition(
    PageCode pageCode,
    SiteType siteType,
    List<PageSectionDefinition> sections
) {

    public PageDefinition {
        sections = List.copyOf(sections);
    }
}
