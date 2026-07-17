package com.zlwang.school.modules.media.repository;

import com.zlwang.school.modules.media.model.MediaFileType;
import com.zlwang.school.modules.media.model.StorageType;

public record CreateCmsMedia(
    StorageType storageType,
    MediaFileType fileType,
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
