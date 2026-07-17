package com.zlwang.school.modules.column.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.modules.column.dto.CreateColumnRequest;
import com.zlwang.school.modules.column.dto.SortColumnsRequest;
import com.zlwang.school.modules.column.dto.UpdateColumnRequest;
import com.zlwang.school.modules.column.dto.UpdateColumnStatusRequest;
import com.zlwang.school.modules.column.service.CmsColumnService;
import com.zlwang.school.modules.column.vo.ColumnEditorSchemaResponse;
import com.zlwang.school.modules.column.vo.ColumnTreeNode;
import com.zlwang.school.modules.template.model.SiteType;
import com.zlwang.school.security.AuthenticatedUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/columns")
public class CmsColumnController {

    private final CmsColumnService cmsColumnService;

    public CmsColumnController(CmsColumnService cmsColumnService) {
        this.cmsColumnService = cmsColumnService;
    }

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('cms:column')")
    public ApiResult<List<ColumnTreeNode>> findTree(@RequestParam(required = false) SiteType siteType) {
        return ApiResult.success(cmsColumnService.findTree(siteType));
    }

    @GetMapping("/{id}/editor-schema")
    @PreAuthorize("hasAuthority('cms:column')")
    public ApiResult<ColumnEditorSchemaResponse> findEditorSchema(@Positive @PathVariable long id) {
        return ApiResult.success(cmsColumnService.findEditorSchema(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('cms:column:manage')")
    public ApiResult<Long> create(
        @Valid @RequestBody CreateColumnRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        return ApiResult.success(cmsColumnService.create(request, operator.id()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:column:manage')")
    public ApiResult<Void> update(
        @Positive @PathVariable long id,
        @Valid @RequestBody UpdateColumnRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsColumnService.update(id, request, operator.id());
        return ApiResult.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('cms:column:manage')")
    public ApiResult<Void> updateStatus(
        @Positive @PathVariable long id,
        @Valid @RequestBody UpdateColumnStatusRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsColumnService.updateStatus(id, request.enabled(), operator.id());
        return ApiResult.success();
    }

    @PutMapping("/sort")
    @PreAuthorize("hasAuthority('cms:column:manage')")
    public ApiResult<Void> updateSort(
        @Valid @RequestBody SortColumnsRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsColumnService.updateSort(request, operator.id());
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('cms:column:manage')")
    public ApiResult<Void> delete(
        @Positive @PathVariable long id,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        cmsColumnService.delete(id, operator.id());
        return ApiResult.success();
    }
}
