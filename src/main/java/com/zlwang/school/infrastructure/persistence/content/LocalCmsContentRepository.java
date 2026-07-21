package com.zlwang.school.infrastructure.persistence.content;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.infrastructure.persistence.local.LocalCmsStore;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.content.repository.CmsContentRepository;
import com.zlwang.school.modules.content.repository.CreateCmsContent;
import com.zlwang.school.modules.content.repository.UpdateCmsContent;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class LocalCmsContentRepository implements CmsContentRepository {

    private final LocalCmsStore localCmsStore;

    public LocalCmsContentRepository(LocalCmsStore localCmsStore) {
        this.localCmsStore = localCmsStore;
    }

    @Override
    public PageResult<CmsContent> findPage(
        String keyword,
        Long columnId,
        SiteType siteType,
        ContentStatus status,
        long pageNo,
        long pageSize
    ) {
        return localCmsStore.findContents(keyword, columnId, siteType, status, pageNo, pageSize);
    }

    @Override
    public Optional<CmsContent> findById(long id) {
        return localCmsStore.findContent(id);
    }

    @Override
    public List<CmsContent> findPublishedByColumn(
        long columnId,
        SiteType siteType,
        LocalDateTime publishedAt,
        int limit
    ) {
        return localCmsStore.findPublishedContents(columnId, siteType, publishedAt, limit);
    }

    @Override
    public List<CmsContent> findPublishedGallery(
        SiteType siteType,
        LocalDateTime publishedAt,
        int limit
    ) {
        return localCmsStore.findPublishedGallery(siteType, publishedAt, limit);
    }

    @Override
    public long create(CreateCmsContent command) {
        return localCmsStore.createContent(command);
    }

    @Override
    public boolean update(UpdateCmsContent command) {
        return localCmsStore.updateContent(command);
    }

    @Override
    public boolean publish(long id, LocalDateTime publishAt, long operatorId) {
        return localCmsStore.publishContent(id, publishAt);
    }

    @Override
    public boolean offline(long id, long operatorId) {
        return localCmsStore.offlineContent(id);
    }

    @Override
    public boolean updateTop(long id, boolean topFlag, long operatorId) {
        return localCmsStore.updateContentTop(id, topFlag);
    }

    @Override
    public boolean updateRecommend(long id, boolean recommendFlag, long operatorId) {
        return localCmsStore.updateContentRecommend(id, recommendFlag);
    }

    @Override
    public boolean delete(long id, long operatorId) {
        return localCmsStore.deleteContent(id);
    }
}
