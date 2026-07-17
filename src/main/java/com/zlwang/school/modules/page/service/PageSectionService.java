package com.zlwang.school.modules.page.service;

import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CmsColumnRepository;
import com.zlwang.school.modules.page.dto.PageSectionItemRequest;
import com.zlwang.school.modules.page.dto.ReplacePageSectionsRequest;
import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.page.model.PageDefinition;
import com.zlwang.school.modules.page.model.PageSectionDefinition;
import com.zlwang.school.modules.page.repository.PageSectionRepository;
import com.zlwang.school.modules.page.repository.SavePageSection;
import com.zlwang.school.modules.page.vo.PageSectionResponse;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class PageSectionService {

    private final PageSectionRepository pageSectionRepository;
    private final CmsColumnRepository cmsColumnRepository;
    private final PageSectionRegistry pageSectionRegistry;
    private final PageSectionConfigValidator configValidator;

    public PageSectionService(
        PageSectionRepository pageSectionRepository,
        CmsColumnRepository cmsColumnRepository,
        PageSectionRegistry pageSectionRegistry,
        PageSectionConfigValidator configValidator
    ) {
        this.pageSectionRepository = pageSectionRepository;
        this.cmsColumnRepository = cmsColumnRepository;
        this.pageSectionRegistry = pageSectionRegistry;
        this.configValidator = configValidator;
    }

    public List<PageSectionResponse> findAll(PageCode pageCode) {
        PageDefinition page = pageSectionRegistry.get(pageCode);
        return pageSectionRepository.findAll(page.siteType(), pageCode).stream()
            .map(PageSectionResponse::from)
            .toList();
    }

    public void replace(PageCode pageCode, ReplacePageSectionsRequest request, long operatorId) {
        PageDefinition page = pageSectionRegistry.get(pageCode);
        validateCompleteSet(page, request.sections());
        Set<Integer> sortNumbers = new HashSet<>();
        List<SavePageSection> commands = request.sections().stream().map(item -> {
            if (!sortNumbers.add(item.sortNo())) {
                throw badRequest("区块排序号不能重复");
            }
            PageSectionDefinition definition = definition(page, item.sectionCode());
            validateDefinition(page, definition, item);
            Map<String, Object> config = configValidator.validate(pageCode, item.sectionType(), item.config());
            return new SavePageSection(
                page.siteType(),
                pageCode,
                definition.sectionCode(),
                item.sectionName().trim(),
                definition.sectionType(),
                item.dataSourceColumnId(),
                item.displayCount(),
                item.displayStyle().trim(),
                config,
                item.sortNo(),
                item.enabled()
            );
        }).toList();
        pageSectionRepository.replace(page.siteType(), pageCode, commands, operatorId);
    }

    private void validateCompleteSet(PageDefinition page, List<PageSectionItemRequest> items) {
        Set<String> codes = new HashSet<>();
        for (PageSectionItemRequest item : items) {
            String code = item.sectionCode().trim();
            if (!codes.add(code)) {
                throw badRequest("区块编码不能重复：" + code);
            }
        }
        Set<String> expected = page.sections().stream()
            .map(PageSectionDefinition::sectionCode)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        codes.stream().filter(code -> !expected.contains(code)).findFirst().ifPresent(code -> {
            throw badRequest("页面包含未定义区块：" + code);
        });
        expected.stream().filter(code -> !codes.contains(code)).findFirst().ifPresent(code -> {
            throw badRequest("页面缺少预定义区块：" + code);
        });
    }

    private void validateDefinition(
        PageDefinition page,
        PageSectionDefinition definition,
        PageSectionItemRequest item
    ) {
        if (item.sectionType() != definition.sectionType()) {
            throw badRequest("区块 " + definition.sectionCode() + " 的类型不能修改");
        }
        if (!definition.displayStyles().contains(item.displayStyle().trim())) {
            throw badRequest("区块 " + definition.sectionCode() + " 使用了不支持的展示样式");
        }
        validateDisplayCount(definition, item.displayCount());
        validateDataSource(page, definition, item.dataSourceColumnId());
    }

    private void validateDisplayCount(PageSectionDefinition definition, Integer displayCount) {
        if (!definition.displayCountAllowed() && displayCount != null) {
            throw badRequest("区块 " + definition.sectionCode() + " 不能设置展示数量");
        }
        if (definition.displayCountRequired() && displayCount == null) {
            throw badRequest("区块 " + definition.sectionCode() + " 必须设置展示数量");
        }
        if (displayCount != null
            && (displayCount < definition.minDisplayCount() || displayCount > definition.maxDisplayCount())) {
            throw badRequest(
                "区块 " + definition.sectionCode() + " 的展示数量必须在 "
                    + definition.minDisplayCount() + "-" + definition.maxDisplayCount() + " 之间"
            );
        }
    }

    private void validateDataSource(
        PageDefinition page,
        PageSectionDefinition definition,
        Long columnId
    ) {
        if (!definition.dataSourceAllowed() && columnId != null) {
            throw badRequest("区块 " + definition.sectionCode() + " 不能设置数据源栏目");
        }
        if (definition.dataSourceRequired() && columnId == null) {
            throw badRequest("区块 " + definition.sectionCode() + " 必须设置数据源栏目");
        }
        if (columnId == null) {
            return;
        }
        CmsColumn column = cmsColumnRepository.findById(columnId)
            .orElseThrow(() -> badRequest("数据源栏目不存在：" + columnId));
        if (column.siteType() != page.siteType()) {
            throw badRequest("数据源栏目与页面必须属于同一站点");
        }
        if (!column.enabled()) {
            throw badRequest("数据源栏目已停用：" + columnId);
        }
    }

    private PageSectionDefinition definition(PageDefinition page, String sectionCode) {
        Map<String, PageSectionDefinition> definitions = page.sections().stream()
            .collect(Collectors.toMap(PageSectionDefinition::sectionCode, Function.identity()));
        return definitions.get(sectionCode.trim());
    }

    private BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, message);
    }
}
