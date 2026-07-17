package com.zlwang.school.modules.content.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.content.dto.ContentPageQuery;
import com.zlwang.school.modules.content.dto.CreateContentRequest;
import com.zlwang.school.modules.content.dto.PublishContentRequest;
import com.zlwang.school.modules.content.dto.UpdateContentRecommendRequest;
import com.zlwang.school.modules.content.dto.UpdateContentRequest;
import com.zlwang.school.modules.content.dto.UpdateContentTopRequest;
import com.zlwang.school.modules.content.service.CmsContentService;
import com.zlwang.school.modules.content.vo.ContentDetailResponse;
import com.zlwang.school.modules.content.vo.ContentSummaryResponse;
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
@RequestMapping("/api/admin/contents")
public class CmsContentController {

    private final CmsContentService cmsContentService;

    public CmsContentController(CmsContentService cmsContentService) {
        this.cmsContentService = cmsContentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cms:content')")
    public ApiResult<PageResult<ContentSummaryResponse>> findPage(@Valid ContentPageQuery query) {
        return ApiResult.success(cmsContentService.findPage(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:content')")
    public ApiResult<ContentDetailResponse> findById(@Positive @PathVariable long id) {
        return ApiResult.success(cmsContentService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cms:content:manage')")
    public ApiResult<Long> create(
        @Valid @RequestBody CreateContentRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        return ApiResult.success(cmsContentService.create(request, operator.id()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:content:manage')")
    public ApiResult<Void> update(
        @Positive @PathVariable long id,
        @Valid @RequestBody UpdateContentRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsContentService.update(id, request, operator.id());
        return ApiResult.success();
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('cms:content:manage')")
    public ApiResult<Void> publish(
        @Positive @PathVariable long id,
        @RequestBody(required = false) PublishContentRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsContentService.publish(id, request == null ? new PublishContentRequest(null) : request, operator.id());
        return ApiResult.success();
    }

    @PutMapping("/{id}/offline")
    @PreAuthorize("hasAuthority('cms:content:manage')")
    public ApiResult<Void> offline(
        @Positive @PathVariable long id,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsContentService.offline(id, operator.id());
        return ApiResult.success();
    }

    @PutMapping("/{id}/top")
    @PreAuthorize("hasAuthority('cms:content:manage')")
    public ApiResult<Void> updateTop(
        @Positive @PathVariable long id,
        @Valid @RequestBody UpdateContentTopRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsContentService.updateTop(id, request, operator.id());
        return ApiResult.success();
    }

    @PutMapping("/{id}/recommend")
    @PreAuthorize("hasAuthority('cms:content:manage')")
    public ApiResult<Void> updateRecommend(
        @Positive @PathVariable long id,
        @Valid @RequestBody UpdateContentRecommendRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsContentService.updateRecommend(id, request, operator.id());
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:content:manage')")
    public ApiResult<Void> delete(
        @Positive @PathVariable long id,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsContentService.delete(id, operator.id());
        return ApiResult.success();
    }
}
