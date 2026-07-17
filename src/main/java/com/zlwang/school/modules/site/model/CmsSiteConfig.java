package com.zlwang.school.modules.site.model;

import java.time.LocalDateTime;

public record CmsSiteConfig(
    long id,
    SiteScope siteType,
    String configKey,
    String configValue,
    SiteConfigType configType,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
