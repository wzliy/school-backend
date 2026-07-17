package com.zlwang.school.modules.seo.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.modules.seo.model.SeoMetadata;
import com.zlwang.school.modules.seo.service.SeoMetadataService;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/seo")
public class CmsSeoController {

    private final SeoMetadataService seoMetadataService;

    public CmsSeoController(SeoMetadataService seoMetadataService) {
        this.seoMetadataService = seoMetadataService;
    }

    @GetMapping("/columns/{id}")
    @PreAuthorize("hasAuthority('cms:column')")
    public ApiResult<SeoMetadata> resolveColumn(@Positive @PathVariable long id) {
        return ApiResult.success(seoMetadataService.resolveColumn(id));
    }

    @GetMapping("/contents/{id}")
    @PreAuthorize("hasAuthority('cms:content')")
    public ApiResult<SeoMetadata> resolveContent(@Positive @PathVariable long id) {
        return ApiResult.success(seoMetadataService.resolveContent(id));
    }
}
