package com.zlwang.school.modules.banner.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateBannerStatusRequest(@NotNull Boolean enabled) {
}
