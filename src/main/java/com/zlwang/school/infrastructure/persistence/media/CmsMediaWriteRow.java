package com.zlwang.school.infrastructure.persistence.media;

public record CmsMediaWriteRow(
    String storageType,
    String fileType,
    String originalName,
    String storedName,
    String extension,
    String mimeType,
    long fileSize,
    String filePath,
    String accessUrl,
    long uploaderId,
    String remark
) {
}
