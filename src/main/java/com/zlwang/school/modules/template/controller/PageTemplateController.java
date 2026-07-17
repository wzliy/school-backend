package com.zlwang.school.modules.template.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.modules.template.model.PageTemplateDefinition;
import com.zlwang.school.modules.template.service.PageTemplateRegistry;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/page-templates")
public class PageTemplateController {

    private final PageTemplateRegistry pageTemplateRegistry;

    public PageTemplateController(PageTemplateRegistry pageTemplateRegistry) {
        this.pageTemplateRegistry = pageTemplateRegistry;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('cms:column')")
    public ApiResult<List<PageTemplateDefinition>> findAll() {
        return ApiResult.success(pageTemplateRegistry.findAll());
    }
}
