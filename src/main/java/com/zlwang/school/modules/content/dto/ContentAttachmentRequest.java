package com.zlwang.school.modules.content.dto;

import com.zlwang.school.modules.content.model.AttachmentFileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ContentAttachmentRequest(
    @Positive(message = "媒体 ID 必须大于 0")
    Long mediaId,

    @NotBlank(message = "不能为空")
    @Size(max = 255, message = "长度不能超过 255 个字符")
    String fileName,

    @NotBlank(message = "不能为空")
    @Size(max = 512, message = "长度不能超过 512 个字符")
    String fileUrl,

    @PositiveOrZero(message = "不能小于 0")
    long fileSize,

    @NotNull(message = "不能为空")
    AttachmentFileType fileType,

    @PositiveOrZero(message = "不能小于 0")
    int sortNo
) {
}
