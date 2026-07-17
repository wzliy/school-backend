package com.zlwang.school.modules.page.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ReplacePageSectionsRequest(
    @NotEmpty(message = "不能为空")
    @Size(max = 20, message = "数量不能超过 20")
    List<@Valid PageSectionItemRequest> sections
) {

    public ReplacePageSectionsRequest {
        sections = sections == null ? List.of() : List.copyOf(sections);
    }
}
