package com.zlwang.school.infrastructure.persistence.media;

import java.time.LocalDateTime;

public record CmsMediaRow(
    long id,
    String storageType,
    String fileType,
    String originalName,
    String storedName,
    String extension,
    String mimeType,
    long fileSize,
    String filePath,
    String accessUrl,
    Long uploaderId,
    String remark,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
