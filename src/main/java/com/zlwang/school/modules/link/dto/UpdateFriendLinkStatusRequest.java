package com.zlwang.school.modules.link.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateFriendLinkStatusRequest(
    @NotNull Boolean enabled
) {
}
