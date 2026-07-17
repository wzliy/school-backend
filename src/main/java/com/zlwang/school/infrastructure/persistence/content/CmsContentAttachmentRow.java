package com.zlwang.school.infrastructure.persistence.content;

import java.time.LocalDateTime;

public record CmsContentAttachmentRow(
    long id,
    long contentId,
    Long mediaId,
    String fileName,
    String fileUrl,
    long fileSize,
    String fileType,
    int sortNo,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
