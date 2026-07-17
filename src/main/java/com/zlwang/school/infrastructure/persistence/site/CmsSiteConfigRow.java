package com.zlwang.school.infrastructure.persistence.site;

import java.time.LocalDateTime;

public record CmsSiteConfigRow(
    long id,
    String siteType,
    String configKey,
    String configValue,
    String configType,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
