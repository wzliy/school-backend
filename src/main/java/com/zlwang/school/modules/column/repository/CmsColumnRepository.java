package com.zlwang.school.modules.column.repository;

import com.zlwang.school.modules.column.dto.ColumnSortItem;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.template.model.SiteType;
import java.util.List;
import java.util.Optional;

public interface CmsColumnRepository {

    List<CmsColumn> findAll(SiteType siteType);

    Optional<CmsColumn> findById(long id);

    boolean existsByCode(SiteType siteType, String columnCode, Long excludeId);

    long countChildren(long id);

    long countContents(long id);

    long countPageSections(long id);

    long create(CreateCmsColumn command);

    boolean update(UpdateCmsColumn command);

    boolean updateStatus(long id, boolean enabled, long operatorId);

    void updateSort(List<ColumnSortItem> items, long operatorId);

    boolean delete(long id, long operatorId);
}
