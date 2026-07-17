package com.zlwang.school.infrastructure.persistence.column;

import com.zlwang.school.modules.column.dto.ColumnSortItem;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CmsColumnRepository;
import com.zlwang.school.modules.column.repository.CreateCmsColumn;
import com.zlwang.school.modules.column.repository.UpdateCmsColumn;
import com.zlwang.school.modules.template.model.ColumnType;
import com.zlwang.school.modules.template.model.PageTemplateKey;
import com.zlwang.school.modules.template.model.SiteType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Repository
@Profile("!local")
public class MybatisCmsColumnRepository implements CmsColumnRepository {

    private final CmsColumnMapper cmsColumnMapper;
    private final ObjectMapper objectMapper;

    public MybatisCmsColumnRepository(CmsColumnMapper cmsColumnMapper, ObjectMapper objectMapper) {
        this.cmsColumnMapper = cmsColumnMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CmsColumn> findAll(SiteType siteType) {
        return cmsColumnMapper.findAll(siteType == null ? null : siteType.name()).stream()
            .map(this::toColumn)
            .toList();
    }

    @Override
    public Optional<CmsColumn> findById(long id) {
        return Optional.ofNullable(cmsColumnMapper.findById(id)).map(this::toColumn);
    }

    @Override
    public boolean existsByCode(SiteType siteType, String columnCode, Long excludeId) {
        return cmsColumnMapper.countByCode(siteType.name(), columnCode, excludeId) > 0;
    }

    @Override
    public long countChildren(long id) {
        return cmsColumnMapper.countChildren(id);
    }

    @Override
    public long countContents(long id) {
        return cmsColumnMapper.countContents(id);
    }

    @Override
    public long create(CreateCmsColumn command) {
        cmsColumnMapper.insert(
            command.parentId(),
            command.siteType().name(),
            command.columnName(),
            command.columnCode(),
            command.columnType().name(),
            command.routePath(),
            command.externalUrl(),
            name(command.templateKey()),
            name(command.detailTemplateKey()),
            json(command.templateConfig()),
            command.coverUrl(),
            command.sortNo(),
            flag(command.navVisible()),
            flag(command.enabled()),
            command.seoTitle(),
            command.seoKeywords(),
            command.seoDescription(),
            command.remark(),
            command.operatorId()
        );
        Long id = cmsColumnMapper.findIdByCode(command.siteType().name(), command.columnCode());
        if (id == null) {
            throw new IllegalStateException("新增栏目后未查询到栏目 ID");
        }
        return id;
    }

    @Override
    public boolean update(UpdateCmsColumn command) {
        return cmsColumnMapper.update(
            command.id(),
            command.parentId(),
            command.columnName(),
            command.columnCode(),
            command.columnType().name(),
            command.routePath(),
            command.externalUrl(),
            name(command.templateKey()),
            name(command.detailTemplateKey()),
            json(command.templateConfig()),
            command.coverUrl(),
            command.sortNo(),
            flag(command.navVisible()),
            flag(command.enabled()),
            command.seoTitle(),
            command.seoKeywords(),
            command.seoDescription(),
            command.remark(),
            command.operatorId()
        ) > 0;
    }

    @Override
    public boolean updateStatus(long id, boolean enabled, long operatorId) {
        return cmsColumnMapper.updateStatus(id, flag(enabled), operatorId) > 0;
    }

    @Override
    @Transactional
    public void updateSort(List<ColumnSortItem> items, long operatorId) {
        items.forEach(item -> cmsColumnMapper.updateSort(item.id(), item.parentId(), item.sortNo(), operatorId));
    }

    @Override
    public boolean delete(long id, long operatorId) {
        return cmsColumnMapper.delete(id, operatorId) > 0;
    }

    private CmsColumn toColumn(CmsColumnRow row) {
        return new CmsColumn(
            row.id(),
            row.parentId(),
            SiteType.valueOf(row.siteType()),
            row.columnName(),
            row.columnCode(),
            ColumnType.valueOf(row.columnType()),
            row.routePath(),
            row.externalUrl(),
            key(row.templateKey()),
            key(row.detailTemplateKey()),
            map(row.templateConfig()),
            row.coverUrl(),
            row.sortNo(),
            row.navVisible() == 1,
            row.enabled() == 1,
            row.seoTitle(),
            row.seoKeywords(),
            row.seoDescription(),
            row.remark(),
            row.createdAt(),
            row.updatedAt()
        );
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("序列化栏目模板配置失败", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception ex) {
            throw new IllegalStateException("解析栏目模板配置失败", ex);
        }
    }

    private String name(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private PageTemplateKey key(String value) {
        return value == null ? null : PageTemplateKey.valueOf(value);
    }

    private int flag(boolean value) {
        return value ? 1 : 0;
    }
}
