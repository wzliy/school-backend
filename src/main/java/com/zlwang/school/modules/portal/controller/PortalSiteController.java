package com.zlwang.school.modules.portal.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.modules.banner.model.BannerPosition;
import com.zlwang.school.modules.portal.service.PortalSiteService;
import com.zlwang.school.modules.portal.vo.PortalBannerResponse;
import com.zlwang.school.modules.portal.vo.PortalFriendLinkResponse;
import com.zlwang.school.modules.portal.vo.PortalNavigationNodeResponse;
import com.zlwang.school.modules.portal.vo.PortalSiteConfigResponse;
import com.zlwang.school.modules.template.model.SiteType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Portal 公共数据", description = "站点配置、导航、Banner 和友情链接公开查询接口")
@RestController
@RequestMapping("/api/portal")
public class PortalSiteController {

    private final PortalSiteService portalSiteService;

    public PortalSiteController(PortalSiteService portalSiteService) {
        this.portalSiteService = portalSiteService;
    }

    @Operation(summary = "查询公开站点配置")
    @GetMapping("/site-config")
    public ApiResult<PortalSiteConfigResponse> findSiteConfig(
        @RequestParam SiteType siteType
    ) {
        return ApiResult.success(portalSiteService.findSiteConfig(siteType));
    }

    @Operation(summary = "查询公开导航树")
    @GetMapping("/navigation")
    public ApiResult<List<PortalNavigationNodeResponse>> findNavigation(
        @RequestParam SiteType siteType
    ) {
        return ApiResult.success(portalSiteService.findNavigation(siteType));
    }

    @Operation(summary = "查询当前有效 Banner")
    @GetMapping("/banners")
    public ApiResult<List<PortalBannerResponse>> findBanners(
        @RequestParam SiteType siteType,
        @RequestParam BannerPosition position
    ) {
        return ApiResult.success(portalSiteService.findBanners(siteType, position));
    }

    @Operation(summary = "查询公开友情链接")
    @GetMapping("/friend-links")
    public ApiResult<List<PortalFriendLinkResponse>> findFriendLinks(
        @RequestParam SiteType siteType
    ) {
        return ApiResult.success(portalSiteService.findFriendLinks(siteType));
    }
}
