package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.content.model.AttachmentFileType;
import com.zlwang.school.modules.content.model.ContentAttachment;

public record PortalContentAttachmentResponse(
    long id,
    String fileName,
    String fileUrl,
    long fileSize,
    AttachmentFileType fileType,
    int sortNo
) {

    public static PortalContentAttachmentResponse from(ContentAttachment attachment) {
        return new PortalContentAttachmentResponse(
            attachment.id(),
            attachment.fileName(),
            attachment.fileUrl(),
            attachment.fileSize(),
            attachment.fileType(),
            attachment.sortNo()
        );
    }
}
