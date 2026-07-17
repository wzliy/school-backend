package com.zlwang.school.modules.media.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.media.dto.MediaPageQuery;
import com.zlwang.school.modules.media.model.CmsMedia;
import com.zlwang.school.modules.media.service.CmsMediaService;
import com.zlwang.school.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/api/admin/media")
public class CmsMediaController {

    private final CmsMediaService cmsMediaService;

    public CmsMediaController(CmsMediaService cmsMediaService) {
        this.cmsMediaService = cmsMediaService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cms:media')")
    public ApiResult<PageResult<CmsMedia>> findPage(@Valid MediaPageQuery query) {
        return ApiResult.success(cmsMediaService.findPage(query));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:media')")
    public ApiResult<CmsMedia> findById(@Positive @PathVariable long id) {
        return ApiResult.success(cmsMediaService.findById(id));
    }

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('cms:media:manage')")
    public ApiResult<Long> upload(
        @RequestPart("file") MultipartFile file,
        @Size(max = 512, message = "长度不能超过 512 个字符")
        @RequestParam(required = false) String remark,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        return ApiResult.success(cmsMediaService.upload(file, remark, operator.id()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:media:manage')")
    public ApiResult<Void> delete(
        @Positive @PathVariable long id,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsMediaService.delete(id, operator.id());
        return ApiResult.success();
    }
}
