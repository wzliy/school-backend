package com.zlwang.school.modules.portal.vo;

import com.zlwang.school.modules.content.model.AttachmentFileType;
import com.zlwang.school.modules.content.model.ContentAttachment;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description = "公开内容附件",
    example = "{\"id\":2001,\"fileName\":\"招生简章.pdf\",\"fileUrl\":\"/uploads/admission.pdf\",\"fileSize\":102400,\"fileType\":\"DOCUMENT\",\"sortNo\":10}"
)
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
