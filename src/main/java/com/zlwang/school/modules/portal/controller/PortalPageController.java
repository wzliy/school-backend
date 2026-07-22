package com.zlwang.school.modules.portal.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.portal.service.PortalPageService;
import com.zlwang.school.modules.portal.vo.PortalPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Portal 页面", description = "官网主站与招生就业专题站公开页面聚合接口")
@RestController
@RequestMapping("/api/portal")
public class PortalPageController {

    private final PortalPageService portalPageService;

    public PortalPageController(PortalPageService portalPageService) {
        this.portalPageService = portalPageService;
    }

    @Operation(summary = "查询公开页面聚合数据")
    @GetMapping("/pages/{pageCode}")
    public ApiResult<PortalPageResponse> findPage(
        @Parameter(description = "页面编码", example = "HOME")
        @PathVariable PageCode pageCode
    ) {
        return ApiResult.success(portalPageService.findPage(pageCode));
    }

    @Operation(summary = "查询招生就业专题首页聚合数据")
    @GetMapping("/recruit/home")
    public ApiResult<PortalPageResponse> findRecruitHome() {
        return ApiResult.success(portalPageService.findPage(PageCode.RECRUIT_HOME));
    }
}
