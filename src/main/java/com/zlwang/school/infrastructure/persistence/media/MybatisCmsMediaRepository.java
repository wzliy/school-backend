package com.zlwang.school.infrastructure.persistence.media;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.media.model.CmsMedia;
import com.zlwang.school.modules.media.model.MediaFileType;
import com.zlwang.school.modules.media.model.StorageType;
import com.zlwang.school.modules.media.repository.CmsMediaRepository;
import com.zlwang.school.modules.media.repository.CreateCmsMedia;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("!local")
public class MybatisCmsMediaRepository implements CmsMediaRepository {

    private final CmsMediaMapper cmsMediaMapper;

    public MybatisCmsMediaRepository(CmsMediaMapper cmsMediaMapper) {
        this.cmsMediaMapper = cmsMediaMapper;
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
        String type = name(fileType);
        String storage = name(storageType);
        long total = cmsMediaMapper.countMedia(keyword, type, storage, uploaderId);
        if (total == 0) {
            return PageResult.empty(pageNo, pageSize);
        }
        List<CmsMedia> records = cmsMediaMapper.findMedia(
            keyword,
            type,
            storage,
            uploaderId,
            (pageNo - 1) * pageSize,
            pageSize
        ).stream().map(this::toMedia).toList();
        return PageResult.of(records, total, pageNo, pageSize);
    }

    @Override
    public Optional<CmsMedia> findById(long id) {
        return Optional.ofNullable(cmsMediaMapper.findById(id)).map(this::toMedia);
    }

    @Override
    @Transactional
    public long create(CreateCmsMedia command) {
        cmsMediaMapper.insert(new CmsMediaWriteRow(
            command.storageType().name(),
            command.fileType().name(),
            command.originalName(),
            command.storedName(),
            command.extension(),
            command.mimeType(),
            command.fileSize(),
            command.filePath(),
            command.accessUrl(),
            command.uploaderId(),
            command.remark()
        ));
        return cmsMediaMapper.lastInsertId();
    }

    @Override
    public boolean delete(long id, long operatorId) {
        return cmsMediaMapper.delete(id, operatorId) > 0;
    }

    @Override
    public long countReferences(long id) {
        return cmsMediaMapper.countReferences(id);
    }

    private CmsMedia toMedia(CmsMediaRow row) {
        return new CmsMedia(
            row.id(),
            StorageType.valueOf(row.storageType()),
            MediaFileType.valueOf(row.fileType()),
            row.originalName(),
            row.storedName(),
            row.extension(),
            row.mimeType(),
            row.fileSize(),
            row.filePath(),
            row.accessUrl(),
            row.uploaderId(),
            row.remark(),
            row.createdAt(),
            row.updatedAt()
        );
    }

    private String name(Enum<?> value) {
        return value == null ? null : value.name();
    }
}
