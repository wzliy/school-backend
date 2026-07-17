package com.zlwang.school.infrastructure.persistence.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.media.model.CmsMedia;
import com.zlwang.school.modules.media.model.MediaFileType;
import com.zlwang.school.modules.media.model.StorageType;
import com.zlwang.school.modules.media.repository.CreateCmsMedia;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MybatisCmsMediaRepositoryTests {

    @Mock
    private CmsMediaMapper cmsMediaMapper;

    private MybatisCmsMediaRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MybatisCmsMediaRepository(cmsMediaMapper);
    }

    @Test
    void mapsPageRowsAndEnums() {
        LocalDateTime now = LocalDateTime.now();
        when(cmsMediaMapper.countMedia("校徽", "IMAGE", "LOCAL", 1L)).thenReturn(1L);
        when(cmsMediaMapper.findMedia("校徽", "IMAGE", "LOCAL", 1L, 0, 10))
            .thenReturn(List.of(row(now)));

        PageResult<CmsMedia> page = repository.findPage(
            "校徽",
            MediaFileType.IMAGE,
            StorageType.LOCAL,
            1L,
            1,
            10
        );

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records()).singleElement().satisfies(media -> {
            assertThat(media.storageType()).isEqualTo(StorageType.LOCAL);
            assertThat(media.fileType()).isEqualTo(MediaFileType.IMAGE);
            assertThat(media.originalName()).isEqualTo("校徽.png");
        });
    }

    @Test
    void createWritesMetadataAndReturnsGeneratedId() {
        CreateCmsMedia command = new CreateCmsMedia(
            StorageType.LOCAL,
            MediaFileType.DOCUMENT,
            "招生简章.pdf",
            "generated.pdf",
            "pdf",
            "application/pdf",
            1024L,
            "2026/07/generated.pdf",
            "/uploads/2026/07/generated.pdf",
            2L,
            "招生资料"
        );
        when(cmsMediaMapper.lastInsertId()).thenReturn(9L);

        assertThat(repository.create(command)).isEqualTo(9L);

        ArgumentCaptor<CmsMediaWriteRow> captor = ArgumentCaptor.forClass(CmsMediaWriteRow.class);
        verify(cmsMediaMapper).insert(captor.capture());
        assertThat(captor.getValue().storageType()).isEqualTo("LOCAL");
        assertThat(captor.getValue().fileType()).isEqualTo("DOCUMENT");
        assertThat(captor.getValue().uploaderId()).isEqualTo(2L);
        assertThat(captor.getValue().accessUrl()).isEqualTo("/uploads/2026/07/generated.pdf");
    }

    @Test
    void delegatesReferenceCountAndLogicalDelete() {
        when(cmsMediaMapper.countReferences(7L)).thenReturn(2L);
        when(cmsMediaMapper.delete(7L, 3L)).thenReturn(1);

        assertThat(repository.countReferences(7L)).isEqualTo(2);
        assertThat(repository.delete(7L, 3L)).isTrue();
    }

    private CmsMediaRow row(LocalDateTime now) {
        return new CmsMediaRow(
            1L,
            "LOCAL",
            "IMAGE",
            "校徽.png",
            "generated.png",
            "png",
            "image/png",
            128L,
            "2026/07/generated.png",
            "/uploads/2026/07/generated.png",
            1L,
            null,
            now,
            now
        );
    }
}
