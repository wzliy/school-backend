package com.zlwang.school.modules.media.repository;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.media.model.CmsMedia;
import com.zlwang.school.modules.media.model.MediaFileType;
import com.zlwang.school.modules.media.model.StorageType;
import java.util.Optional;

public interface CmsMediaRepository {

    PageResult<CmsMedia> findPage(
        String keyword,
        MediaFileType fileType,
        StorageType storageType,
        Long uploaderId,
        long pageNo,
        long pageSize
    );

    Optional<CmsMedia> findById(long id);

    long create(CreateCmsMedia command);

    boolean delete(long id, long operatorId);

    long countReferences(long id);
}
