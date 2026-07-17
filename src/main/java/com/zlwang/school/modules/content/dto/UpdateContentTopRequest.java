package com.zlwang.school.modules.content.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateContentTopRequest(@NotNull(message = "不能为空") Boolean topFlag) {
}
