package com.zlwang.school.modules.column.service;

import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.column.dto.ColumnSortItem;
import com.zlwang.school.modules.column.dto.CreateColumnRequest;
import com.zlwang.school.modules.column.dto.SortColumnsRequest;
import com.zlwang.school.modules.column.dto.UpdateColumnRequest;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CmsColumnRepository;
import com.zlwang.school.modules.column.repository.CreateCmsColumn;
import com.zlwang.school.modules.column.repository.UpdateCmsColumn;
import com.zlwang.school.modules.column.vo.ColumnEditorSchemaResponse;
import com.zlwang.school.modules.column.vo.ColumnTreeNode;
import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.EditorFieldDefinition;
import com.zlwang.school.modules.template.model.PageTemplateDefinition;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import com.zlwang.school.modules.template.model.TemplateUsage;
import com.zlwang.school.modules.template.service.PageTemplateRegistry;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CmsColumnService {

    private final CmsColumnRepository cmsColumnRepository;
    private final PageTemplateRegistry pageTemplateRegistry;
    private final ColumnTemplateConfigValidator templateConfigValidator;

    public CmsColumnService(
        CmsColumnRepository cmsColumnRepository,
        PageTemplateRegistry pageTemplateRegistry,
        ColumnTemplateConfigValidator templateConfigValidator
    ) {
        this.cmsColumnRepository = cmsColumnRepository;
        this.pageTemplateRegistry = pageTemplateRegistry;
        this.templateConfigValidator = templateConfigValidator;
    }

    public List<ColumnTreeNode> findTree(SiteType siteType) {
        List<CmsColumn> columns = cmsColumnRepository.findAll(siteType);
        Map<Long, List<CmsColumn>> children = new HashMap<>();
        columns.forEach(column -> children.computeIfAbsent(column.parentId(), ignored -> new ArrayList<>()).add(column));
        Comparator<CmsColumn> order = Comparator.comparingInt(CmsColumn::sortNo).thenComparingLong(CmsColumn::id);
        children.values().forEach(items -> items.sort(order));
        return buildTree(0L, children, new HashSet<>());
    }

    public ColumnEditorSchemaResponse findEditorSchema(long id) {
        CmsColumn column = requiredColumn(id);
        if (column.templateKey() == null) {
            return new ColumnEditorSchemaResponse(
                column.id(),
                column.siteType(),
                column.columnType(),
                null,
                null,
                Map.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
            );
        }
        PageTemplateDefinition pageTemplate = requiredTemplate(column.templateKey());
        PageTemplateDefinition detailTemplate = column.detailTemplateKey() == null
            ? null
            : requiredTemplate(column.detailTemplateKey());
        PageTemplateDefinition contentTemplate = detailTemplate == null ? pageTemplate : detailTemplate;

        List<EditorFieldDefinition> extensionFields = new ArrayList<>(pageTemplate.editorSchema().extensionFields());
        if (detailTemplate != null) {
            Set<String> codes = extensionFields.stream()
                .map(EditorFieldDefinition::fieldCode)
                .collect(java.util.stream.Collectors.toSet());
            detailTemplate.editorSchema().extensionFields().stream()
                .filter(field -> codes.add(field.fieldCode()))
                .forEach(extensionFields::add);
        }
        return new ColumnEditorSchemaResponse(
            column.id(),
            column.siteType(),
            column.columnType(),
            column.templateKey(),
            column.detailTemplateKey(),
            column.templateConfig(),
            pageTemplate.editorSchema().columnFields(),
            detailTemplate == null ? List.of() : detailTemplate.editorSchema().columnFields(),
            contentTemplate.editorSchema().contentFields(),
            List.copyOf(extensionFields)
        );
    }

    public long create(CreateColumnRequest request, long operatorId) {
        String code = request.columnCode().trim();
        if (cmsColumnRepository.existsByCode(request.siteType(), code, null)) {
            throw conflict("同站点栏目编码已存在");
        }
        validateParent(request.parentId(), request.siteType(), null);
        TemplateSelection templates = validateTemplates(
            request.siteType(),
            request.columnType(),
            request.templateKey(),
            request.detailTemplateKey(),
            request.routePath(),
            request.externalUrl()
        );
        Map<String, Object> config = templates.pageTemplateKey() == null
            ? Map.of()
            : templateConfigValidator.validateAndApplyDefaults(
                templates.pageTemplateKey(), templates.detailTemplateKey(), request.templateConfig()
            );
        try {
            return cmsColumnRepository.create(new CreateCmsColumn(
                request.parentId(),
                request.siteType(),
                request.columnName().trim(),
                code,
                request.columnType(),
                normalize(request.routePath()),
                normalize(request.externalUrl()),
                templates.pageTemplateKey(),
                templates.detailTemplateKey(),
                config,
                normalize(request.coverUrl()),
                request.sortNo(),
                request.navVisible(),
                request.enabled(),
                normalize(request.seoTitle()),
                normalize(request.seoKeywords()),
                normalize(request.seoDescription()),
                normalize(request.remark()),
                operatorId
            ));
        } catch (DataIntegrityViolationException ex) {
            throw conflict("同站点栏目编码已存在");
        }
    }

    public void update(long id, UpdateColumnRequest request, long operatorId) {
        CmsColumn existing = requiredColumn(id);
        String code = request.columnCode().trim();
        if (cmsColumnRepository.existsByCode(existing.siteType(), code, id)) {
            throw conflict("同站点栏目编码已存在");
        }
        validateParent(request.parentId(), existing.siteType(), id);
        TemplateSelection templates = validateTemplates(
            existing.siteType(),
            request.columnType(),
            request.templateKey(),
            request.detailTemplateKey(),
            request.routePath(),
            request.externalUrl()
        );
        if (cmsColumnRepository.countContents(id) > 0
            && (!java.util.Objects.equals(existing.templateKey(), templates.pageTemplateKey())
                || !java.util.Objects.equals(existing.detailTemplateKey(), templates.detailTemplateKey()))) {
            throw new BusinessException(ErrorCode.CONFLICT, "栏目已有内容，不能直接切换页面模板");
        }
        Map<String, Object> config = templates.pageTemplateKey() == null
            ? Map.of()
            : templateConfigValidator.validateAndApplyDefaults(
                templates.pageTemplateKey(), templates.detailTemplateKey(), request.templateConfig()
            );
        try {
            boolean updated = cmsColumnRepository.update(new UpdateCmsColumn(
                id,
                request.parentId(),
                request.columnName().trim(),
                code,
                request.columnType(),
                normalize(request.routePath()),
                normalize(request.externalUrl()),
                templates.pageTemplateKey(),
                templates.detailTemplateKey(),
                config,
                normalize(request.coverUrl()),
                request.sortNo(),
                request.navVisible(),
                request.enabled(),
                normalize(request.seoTitle()),
                normalize(request.seoKeywords()),
                normalize(request.seoDescription()),
                normalize(request.remark()),
                operatorId
            ));
            if (!updated) {
                throw notFound(id);
            }
        } catch (DataIntegrityViolationException ex) {
            throw conflict("同站点栏目编码已存在");
        }
    }

    public void updateStatus(long id, boolean enabled, long operatorId) {
        if (!cmsColumnRepository.updateStatus(id, enabled, operatorId)) {
            throw notFound(id);
        }
    }

    public void updateSort(SortColumnsRequest request, long operatorId) {
        List<ColumnSortItem> items = request.items();
        if (items.stream().map(ColumnSortItem::id).distinct().count() != items.size()) {
            throw badRequest("排序列表包含重复栏目");
        }
        for (ColumnSortItem item : items) {
            CmsColumn column = requiredColumn(item.id());
            validateParent(item.parentId(), column.siteType(), item.id());
        }
        cmsColumnRepository.updateSort(items, operatorId);
    }

    public void delete(long id, long operatorId) {
        requiredColumn(id);
        if (cmsColumnRepository.countChildren(id) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "栏目包含子栏目，不能删除");
        }
        if (cmsColumnRepository.countContents(id) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "栏目包含内容，不能删除");
        }
        if (!cmsColumnRepository.delete(id, operatorId)) {
            throw notFound(id);
        }
    }

    private TemplateSelection validateTemplates(
        SiteType siteType,
        ColumnType columnType,
        PageTemplateKey pageTemplateKey,
        PageTemplateKey detailTemplateKey,
        String routePath,
        String externalUrl
    ) {
        if (columnType == ColumnType.LINK) {
            if (pageTemplateKey != null || detailTemplateKey != null) {
                throw badRequest("外链栏目不能绑定页面模板");
            }
            validateExternalUrl(externalUrl);
            return new TemplateSelection(null, null);
        }
        if (!StringUtils.hasText(routePath) || !routePath.trim().startsWith("/")) {
            throw badRequest("非外链栏目的路由路径必须以 / 开头");
        }
        if (StringUtils.hasText(externalUrl)) {
            throw badRequest("非外链栏目不能设置外部链接");
        }
        if (pageTemplateKey == null) {
            throw badRequest("非外链栏目必须选择页面模板");
        }
        PageTemplateDefinition pageTemplate = requiredTemplate(pageTemplateKey);
        if (pageTemplate.usage() == TemplateUsage.DETAIL || pageTemplate.usage() == TemplateUsage.SYSTEM
            || !pageTemplate.supports(siteType, columnType)) {
            throw badRequest("页面模板与站点或栏目类型不兼容");
        }

        PageTemplateKey resolvedDetail = detailTemplateKey == null
            ? pageTemplate.defaultDetailTemplateKey()
            : detailTemplateKey;
        if (resolvedDetail != null) {
            PageTemplateDefinition detailTemplate = requiredTemplate(resolvedDetail);
            if (detailTemplate.usage() != TemplateUsage.DETAIL
                || !detailTemplate.supports(siteType, columnType)) {
                throw badRequest("详情模板与站点或栏目类型不兼容");
            }
        }
        return new TemplateSelection(pageTemplateKey, resolvedDetail);
    }

    private void validateParent(long parentId, SiteType siteType, Long currentId) {
        if (parentId == 0) {
            return;
        }
        if (currentId != null && parentId == currentId) {
            throw badRequest("栏目不能将自身设为父栏目");
        }
        CmsColumn parent = requiredColumn(parentId);
        if (parent.siteType() != siteType) {
            throw badRequest("父栏目与当前栏目必须属于同一站点");
        }
        Set<Long> visited = new HashSet<>();
        long cursor = parentId;
        while (cursor != 0) {
            if (!visited.add(cursor) || (currentId != null && cursor == currentId)) {
                throw badRequest("栏目父级关系不能形成循环");
            }
            cursor = cmsColumnRepository.findById(cursor).map(CmsColumn::parentId).orElse(0L);
        }
    }

    private void validateExternalUrl(String externalUrl) {
        if (!StringUtils.hasText(externalUrl)) {
            throw badRequest("外链栏目必须设置外部链接");
        }
        try {
            URI uri = URI.create(externalUrl.trim());
            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null) {
                throw badRequest("外部链接必须是有效的 HTTP 或 HTTPS 地址");
            }
        } catch (IllegalArgumentException ex) {
            throw badRequest("外部链接必须是有效的 HTTP 或 HTTPS 地址");
        }
    }

    private List<ColumnTreeNode> buildTree(
        long parentId,
        Map<Long, List<CmsColumn>> children,
        Set<Long> path
    ) {
        List<ColumnTreeNode> result = new ArrayList<>();
        for (CmsColumn column : children.getOrDefault(parentId, List.of())) {
            if (!path.add(column.id())) {
                continue;
            }
            result.add(ColumnTreeNode.from(column, buildTree(column.id(), children, path)));
            path.remove(column.id());
        }
        return List.copyOf(result);
    }

    private CmsColumn requiredColumn(long id) {
        return cmsColumnRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private PageTemplateDefinition requiredTemplate(PageTemplateKey key) {
        return pageTemplateRegistry.findByKey(key)
            .orElseThrow(() -> badRequest("页面模板不存在：" + key));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private BusinessException notFound(long id) {
        return new BusinessException(ErrorCode.NOT_FOUND, "栏目不存在：" + id);
    }

    private BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, message);
    }

    private BusinessException conflict(String message) {
        return new BusinessException(ErrorCode.CONFLICT, message);
    }

    private record TemplateSelection(
        PageTemplateKey pageTemplateKey,
        PageTemplateKey detailTemplateKey
    ) {
    }
}
