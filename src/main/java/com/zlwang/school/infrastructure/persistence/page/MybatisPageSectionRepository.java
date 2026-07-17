package com.zlwang.school.infrastructure.persistence.page;

import com.zlwang.school.modules.page.model.PageCode;
import com.zlwang.school.modules.page.model.PageSection;
import com.zlwang.school.modules.page.model.PageSectionType;
import com.zlwang.school.modules.page.repository.PageSectionRepository;
import com.zlwang.school.modules.page.repository.SavePageSection;
import com.zlwang.school.modules.template.model.SiteType;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Repository
@Profile("!local")
public class MybatisPageSectionRepository implements PageSectionRepository {

    private final PageSectionMapper pageSectionMapper;
    private final ObjectMapper objectMapper;

    public MybatisPageSectionRepository(PageSectionMapper pageSectionMapper, ObjectMapper objectMapper) {
        this.pageSectionMapper = pageSectionMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<PageSection> findAll(SiteType siteType, PageCode pageCode) {
        return pageSectionMapper.findAll(siteType.name(), pageCode.name()).stream()
            .map(this::toSection)
            .toList();
    }

    @Override
    @Transactional
    public void replace(
        SiteType siteType,
        PageCode pageCode,
        List<SavePageSection> sections,
        long operatorId
    ) {
        for (SavePageSection section : sections) {
            PageSectionWriteRow row = writeRow(section, operatorId);
            Long id = pageSectionMapper.findAnyIdByCode(
                siteType.name(),
                pageCode.name(),
                section.sectionCode()
            );
            if (id == null) {
                pageSectionMapper.insert(row);
            } else {
                pageSectionMapper.update(id, row);
            }
        }
    }

    private PageSection toSection(PageSectionRow row) {
        return new PageSection(
            row.id(),
            SiteType.valueOf(row.siteType()),
            PageCode.valueOf(row.pageCode()),
            row.sectionCode(),
            row.sectionName(),
            PageSectionType.valueOf(row.sectionType()),
            row.dataSourceColumnId(),
            row.displayCount(),
            row.displayStyle(),
            map(row.configJson()),
            row.sortNo(),
            row.enabled() == 1,
            row.createdAt(),
            row.updatedAt()
        );
    }

    private PageSectionWriteRow writeRow(SavePageSection section, long operatorId) {
        return new PageSectionWriteRow(
            section.siteType().name(),
            section.pageCode().name(),
            section.sectionCode(),
            section.sectionName(),
            section.sectionType().name(),
            section.dataSourceColumnId(),
            section.displayCount(),
            section.displayStyle(),
            json(section.config()),
            section.sortNo(),
            section.enabled() ? 1 : 0,
            operatorId
        );
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("序列化页面区块配置失败", ex);
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
            throw new IllegalStateException("解析页面区块配置失败", ex);
        }
    }
}
