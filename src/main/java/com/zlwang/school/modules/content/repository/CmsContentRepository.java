package com.zlwang.school.modules.content.repository;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CmsContentRepository {

    PageResult<CmsContent> findPage(
        String keyword,
        Long columnId,
        SiteType siteType,
        ContentStatus status,
        long pageNo,
        long pageSize
    );

    Optional<CmsContent> findById(long id);

    long create(CreateCmsContent command);

    boolean update(UpdateCmsContent command);

    boolean publish(long id, LocalDateTime publishAt, long operatorId);

    boolean offline(long id, long operatorId);

    boolean updateTop(long id, boolean topFlag, long operatorId);

    boolean updateRecommend(long id, boolean recommendFlag, long operatorId);

    boolean delete(long id, long operatorId);
}
