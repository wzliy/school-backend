package com.zlwang.school.modules.portal.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.portal.dto.PortalContentPageQuery;
import com.zlwang.school.modules.portal.dto.PortalSearchQuery;
import com.zlwang.school.modules.portal.service.PortalContentService;
import com.zlwang.school.modules.portal.vo.PortalColumnDetailResponse;
import com.zlwang.school.modules.portal.vo.PortalColumnTreeNodeResponse;
import com.zlwang.school.modules.portal.vo.PortalContentDetailResponse;
import com.zlwang.school.modules.portal.vo.PortalContentSummaryResponse;
import com.zlwang.school.modules.portal.vo.PortalSearchResponse;
import com.zlwang.school.modules.portal.vo.PortalViewCountResponse;
import com.zlwang.school.modules.template.model.SiteType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@Tag(name = "Portal 栏目内容", description = "公开栏目树、栏目内容分页和内容详情接口")
@RestController
@RequestMapping("/api/portal")
public class PortalContentController {

    private final PortalContentService portalContentService;

    public PortalContentController(PortalContentService portalContentService) {
        this.portalContentService = portalContentService;
    }

    @Operation(summary = "查询公开栏目树")
    @GetMapping("/columns")
    public ApiResult<List<PortalColumnTreeNodeResponse>> findColumnTree(
        @RequestParam SiteType siteType
    ) {
        return ApiResult.success(portalContentService.findColumnTree(siteType));
    }

    @Operation(summary = "查询公开栏目详情")
    @GetMapping("/columns/{id}")
    public ApiResult<PortalColumnDetailResponse> findColumn(
        @Positive @PathVariable long id
    ) {
        return ApiResult.success(portalContentService.findColumn(id));
    }

    @Operation(summary = "分页查询栏目公开内容")
    @GetMapping("/columns/{id}/contents")
    public ApiResult<PageResult<PortalContentSummaryResponse>> findContents(
        @Positive @PathVariable long id,
        @Valid @ParameterObject PortalContentPageQuery query
    ) {
        return ApiResult.success(portalContentService.findContents(id, query));
    }

    @Operation(summary = "查询公开内容详情及附件")
    @GetMapping("/contents/{id}")
    public ApiResult<PortalContentDetailResponse> findContent(
        @Positive @PathVariable long id
    ) {
        return ApiResult.success(portalContentService.findContent(id));
    }

    @Operation(summary = "搜索公开内容")
    @GetMapping("/search")
    public ApiResult<PortalSearchResponse> search(
        @Valid @ParameterObject PortalSearchQuery query
    ) {
        return ApiResult.success(portalContentService.search(query));
    }

    @Operation(summary = "增加公开内容浏览量")
    @PutMapping("/contents/{id}/view-count")
    public ApiResult<PortalViewCountResponse> incrementViewCount(
        @Positive @PathVariable long id
    ) {
        return ApiResult.success(portalContentService.incrementViewCount(id));
    }
}
