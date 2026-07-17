package com.zlwang.school.infrastructure.persistence.content;

public record CmsContentAttachmentWriteRow(
    Long mediaId,
    String fileName,
    String fileUrl,
    long fileSize,
    String fileType,
    int sortNo
) {
}
