package com.zlwang.school.modules.banner.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.banner.dto.BannerPageQuery;
import com.zlwang.school.modules.banner.dto.CreateBannerRequest;
import com.zlwang.school.modules.banner.dto.SortBannersRequest;
import com.zlwang.school.modules.banner.dto.UpdateBannerRequest;
import com.zlwang.school.modules.banner.dto.UpdateBannerStatusRequest;
import com.zlwang.school.modules.banner.model.CmsBanner;
import com.zlwang.school.modules.banner.service.CmsBannerService;
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
@RequestMapping("/api/admin/banners")
public class CmsBannerController {

    private final CmsBannerService cmsBannerService;

    public CmsBannerController(CmsBannerService cmsBannerService) {
        this.cmsBannerService = cmsBannerService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cms:banner')")
    public ApiResult<PageResult<CmsBanner>> findPage(@Valid BannerPageQuery query) {
        return ApiResult.success(cmsBannerService.findPage(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:banner')")
    public ApiResult<CmsBanner> findById(@Positive @PathVariable long id) {
        return ApiResult.success(cmsBannerService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cms:banner:manage')")
    public ApiResult<Long> create(
        @Valid @RequestBody CreateBannerRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        return ApiResult.success(cmsBannerService.create(request, operator.id()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:banner:manage')")
    public ApiResult<Void> update(
        @Positive @PathVariable long id,
        @Valid @RequestBody UpdateBannerRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsBannerService.update(id, request, operator.id());
        return ApiResult.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('cms:banner:manage')")
    public ApiResult<Void> updateStatus(
        @Positive @PathVariable long id,
        @Valid @RequestBody UpdateBannerStatusRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsBannerService.updateStatus(id, request.enabled(), operator.id());
        return ApiResult.success();
    }

    @PutMapping("/sort")
    @PreAuthorize("hasAuthority('cms:banner:manage')")
    public ApiResult<Void> updateSort(
        @Valid @RequestBody SortBannersRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsBannerService.updateSort(request, operator.id());
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:banner:manage')")
    public ApiResult<Void> delete(
        @Positive @PathVariable long id,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsBannerService.delete(id, operator.id());
        return ApiResult.success();
    }
}
