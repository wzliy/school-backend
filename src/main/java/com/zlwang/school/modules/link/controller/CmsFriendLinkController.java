package com.zlwang.school.modules.link.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.link.dto.CreateFriendLinkRequest;
import com.zlwang.school.modules.link.dto.FriendLinkPageQuery;
import com.zlwang.school.modules.link.dto.SortFriendLinksRequest;
import com.zlwang.school.modules.link.dto.UpdateFriendLinkRequest;
import com.zlwang.school.modules.link.dto.UpdateFriendLinkStatusRequest;
import com.zlwang.school.modules.link.model.CmsFriendLink;
import com.zlwang.school.modules.link.service.CmsFriendLinkService;
import com.zlwang.school.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/friend-links")
public class CmsFriendLinkController {

    private final CmsFriendLinkService cmsFriendLinkService;

    public CmsFriendLinkController(CmsFriendLinkService cmsFriendLinkService) {
        this.cmsFriendLinkService = cmsFriendLinkService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cms:friend-link')")
    public ApiResult<PageResult<CmsFriendLink>> findPage(@Valid FriendLinkPageQuery query) {
        return ApiResult.success(cmsFriendLinkService.findPage(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:friend-link')")
    public ApiResult<CmsFriendLink> findById(@Positive @PathVariable long id) {
        return ApiResult.success(cmsFriendLinkService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cms:friend-link:manage')")
    public ApiResult<Long> create(
        @Valid @RequestBody CreateFriendLinkRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        return ApiResult.success(cmsFriendLinkService.create(request, operator.id()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:friend-link:manage')")
    public ApiResult<Void> update(
        @Positive @PathVariable long id,
        @Valid @RequestBody UpdateFriendLinkRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsFriendLinkService.update(id, request, operator.id());
        return ApiResult.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('cms:friend-link:manage')")
    public ApiResult<Void> updateStatus(
        @Positive @PathVariable long id,
        @Valid @RequestBody UpdateFriendLinkStatusRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsFriendLinkService.updateStatus(id, request.enabled(), operator.id());
        return ApiResult.success();
    }

    @PutMapping("/sort")
    @PreAuthorize("hasAuthority('cms:friend-link:manage')")
    public ApiResult<Void> updateSort(
        @Valid @RequestBody SortFriendLinksRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsFriendLinkService.updateSort(request, operator.id());
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:friend-link:manage')")
    public ApiResult<Void> delete(
        @Positive @PathVariable long id,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsFriendLinkService.delete(id, operator.id());
        return ApiResult.success();
    }
}
