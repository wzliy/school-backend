package com.zlwang.school.infrastructure.persistence.content;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.content.dto.ContentAttachmentRequest;
import com.zlwang.school.modules.content.model.AttachmentFileType;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentAttachment;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.content.repository.CmsContentRepository;
import com.zlwang.school.modules.content.repository.CreateCmsContent;
import com.zlwang.school.modules.content.repository.UpdateCmsContent;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Repository
@Profile("!local")
public class MybatisCmsContentRepository implements CmsContentRepository {

    private final CmsContentMapper cmsContentMapper;
    private final ObjectMapper objectMapper;

    public MybatisCmsContentRepository(CmsContentMapper cmsContentMapper, ObjectMapper objectMapper) {
        this.cmsContentMapper = cmsContentMapper;
        this.objectMapper = objectMapper;
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
        String site = name(siteType);
        String state = name(status);
        long total = cmsContentMapper.countContents(keyword, columnId, site, state);
        if (total == 0) {
            return PageResult.empty(pageNo, pageSize);
        }
        List<CmsContent> records = cmsContentMapper.findContents(
            keyword,
            columnId,
            site,
            state,
            (pageNo - 1) * pageSize,
            pageSize
        ).stream().map(row -> toContent(row, List.of())).toList();
        return PageResult.of(records, total, pageNo, pageSize);
    }

    @Override
    public Optional<CmsContent> findById(long id) {
        CmsContentRow row = cmsContentMapper.findById(id);
        if (row == null) {
            return Optional.empty();
        }
        List<ContentAttachment> attachments = cmsContentMapper.findAttachments(id).stream()
            .map(this::toAttachment)
            .toList();
        return Optional.of(toContent(row, attachments));
    }

    @Override
    public PageResult<CmsContent> findPublishedPage(
        long columnId,
        SiteType siteType,
        LocalDateTime publishedAt,
        long pageNo,
        long pageSize
    ) {
        String site = siteType.name();
        long total = cmsContentMapper.countPublishedPage(columnId, site, publishedAt);
        if (total == 0) {
            return PageResult.empty(pageNo, pageSize);
        }
        List<CmsContent> records = cmsContentMapper.findPublishedPage(
            columnId,
            site,
            publishedAt,
            (pageNo - 1) * pageSize,
            pageSize
        ).stream().map(row -> toContent(row, List.of())).toList();
        return PageResult.of(records, total, pageNo, pageSize);
    }

    @Override
    public List<CmsContent> findPublishedByColumn(
        long columnId,
        SiteType siteType,
        LocalDateTime publishedAt,
        int limit
    ) {
        return cmsContentMapper.findPublishedByColumn(
            columnId,
            siteType.name(),
            publishedAt,
            limit
        ).stream().map(row -> toContent(row, List.of())).toList();
    }

    @Override
    public List<CmsContent> findPublishedGallery(
        SiteType siteType,
        LocalDateTime publishedAt,
        int limit
    ) {
        return cmsContentMapper.findPublishedGallery(siteType.name(), publishedAt, limit).stream()
            .map(row -> toContent(row, List.of()))
            .toList();
    }

    @Override
    @Transactional
    public long create(CreateCmsContent command) {
        cmsContentMapper.insertContent(writeRow(command));
        long id = cmsContentMapper.lastInsertId();
        insertAttachments(id, command.attachments(), command.operatorId());
        return id;
    }

    @Override
    @Transactional
    public boolean update(UpdateCmsContent command) {
        if (cmsContentMapper.updateContent(command.id(), writeRow(command)) == 0) {
            return false;
        }
        cmsContentMapper.deleteAttachments(command.id(), command.operatorId());
        insertAttachments(command.id(), command.attachments(), command.operatorId());
        return true;
    }

    @Override
    public boolean publish(long id, LocalDateTime publishAt, long operatorId) {
        return cmsContentMapper.publish(id, publishAt, operatorId) > 0;
    }

    @Override
    public boolean offline(long id, long operatorId) {
        return cmsContentMapper.offline(id, operatorId) > 0;
    }

    @Override
    public boolean updateTop(long id, boolean topFlag, long operatorId) {
        return cmsContentMapper.updateTop(id, flag(topFlag), operatorId) > 0;
    }

    @Override
    public boolean updateRecommend(long id, boolean recommendFlag, long operatorId) {
        return cmsContentMapper.updateRecommend(id, flag(recommendFlag), operatorId) > 0;
    }

    @Override
    @Transactional
    public boolean delete(long id, long operatorId) {
        if (cmsContentMapper.deleteContent(id, operatorId) == 0) {
            return false;
        }
        cmsContentMapper.deleteAttachments(id, operatorId);
        return true;
    }

    private CmsContentWriteRow writeRow(CreateCmsContent command) {
        return new CmsContentWriteRow(
            command.columnId(),
            command.siteType().name(),
            command.title(),
            command.subtitle(),
            command.summary(),
            command.contentHtml(),
            command.coverUrl(),
            command.source(),
            command.author(),
            command.publishAt(),
            command.status().name(),
            flag(command.topFlag()),
            flag(command.recommendFlag()),
            command.sortNo(),
            command.seoTitle(),
            command.seoKeywords(),
            command.seoDescription(),
            json(command.extensionData()),
            command.operatorId()
        );
    }

    private CmsContentWriteRow writeRow(UpdateCmsContent command) {
        return new CmsContentWriteRow(
            command.columnId(),
            command.siteType().name(),
            command.title(),
            command.subtitle(),
            command.summary(),
            command.contentHtml(),
            command.coverUrl(),
            command.source(),
            command.author(),
            command.publishAt(),
            null,
            flag(command.topFlag()),
            flag(command.recommendFlag()),
            command.sortNo(),
            command.seoTitle(),
            command.seoKeywords(),
            command.seoDescription(),
            json(command.extensionData()),
            command.operatorId()
        );
    }

    private void insertAttachments(long contentId, List<ContentAttachmentRequest> requests, long operatorId) {
        if (requests.isEmpty()) {
            return;
        }
        List<CmsContentAttachmentWriteRow> rows = requests.stream()
            .map(request -> new CmsContentAttachmentWriteRow(
                request.mediaId(),
                request.fileName(),
                request.fileUrl(),
                request.fileSize(),
                request.fileType().name(),
                request.sortNo()
            ))
            .toList();
        cmsContentMapper.insertAttachments(contentId, rows, operatorId);
    }

    private CmsContent toContent(CmsContentRow row, List<ContentAttachment> attachments) {
        return new CmsContent(
            row.id(),
            row.columnId(),
            row.columnName(),
            SiteType.valueOf(row.siteType()),
            row.title(),
            row.subtitle(),
            row.summary(),
            row.contentHtml(),
            row.coverUrl(),
            row.source(),
            row.author(),
            row.publishAt(),
            ContentStatus.valueOf(row.status()),
            row.topFlag() == 1,
            row.recommendFlag() == 1,
            row.sortNo(),
            row.viewCount(),
            row.seoTitle(),
            row.seoKeywords(),
            row.seoDescription(),
            map(row.extensionData()),
            attachments,
            row.createdAt(),
            row.updatedAt()
        );
    }

    private ContentAttachment toAttachment(CmsContentAttachmentRow row) {
        return new ContentAttachment(
            row.id(),
            row.contentId(),
            row.mediaId(),
            row.fileName(),
            row.fileUrl(),
            row.fileSize(),
            AttachmentFileType.valueOf(row.fileType()),
            row.sortNo(),
            row.createdAt(),
            row.updatedAt()
        );
    }

    private String json(Map<String, Object> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("序列化内容扩展数据失败", ex);
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
            throw new IllegalStateException("解析内容扩展数据失败", ex);
        }
    }

    private String name(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private int flag(boolean value) {
        return value ? 1 : 0;
    }
}
