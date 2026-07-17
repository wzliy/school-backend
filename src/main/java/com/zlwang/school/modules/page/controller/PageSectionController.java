package com.zlwang.school.modules.page.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.modules.page.dto.ReplacePageSectionsRequest;
import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.page.service.PageSectionService;
import com.zlwang.school.modules.page.vo.PageSectionResponse;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/pages")
public class PageSectionController {

    private final PageSectionService pageSectionService;

    public PageSectionController(PageSectionService pageSectionService) {
        this.pageSectionService = pageSectionService;
    }

    @GetMapping("/{pageCode}/sections")
    @PreAuthorize("hasAuthority('cms:column')")
    public ApiResult<List<PageSectionResponse>> findAll(@PathVariable PageCode pageCode) {
        return ApiResult.success(pageSectionService.findAll(pageCode));
    }

    @PutMapping("/{pageCode}/sections")
    @PreAuthorize("hasAuthority('cms:column:manage')")
    public ApiResult<Void> replace(
        @PathVariable PageCode pageCode,
        @Valid @RequestBody ReplacePageSectionsRequest request,
        @AuthenticationPrincipal AuthenticatedUser operator
    ) {
        pageSectionService.replace(pageCode, request, operator.id());
        return ApiResult.success();
    }
}
