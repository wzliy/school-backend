package com.zlwang.school.infrastructure.persistence.column;

import com.zlwang.school.infrastructure.persistence.local.LocalCmsStore;
import com.zlwang.school.modules.column.dto.ColumnSortItem;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CmsColumnRepository;
import com.zlwang.school.modules.column.repository.CreateCmsColumn;
import com.zlwang.school.modules.column.repository.UpdateCmsColumn;
import com.zlwang.school.modules.template.model.SiteType;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class LocalCmsColumnRepository implements CmsColumnRepository {

    private final LocalCmsStore localCmsStore;

    public LocalCmsColumnRepository(LocalCmsStore localCmsStore) {
        this.localCmsStore = localCmsStore;
    }

    @Override
    public List<CmsColumn> findAll(SiteType siteType) {
        return localCmsStore.findColumns(siteType);
    }

    @Override
    public Optional<CmsColumn> findById(long id) {
        return localCmsStore.findColumn(id);
    }

    @Override
    public boolean existsByCode(SiteType siteType, String columnCode, Long excludeId) {
        return localCmsStore.columnCodeExists(siteType, columnCode, excludeId);
    }

    @Override
    public long countChildren(long id) {
        return localCmsStore.countChildColumns(id);
    }

    @Override
    public long countContents(long id) {
        return localCmsStore.countContents(id);
    }

    @Override
    public long countPageSections(long id) {
        return localCmsStore.countPageSections(id);
    }

    @Override
    public long create(CreateCmsColumn command) {
        return localCmsStore.createColumn(command);
    }

    @Override
    public boolean update(UpdateCmsColumn command) {
        return localCmsStore.updateColumn(command);
    }

    @Override
    public boolean updateStatus(long id, boolean enabled, long operatorId) {
        return localCmsStore.updateColumnStatus(id, enabled);
    }

    @Override
    public void updateSort(List<ColumnSortItem> items, long operatorId) {
        localCmsStore.updateColumnSort(items);
    }

    @Override
    public boolean delete(long id, long operatorId) {
        return localCmsStore.deleteColumn(id);
    }
}
