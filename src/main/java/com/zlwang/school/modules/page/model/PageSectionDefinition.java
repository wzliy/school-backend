package com.zlwang.school.modules.page.model;

import java.util.Set;

public record PageSectionDefinition(
    String sectionCode,
    PageSectionType sectionType,
    Set<String> displayStyles,
    boolean dataSourceAllowed,
    boolean dataSourceRequired,
    boolean displayCountAllowed,
    boolean displayCountRequired,
    int minDisplayCount,
    int maxDisplayCount
) {

    public PageSectionDefinition {
        displayStyles = Set.copyOf(displayStyles);
    }
}
