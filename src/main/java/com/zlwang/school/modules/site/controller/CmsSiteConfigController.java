package com.zlwang.school.modules.site.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.modules.site.dto.UpdateSiteConfigsRequest;
import com.zlwang.school.modules.site.model.CmsSiteConfig;
import com.zlwang.school.modules.site.model.SiteScope;
import com.zlwang.school.modules.site.service.CmsSiteConfigService;
import com.zlwang.school.security.AuthenticatedUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/site-config")
public class CmsSiteConfigController {

    private final CmsSiteConfigService cmsSiteConfigService;

    public CmsSiteConfigController(CmsSiteConfigService cmsSiteConfigService) {
        this.cmsSiteConfigService = cmsSiteConfigService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cms:site-config')")
    public ApiResult<List<CmsSiteConfig>> findAll(
        @RequestParam(required = false) SiteScope siteType
    ) {
        return ApiResult.success(cmsSiteConfigService.findAll(siteType));
    }

    @PutMapping("/{siteType}")
    @PreAuthorize("hasAuthority('cms:site-config:manage')")
    public ApiResult<Void> update(
        @PathVariable SiteScope siteType,
        @Valid @RequestBody UpdateSiteConfigsRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsSiteConfigService.update(siteType, request, operator.id());
        return ApiResult.success();
    }
}
