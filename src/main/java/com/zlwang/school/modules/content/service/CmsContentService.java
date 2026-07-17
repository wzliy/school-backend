package com.zlwang.school.modules.content.service;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.banner.model.BannerLinkType;
import com.zlwang.school.modules.banner.repository.CmsBannerRepository;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CmsColumnRepository;
import com.zlwang.school.modules.content.dto.ContentAttachmentRequest;
import com.zlwang.school.modules.content.dto.ContentPageQuery;
import com.zlwang.school.modules.content.dto.CreateContentRequest;
import com.zlwang.school.modules.content.dto.PublishContentRequest;
import com.zlwang.school.modules.content.dto.UpdateContentRecommendRequest;
import com.zlwang.school.modules.content.dto.UpdateContentRequest;
import com.zlwang.school.modules.content.dto.UpdateContentTopRequest;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.content.repository.CmsContentRepository;
import com.zlwang.school.modules.content.repository.CreateCmsContent;
import com.zlwang.school.modules.content.repository.UpdateCmsContent;
import com.zlwang.school.modules.content.vo.ContentDetailResponse;
import com.zlwang.school.modules.content.vo.ContentSummaryResponse;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class CmsContentService {

    private static final int MAX_ATTACHMENTS = 50;
    private static final int MAX_SORT_NO = 999_999;

    private final CmsContentRepository cmsContentRepository;
    private final CmsColumnRepository cmsColumnRepository;
    private final CmsBannerRepository cmsBannerRepository;
    private final ContentTemplateValidator contentTemplateValidator;
    private final RichTextSanitizer richTextSanitizer;

    public CmsContentService(
        CmsContentRepository cmsContentRepository,
        CmsColumnRepository cmsColumnRepository,
        CmsBannerRepository cmsBannerRepository,
        ContentTemplateValidator contentTemplateValidator,
        RichTextSanitizer richTextSanitizer
    ) {
        this.cmsContentRepository = cmsContentRepository;
        this.cmsColumnRepository = cmsColumnRepository;
        this.cmsBannerRepository = cmsBannerRepository;
        this.contentTemplateValidator = contentTemplateValidator;
        this.richTextSanitizer = richTextSanitizer;
    }

    public PageResult<ContentSummaryResponse> findPage(ContentPageQuery query) {
        PageResult<CmsContent> page = cmsContentRepository.findPage(
            normalize(query.getKeyword()),
            query.getColumnId(),
            query.getSiteType(),
            query.getStatus(),
            query.getPageNo(),
            query.getPageSize()
        );
        return PageResult.of(
            page.records().stream().map(ContentSummaryResponse::from).toList(),
            page.total(),
            page.pageNo(),
            page.pageSize()
        );
    }

    public ContentDetailResponse findById(long id) {
        return ContentDetailResponse.from(requiredContent(id));
    }

    public long create(CreateContentRequest request, long operatorId) {
        CmsColumn column = requiredContentColumn(request.columnId());
        validateWriteLimits(request.sortNo(), request.attachments());
        Map<String, Object> extensionData = contentTemplateValidator.validateExtensionData(
            column,
            request.extensionData(),
            false
        );
        return cmsContentRepository.create(new CreateCmsContent(
            column.id(),
            column.siteType(),
            request.title().trim(),
            normalize(request.subtitle()),
            normalize(request.summary()),
            richTextSanitizer.sanitize(request.contentHtml()),
            normalize(request.coverUrl()),
            normalize(request.source()),
            normalize(request.author()),
            request.publishAt(),
            ContentStatus.DRAFT,
            request.topFlag(),
            request.recommendFlag(),
            request.sortNo(),
            normalize(request.seoTitle()),
            normalize(request.seoKeywords()),
            normalize(request.seoDescription()),
            extensionData,
            normalizeAttachments(request.attachments()),
            operatorId
        ));
    }

    public void update(long id, UpdateContentRequest request, long operatorId) {
        CmsContent existing = requiredContent(id);
        CmsColumn column = requiredContentColumn(request.columnId());
        if (existing.siteType() != column.siteType()
            && cmsBannerRepository.countReferences(BannerLinkType.CONTENT, id, false) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "内容被 Banner 引用，不能跨站点移动");
        }
        validateWriteLimits(request.sortNo(), request.attachments());
        Map<String, Object> extensionData = contentTemplateValidator.validateExtensionData(
            column,
            request.extensionData(),
            existing.status() == ContentStatus.PUBLISHED
        );
        LocalDateTime publishAt = existing.status() == ContentStatus.PUBLISHED && request.publishAt() == null
            ? existing.publishAt()
            : request.publishAt();
        CmsContent candidate = new CmsContent(
            existing.id(),
            column.id(),
            column.columnName(),
            column.siteType(),
            request.title().trim(),
            normalize(request.subtitle()),
            normalize(request.summary()),
            richTextSanitizer.sanitize(request.contentHtml()),
            normalize(request.coverUrl()),
            normalize(request.source()),
            normalize(request.author()),
            publishAt,
            existing.status(),
            request.topFlag(),
            request.recommendFlag(),
            request.sortNo(),
            existing.viewCount(),
            normalize(request.seoTitle()),
            normalize(request.seoKeywords()),
            normalize(request.seoDescription()),
            extensionData,
            existing.attachments(),
            existing.createdAt(),
            existing.updatedAt()
        );
        if (candidate.status() == ContentStatus.PUBLISHED) {
            validatePublishable(column, candidate, publishAt);
        }
        boolean updated = cmsContentRepository.update(new UpdateCmsContent(
            id,
            column.id(),
            column.siteType(),
            candidate.title(),
            candidate.subtitle(),
            candidate.summary(),
            candidate.contentHtml(),
            candidate.coverUrl(),
            candidate.source(),
            candidate.author(),
            candidate.publishAt(),
            candidate.topFlag(),
            candidate.recommendFlag(),
            candidate.sortNo(),
            candidate.seoTitle(),
            candidate.seoKeywords(),
            candidate.seoDescription(),
            extensionData,
            normalizeAttachments(request.attachments()),
            operatorId
        ));
        if (!updated) {
            throw notFound(id);
        }
    }

    public void publish(long id, PublishContentRequest request, long operatorId) {
        CmsContent content = requiredContent(id);
        CmsColumn column = requiredContentColumn(content.columnId());
        LocalDateTime publishAt = request.publishAt() == null ? LocalDateTime.now() : request.publishAt();
        validatePublishable(column, content, publishAt);
        if (!cmsContentRepository.publish(id, publishAt, operatorId)) {
            throw notFound(id);
        }
    }

    public void offline(long id, long operatorId) {
        requiredContent(id);
        if (cmsBannerRepository.countReferences(BannerLinkType.CONTENT, id, true) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "内容被启用 Banner 引用，不能下线");
        }
        if (!cmsContentRepository.offline(id, operatorId)) {
            throw notFound(id);
        }
    }

    public void updateTop(long id, UpdateContentTopRequest request, long operatorId) {
        requiredContent(id);
        if (!cmsContentRepository.updateTop(id, request.topFlag(), operatorId)) {
            throw notFound(id);
        }
    }

    public void updateRecommend(long id, UpdateContentRecommendRequest request, long operatorId) {
        requiredContent(id);
        if (!cmsContentRepository.updateRecommend(id, request.recommendFlag(), operatorId)) {
            throw notFound(id);
        }
    }

    public void delete(long id, long operatorId) {
        requiredContent(id);
        if (cmsBannerRepository.countReferences(BannerLinkType.CONTENT, id, false) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "内容被 Banner 引用，不能删除");
        }
        if (!cmsContentRepository.delete(id, operatorId)) {
            throw notFound(id);
        }
    }

    private CmsContent requiredContent(long id) {
        return cmsContentRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private CmsColumn requiredContentColumn(long columnId) {
        CmsColumn column = cmsColumnRepository.findById(columnId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "栏目不存在：" + columnId));
        contentTemplateValidator.contentTemplate(column);
        return column;
    }

    private void validatePublishable(CmsColumn column, CmsContent content, LocalDateTime publishAt) {
        if (!column.enabled()) {
            throw new BusinessException(ErrorCode.CONFLICT, "栏目已停用，不能发布内容");
        }
        contentTemplateValidator.validatePublishedContent(column, content, publishAt);
    }

    private void validateWriteLimits(int sortNo, List<ContentAttachmentRequest> attachments) {
        if (sortNo > MAX_SORT_NO) {
            throw new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, "sortNo 不能大于 " + MAX_SORT_NO);
        }
        if (attachments.size() > MAX_ATTACHMENTS) {
            throw new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, "附件数量不能超过 " + MAX_ATTACHMENTS);
        }
    }

    private List<ContentAttachmentRequest> normalizeAttachments(List<ContentAttachmentRequest> attachments) {
        return attachments.stream()
            .map(attachment -> new ContentAttachmentRequest(
                attachment.mediaId(),
                attachment.fileName().trim(),
                attachment.fileUrl().trim(),
                attachment.fileSize(),
                attachment.fileType(),
                attachment.sortNo()
            ))
            .sorted(Comparator.comparingInt(ContentAttachmentRequest::sortNo))
            .toList();
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private BusinessException notFound(long id) {
        return new BusinessException(ErrorCode.NOT_FOUND, "内容不存在：" + id);
    }
}
