package com.zlwang.school.modules.content.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record CreateContentRequest(
    @Positive(message = "必须大于 0")
    long columnId,

    @NotBlank(message = "不能为空")
    @Size(max = 255, message = "长度不能超过 255 个字符")
    String title,

    @Size(max = 255, message = "长度不能超过 255 个字符")
    String subtitle,

    @Size(max = 1024, message = "长度不能超过 1024 个字符")
    String summary,

    String contentHtml,

    @Size(max = 512, message = "长度不能超过 512 个字符")
    String coverUrl,

    @Size(max = 128, message = "长度不能超过 128 个字符")
    String source,

    @Size(max = 64, message = "长度不能超过 64 个字符")
    String author,

    LocalDateTime publishAt,

    boolean topFlag,

    boolean recommendFlag,

    @PositiveOrZero(message = "不能小于 0")
    int sortNo,

    @Size(max = 255, message = "长度不能超过 255 个字符")
    String seoTitle,

    @Size(max = 512, message = "长度不能超过 512 个字符")
    String seoKeywords,

    @Size(max = 1024, message = "长度不能超过 1024 个字符")
    String seoDescription,

    Map<String, Object> extensionData,

    List<@Valid ContentAttachmentRequest> attachments
) {

    public CreateContentRequest {
        extensionData = extensionData == null
            ? Map.of()
            : Collections.unmodifiableMap(new LinkedHashMap<>(extensionData));
        attachments = attachments == null ? List.of() : List.copyOf(attachments);
    }
}
