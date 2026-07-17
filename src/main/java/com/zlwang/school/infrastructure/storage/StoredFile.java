package com.zlwang.school.infrastructure.storage;

import com.zlwang.school.modules.media.model.StorageType;

public record StoredFile(
    StorageType storageType,
    String storedName,
    String filePath,
    String accessUrl
) {
}
