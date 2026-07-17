package com.zlwang.school.infrastructure.persistence.local;

import static com.zlwang.school.modules.template.model.ColumnType.LIST;
import static com.zlwang.school.modules.template.model.ColumnType.PAGE;
import static com.zlwang.school.modules.template.model.PageTemplateKey.ARTICLE_DETAIL;
import static com.zlwang.school.modules.template.model.PageTemplateKey.ARTICLE_LIST;
import static com.zlwang.school.modules.template.model.PageTemplateKey.ORGANIZATION;
import static com.zlwang.school.modules.template.model.PageTemplateKey.SERVICE_DIRECTORY;
import static com.zlwang.school.modules.template.model.PageTemplateKey.SINGLE_PAGE;
import static com.zlwang.school.modules.template.model.SiteType.MAIN_SITE;
import static com.zlwang.school.modules.template.model.SiteType.RECRUIT_SITE;

import com.zlwang.school.modules.column.dto.ColumnSortItem;
import com.zlwang.school.modules.column.model.CmsColumn;
import com.zlwang.school.modules.column.repository.CreateCmsColumn;
import com.zlwang.school.modules.column.repository.UpdateCmsColumn;
import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.content.dto.ContentAttachmentRequest;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentAttachment;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.content.repository.CreateCmsContent;
import com.zlwang.school.modules.content.repository.UpdateCmsContent;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalCmsStore {

    private final AtomicLong columnIdSequence = new AtomicLong(203L);
    private final AtomicLong contentIdSequence = new AtomicLong();
    private final AtomicLong attachmentIdSequence = new AtomicLong();
    private final Map<Long, CmsColumn> columns = new LinkedHashMap<>();
    private final Map<Long, CmsContent> contents = new LinkedHashMap<>();
    private final Map<Long, List<ContentAttachment>> attachments = new LinkedHashMap<>();

    public LocalCmsStore() {
        LocalDateTime now = LocalDateTime.now();
        add(column(100, MAIN_SITE, "学校概况", "about", PAGE, "/about", SINGLE_PAGE, null,
            Map.of("page", Map.of()), 10, now));
        add(column(101, MAIN_SITE, "新闻中心", "news", LIST, "/news", ARTICLE_LIST, ARTICLE_DETAIL,
            articleConfig(), 20, now));
        add(column(102, MAIN_SITE, "通知公告", "notice", LIST, "/notice", ARTICLE_LIST, ARTICLE_DETAIL,
            articleConfig(), 30, now));
        add(column(103, MAIN_SITE, "机构设置", "organization", LIST, "/organization", ORGANIZATION, null,
            Map.of("page", Map.of("displayStyle", "GROUPED")), 40, now));
        add(column(104, MAIN_SITE, "教育教学", "education", LIST, "/education", ARTICLE_LIST,
            ARTICLE_DETAIL, articleConfig(), 50, now));
        add(column(105, MAIN_SITE, "学生工作", "student-work", LIST, "/student-work", ARTICLE_LIST,
            ARTICLE_DETAIL, articleConfig(), 60, now));
        add(column(106, MAIN_SITE, "公共服务", "service", LIST, "/service", SERVICE_DIRECTORY, null,
            Map.of("page", Map.of("displayStyle", "ICON_GRID")), 70, now));
        add(column(200, RECRUIT_SITE, "招生信息", "admission", LIST, "/recruit/admission", ARTICLE_LIST,
            ARTICLE_DETAIL, articleConfig(), 10, now));
        add(column(201, RECRUIT_SITE, "就业信息", "employment", LIST, "/recruit/employment", ARTICLE_LIST,
            ARTICLE_DETAIL, articleConfig(), 20, now));
        add(column(202, RECRUIT_SITE, "校企合作", "school-enterprise", LIST, "/recruit/school-enterprise",
            ARTICLE_LIST, ARTICLE_DETAIL, articleConfig(), 30, now));
        add(column(203, RECRUIT_SITE, "政策公告", "recruit-policy", LIST, "/recruit/policy", ARTICLE_LIST,
            ARTICLE_DETAIL, articleConfig(), 40, now));
    }

    public synchronized List<CmsColumn> findColumns(SiteType siteType) {
        return columns.values().stream()
            .filter(column -> siteType == null || column.siteType() == siteType)
            .toList();
    }

    public synchronized Optional<CmsColumn> findColumn(long id) {
        return Optional.ofNullable(columns.get(id));
    }

    public synchronized boolean columnCodeExists(SiteType siteType, String code, Long excludeId) {
        return columns.values().stream().anyMatch(column -> column.siteType() == siteType
            && column.columnCode().equals(code)
            && (excludeId == null || column.id() != excludeId));
    }

    public synchronized long countChildColumns(long id) {
        return columns.values().stream().filter(column -> column.parentId() == id).count();
    }

    public synchronized long countContents(long columnId) {
        return contents.values().stream().filter(content -> content.columnId() == columnId).count();
    }

    public synchronized PageResult<CmsContent> findContents(
        String keyword,
        Long columnId,
        SiteType siteType,
        ContentStatus status,
        long pageNo,
        long pageSize
    ) {
        String normalizedKeyword = keyword == null ? null : keyword.toLowerCase(Locale.ROOT);
        List<CmsContent> matched = contents.values().stream()
            .filter(content -> columnId == null || content.columnId() == columnId)
            .filter(content -> siteType == null || content.siteType() == siteType)
            .filter(content -> status == null || content.status() == status)
            .filter(content -> normalizedKeyword == null || containsKeyword(content, normalizedKeyword))
            .sorted(Comparator.comparing(CmsContent::topFlag).reversed()
                .thenComparingInt(CmsContent::sortNo)
                .thenComparing(CmsContent::publishAt, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(CmsContent::id, Comparator.reverseOrder()))
            .toList();
        long offset = (pageNo - 1) * pageSize;
        if (offset >= matched.size()) {
            return PageResult.empty(pageNo, pageSize);
        }
        int fromIndex = Math.toIntExact(offset);
        int toIndex = Math.min(matched.size(), Math.toIntExact(offset + pageSize));
        return PageResult.of(matched.subList(fromIndex, toIndex), matched.size(), pageNo, pageSize);
    }

    public synchronized Optional<CmsContent> findContent(long id) {
        return Optional.ofNullable(contents.get(id)).map(this::withAttachments);
    }

    public synchronized long createContent(CreateCmsContent command) {
        long id = contentIdSequence.incrementAndGet();
        LocalDateTime now = LocalDateTime.now();
        CmsColumn column = columns.get(command.columnId());
        contents.put(id, new CmsContent(
            id,
            command.columnId(),
            column == null ? null : column.columnName(),
            command.siteType(),
            command.title(),
            command.subtitle(),
            command.summary(),
            command.contentHtml(),
            command.coverUrl(),
            command.source(),
            command.author(),
            command.publishAt(),
            command.status(),
            command.topFlag(),
            command.recommendFlag(),
            command.sortNo(),
            0L,
            command.seoTitle(),
            command.seoKeywords(),
            command.seoDescription(),
            command.extensionData(),
            List.of(),
            now,
            now
        ));
        replaceAttachments(id, command.attachments(), now);
        return id;
    }

    public synchronized boolean updateContent(UpdateCmsContent command) {
        CmsContent existing = contents.get(command.id());
        if (existing == null) {
            return false;
        }
        CmsColumn column = columns.get(command.columnId());
        LocalDateTime now = LocalDateTime.now();
        contents.put(command.id(), new CmsContent(
            existing.id(),
            command.columnId(),
            column == null ? null : column.columnName(),
            command.siteType(),
            command.title(),
            command.subtitle(),
            command.summary(),
            command.contentHtml(),
            command.coverUrl(),
            command.source(),
            command.author(),
            command.publishAt(),
            existing.status(),
            command.topFlag(),
            command.recommendFlag(),
            command.sortNo(),
            existing.viewCount(),
            command.seoTitle(),
            command.seoKeywords(),
            command.seoDescription(),
            command.extensionData(),
            List.of(),
            existing.createdAt(),
            now
        ));
        replaceAttachments(command.id(), command.attachments(), now);
        return true;
    }

    public synchronized boolean publishContent(long id, LocalDateTime publishAt) {
        CmsContent existing = contents.get(id);
        if (existing == null) {
            return false;
        }
        contents.put(id, copyContent(
            existing,
            publishAt,
            ContentStatus.PUBLISHED,
            existing.topFlag(),
            existing.recommendFlag()
        ));
        return true;
    }

    public synchronized boolean offlineContent(long id) {
        CmsContent existing = contents.get(id);
        if (existing == null) {
            return false;
        }
        contents.put(id, copyContent(
            existing,
            existing.publishAt(),
            ContentStatus.OFFLINE,
            existing.topFlag(),
            existing.recommendFlag()
        ));
        return true;
    }

    public synchronized boolean updateContentTop(long id, boolean topFlag) {
        CmsContent existing = contents.get(id);
        if (existing == null) {
            return false;
        }
        contents.put(id, copyContent(
            existing,
            existing.publishAt(),
            existing.status(),
            topFlag,
            existing.recommendFlag()
        ));
        return true;
    }

    public synchronized boolean updateContentRecommend(long id, boolean recommendFlag) {
        CmsContent existing = contents.get(id);
        if (existing == null) {
            return false;
        }
        contents.put(id, copyContent(
            existing,
            existing.publishAt(),
            existing.status(),
            existing.topFlag(),
            recommendFlag
        ));
        return true;
    }

    public synchronized boolean deleteContent(long id) {
        attachments.remove(id);
        return contents.remove(id) != null;
    }

    public synchronized long createColumn(CreateCmsColumn command) {
        long id = columnIdSequence.incrementAndGet();
        LocalDateTime now = LocalDateTime.now();
        columns.put(id, new CmsColumn(
            id,
            command.parentId(),
            command.siteType(),
            command.columnName(),
            command.columnCode(),
            command.columnType(),
            command.routePath(),
            command.externalUrl(),
            command.templateKey(),
            command.detailTemplateKey(),
            command.templateConfig(),
            command.coverUrl(),
            command.sortNo(),
            command.navVisible(),
            command.enabled(),
            command.seoTitle(),
            command.seoKeywords(),
            command.seoDescription(),
            command.remark(),
            now,
            now
        ));
        return id;
    }

    public synchronized boolean updateColumn(UpdateCmsColumn command) {
        CmsColumn existing = columns.get(command.id());
        if (existing == null) {
            return false;
        }
        columns.put(command.id(), new CmsColumn(
            existing.id(),
            command.parentId(),
            existing.siteType(),
            command.columnName(),
            command.columnCode(),
            command.columnType(),
            command.routePath(),
            command.externalUrl(),
            command.templateKey(),
            command.detailTemplateKey(),
            command.templateConfig(),
            command.coverUrl(),
            command.sortNo(),
            command.navVisible(),
            command.enabled(),
            command.seoTitle(),
            command.seoKeywords(),
            command.seoDescription(),
            command.remark(),
            existing.createdAt(),
            LocalDateTime.now()
        ));
        return true;
    }

    public synchronized boolean updateColumnStatus(long id, boolean enabled) {
        CmsColumn existing = columns.get(id);
        if (existing == null) {
            return false;
        }
        columns.put(id, copy(existing, existing.parentId(), existing.sortNo(), enabled));
        return true;
    }

    public synchronized void updateColumnSort(List<ColumnSortItem> items) {
        for (ColumnSortItem item : items) {
            CmsColumn existing = columns.get(item.id());
            if (existing != null) {
                columns.put(item.id(), copy(existing, item.parentId(), item.sortNo(), existing.enabled()));
            }
        }
    }

    public synchronized boolean deleteColumn(long id) {
        return columns.remove(id) != null;
    }

    private CmsColumn copy(CmsColumn column, long parentId, int sortNo, boolean enabled) {
        return new CmsColumn(
            column.id(),
            parentId,
            column.siteType(),
            column.columnName(),
            column.columnCode(),
            column.columnType(),
            column.routePath(),
            column.externalUrl(),
            column.templateKey(),
            column.detailTemplateKey(),
            column.templateConfig(),
            column.coverUrl(),
            sortNo,
            column.navVisible(),
            enabled,
            column.seoTitle(),
            column.seoKeywords(),
            column.seoDescription(),
            column.remark(),
            column.createdAt(),
            LocalDateTime.now()
        );
    }

    private boolean containsKeyword(CmsContent content, String keyword) {
        return contains(content.title(), keyword)
            || contains(content.subtitle(), keyword)
            || contains(content.summary(), keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(keyword);
    }

    private CmsContent withAttachments(CmsContent content) {
        return new CmsContent(
            content.id(),
            content.columnId(),
            content.columnName(),
            content.siteType(),
            content.title(),
            content.subtitle(),
            content.summary(),
            content.contentHtml(),
            content.coverUrl(),
            content.source(),
            content.author(),
            content.publishAt(),
            content.status(),
            content.topFlag(),
            content.recommendFlag(),
            content.sortNo(),
            content.viewCount(),
            content.seoTitle(),
            content.seoKeywords(),
            content.seoDescription(),
            content.extensionData(),
            attachments.getOrDefault(content.id(), List.of()),
            content.createdAt(),
            content.updatedAt()
        );
    }

    private CmsContent copyContent(
        CmsContent content,
        LocalDateTime publishAt,
        ContentStatus status,
        boolean topFlag,
        boolean recommendFlag
    ) {
        return new CmsContent(
            content.id(),
            content.columnId(),
            content.columnName(),
            content.siteType(),
            content.title(),
            content.subtitle(),
            content.summary(),
            content.contentHtml(),
            content.coverUrl(),
            content.source(),
            content.author(),
            publishAt,
            status,
            topFlag,
            recommendFlag,
            content.sortNo(),
            content.viewCount(),
            content.seoTitle(),
            content.seoKeywords(),
            content.seoDescription(),
            content.extensionData(),
            List.of(),
            content.createdAt(),
            LocalDateTime.now()
        );
    }

    private void replaceAttachments(long contentId, List<ContentAttachmentRequest> requests, LocalDateTime now) {
        List<ContentAttachment> values = requests.stream()
            .map(request -> new ContentAttachment(
                attachmentIdSequence.incrementAndGet(),
                contentId,
                request.mediaId(),
                request.fileName(),
                request.fileUrl(),
                request.fileSize(),
                request.fileType(),
                request.sortNo(),
                now,
                now
            ))
            .toList();
        attachments.put(contentId, values);
    }

    private void add(CmsColumn column) {
        columns.put(column.id(), column);
    }

    private CmsColumn column(
        long id,
        SiteType siteType,
        String name,
        String code,
        com.zlwang.school.modules.template.model.ColumnType type,
        String route,
        com.zlwang.school.modules.template.model.PageTemplateKey templateKey,
        com.zlwang.school.modules.template.model.PageTemplateKey detailTemplateKey,
        Map<String, Object> config,
        int sortNo,
        LocalDateTime now
    ) {
        return new CmsColumn(
            id,
            0,
            siteType,
            name,
            code,
            type,
            route,
            null,
            templateKey,
            detailTemplateKey,
            config,
            null,
            sortNo,
            true,
            true,
            name,
            null,
            null,
            null,
            now,
            now
        );
    }

    private Map<String, Object> articleConfig() {
        Map<String, Object> page = new LinkedHashMap<>();
        page.put("listStyle", "IMAGE_TEXT");
        page.put("pageSize", 10);
        page.put("showCover", true);
        page.put("showSummary", true);
        page.put("showPublishAt", true);
        page.put("showViewCount", false);
        page.put("defaultSort", "PUBLISH_AT_DESC");
        page.put("emptyText", "暂无相关内容");
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("showAuthor", true);
        detail.put("showSource", true);
        detail.put("showPublishAt", true);
        detail.put("showViewCount", true);
        detail.put("showSiblingNavigation", true);
        detail.put("showAttachments", true);
        detail.put("showRelated", false);
        detail.put("relatedCount", 4);
        detail.put("shareEnabled", false);
        return Map.of("page", Map.copyOf(page), "detail", Map.copyOf(detail));
    }
}
