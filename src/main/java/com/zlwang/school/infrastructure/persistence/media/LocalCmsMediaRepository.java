package com.zlwang.school.infrastructure.persistence.media;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.infrastructure.persistence.local.LocalCmsStore;
import com.zlwang.school.modules.media.model.CmsMedia;
import com.zlwang.school.modules.media.model.MediaFileType;
import com.zlwang.school.modules.media.model.StorageType;
import com.zlwang.school.modules.media.repository.CmsMediaRepository;
import com.zlwang.school.modules.media.repository.CreateCmsMedia;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class LocalCmsMediaRepository implements CmsMediaRepository {

    private final LocalCmsStore localCmsStore;

    public LocalCmsMediaRepository(LocalCmsStore localCmsStore) {
        this.localCmsStore = localCmsStore;
    }

    @Override
    public PageResult<CmsMedia> findPage(
        String keyword,
        MediaFileType fileType,
        StorageType storageType,
        Long uploaderId,
        long pageNo,
        long pageSize
    ) {
        return localCmsStore.findMedia(keyword, fileType, storageType, uploaderId, pageNo, pageSize);
    }

    @Override
    public Optional<CmsMedia> findById(long id) {
        return localCmsStore.findMedia(id);
    }

    @Override
    public long create(CreateCmsMedia command) {
        return localCmsStore.createMedia(command);
    }

    @Override
    public boolean delete(long id, long operatorId) {
        return localCmsStore.deleteMedia(id);
    }

    @Override
    public long countReferences(long id) {
        return localCmsStore.countMediaReferences(id);
    }
}
