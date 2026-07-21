package com.zlwang.school.infrastructure.persistence.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.content.dto.ContentAttachmentRequest;
import com.zlwang.school.modules.content.model.AttachmentFileType;
import com.zlwang.school.modules.content.model.CmsContent;
import com.zlwang.school.modules.content.model.ContentStatus;
import com.zlwang.school.modules.content.repository.CreateCmsContent;
import com.zlwang.school.modules.content.repository.UpdateCmsContent;
import com.zlwang.school.modules.template.model.SiteType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MybatisCmsContentRepositoryTests {

    @Mock
    private CmsContentMapper cmsContentMapper;

    private MybatisCmsContentRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MybatisCmsContentRepository(cmsContentMapper, new ObjectMapper());
    }

    @Test
    void mapsContentExtensionDataAndAttachments() {
        LocalDateTime now = LocalDateTime.now();
        when(cmsContentMapper.findById(11L)).thenReturn(new CmsContentRow(
            11L,
            101L,
            "新闻中心",
            "MAIN_SITE",
            "校园新闻",
            null,
            "摘要",
            "<p>正文</p>",
            null,
            "学校办公室",
            "编辑部",
            now,
            "PUBLISHED",
            1,
            0,
            10,
            20L,
            null,
            null,
            null,
            "{\"gallery\":[\"/uploads/1.jpg\"]}",
            now,
            now
        ));
        when(cmsContentMapper.findAttachments(11L)).thenReturn(List.of(new CmsContentAttachmentRow(
            21L,
            11L,
            31L,
            "招生简章.pdf",
            "/uploads/brochure.pdf",
            1024L,
            "DOCUMENT",
            10,
            now,
            now
        )));

        CmsContent content = repository.findById(11L).orElseThrow();

        assertThat(content.siteType()).isEqualTo(SiteType.MAIN_SITE);
        assertThat(content.status()).isEqualTo(ContentStatus.PUBLISHED);
        assertThat(content.extensionData()).containsKey("gallery");
        assertThat(content.attachments()).singleElement()
            .satisfies(attachment -> assertThat(attachment.fileType()).isEqualTo(AttachmentFileType.DOCUMENT));
    }

    @Test
    void publicQueriesMapPublishedContentWithoutAttachments() {
        LocalDateTime publishedAt = LocalDateTime.of(2026, 7, 20, 10, 0);
        CmsContentRow row = row(publishedAt);
        when(cmsContentMapper.findPublishedByColumn(101L, "MAIN_SITE", publishedAt, 6))
            .thenReturn(List.of(row));
        when(cmsContentMapper.findPublishedGallery("MAIN_SITE", publishedAt, 8))
            .thenReturn(List.of(row));

        assertThat(repository.findPublishedByColumn(
            101L,
            SiteType.MAIN_SITE,
            publishedAt,
            6
        )).singleElement().satisfies(content -> {
            assertThat(content.status()).isEqualTo(ContentStatus.PUBLISHED);
            assertThat(content.attachments()).isEmpty();
        });
        assertThat(repository.findPublishedGallery(
            SiteType.MAIN_SITE,
            publishedAt,
            8
        )).hasSize(1);
    }

    @Test
    void publicPageUsesFilteredCountAndDatabaseOffset() {
        LocalDateTime publishedAt = LocalDateTime.of(2026, 7, 20, 10, 0);
        when(cmsContentMapper.countPublishedPage(101L, "MAIN_SITE", publishedAt))
            .thenReturn(3L);
        when(cmsContentMapper.findPublishedPage(
            101L,
            "MAIN_SITE",
            publishedAt,
            2L,
            2L
        )).thenReturn(List.of(row(publishedAt)));

        PageResult<CmsContent> page = repository.findPublishedPage(
            101L,
            SiteType.MAIN_SITE,
            publishedAt,
            2,
            2
        );

        assertThat(page.total()).isEqualTo(3);
        assertThat(page.pageNo()).isEqualTo(2);
        assertThat(page.records()).singleElement()
            .satisfies(content -> assertThat(content.attachments()).isEmpty());
    }

    @Test
    void createWritesContentThenAttachmentsWithGeneratedId() {
        LocalDateTime publishAt = LocalDateTime.of(2026, 7, 17, 10, 0);
        CreateCmsContent command = new CreateCmsContent(
            101L,
            SiteType.MAIN_SITE,
            "校园新闻",
            null,
            "摘要",
            "<p>正文</p>",
            null,
            "学校办公室",
            "编辑部",
            publishAt,
            ContentStatus.DRAFT,
            false,
            true,
            10,
            null,
            null,
            null,
            Map.of("shortName", "新闻"),
            List.of(attachment("附件.pdf")),
            1L
        );
        when(cmsContentMapper.lastInsertId()).thenReturn(12L);

        assertThat(repository.create(command)).isEqualTo(12L);

        ArgumentCaptor<CmsContentWriteRow> rowCaptor = ArgumentCaptor.forClass(CmsContentWriteRow.class);
        verify(cmsContentMapper).insertContent(rowCaptor.capture());
        assertThat(rowCaptor.getValue().siteType()).isEqualTo("MAIN_SITE");
        assertThat(rowCaptor.getValue().status()).isEqualTo("DRAFT");
        assertThat(rowCaptor.getValue().extensionData()).isEqualTo("{\"shortName\":\"新闻\"}");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CmsContentAttachmentWriteRow>> attachmentsCaptor = ArgumentCaptor.forClass(List.class);
        verify(cmsContentMapper).insertAttachments(eq(12L), attachmentsCaptor.capture(), eq(1L));
        assertThat(attachmentsCaptor.getValue()).singleElement()
            .satisfies(row -> assertThat(row.fileType()).isEqualTo("DOCUMENT"));
    }

    @Test
    void updateReplacesExistingAttachments() {
        UpdateCmsContent command = new UpdateCmsContent(
            13L,
            101L,
            SiteType.MAIN_SITE,
            "已更新新闻",
            null,
            null,
            "<p>正文</p>",
            null,
            null,
            null,
            null,
            true,
            false,
            20,
            null,
            null,
            null,
            Map.of(),
            List.of(attachment("新附件.pdf")),
            2L
        );
        when(cmsContentMapper.updateContent(org.mockito.ArgumentMatchers.eq(13L), org.mockito.ArgumentMatchers.any()))
            .thenReturn(1);

        assertThat(repository.update(command)).isTrue();

        verify(cmsContentMapper).deleteAttachments(13L, 2L);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CmsContentAttachmentWriteRow>> attachmentsCaptor = ArgumentCaptor.forClass(List.class);
        verify(cmsContentMapper).insertAttachments(eq(13L), attachmentsCaptor.capture(), eq(2L));
        assertThat(attachmentsCaptor.getValue()).singleElement()
            .satisfies(row -> assertThat(row.fileName()).isEqualTo("新附件.pdf"));
    }

    private ContentAttachmentRequest attachment(String name) {
        return new ContentAttachmentRequest(
            null,
            name,
            "/uploads/" + name,
            1024L,
            AttachmentFileType.DOCUMENT,
            10
        );
    }

    private CmsContentRow row(LocalDateTime now) {
        return new CmsContentRow(
            11L,
            101L,
            "新闻中心",
            "MAIN_SITE",
            "校园新闻",
            null,
            "摘要",
            "<p>正文</p>",
            "/uploads/news.jpg",
            null,
            null,
            now.minusHours(1),
            "PUBLISHED",
            0,
            1,
            10,
            20L,
            null,
            null,
            null,
            "{}",
            now,
            now
        );
    }
}
