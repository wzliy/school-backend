package com.zlwang.school.modules.column.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateColumnStatusRequest(@NotNull Boolean enabled) {
}
