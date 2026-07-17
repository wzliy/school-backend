package com.zlwang.school.modules.content.model;

import java.time.LocalDateTime;

public record ContentAttachment(
    long id,
    long contentId,
    Long mediaId,
    String fileName,
    String fileUrl,
    long fileSize,
    AttachmentFileType fileType,
    int sortNo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
