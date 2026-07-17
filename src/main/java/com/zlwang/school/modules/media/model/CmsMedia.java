package com.zlwang.school.modules.media.model;

import java.time.LocalDateTime;

public record CmsMedia(
    long id,
    StorageType storageType,
    MediaFileType fileType,
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
