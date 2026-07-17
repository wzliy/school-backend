package com.zlwang.school.infrastructure.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class LocalStorageServiceTests {

    @TempDir
    private Path storageRoot;

    @Test
    void storesFilesBelowConfiguredRootAndDeletesThem() throws Exception {
        FileStorageProperties properties = properties();
        LocalStorageService storageService = new LocalStorageService(properties);
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "logo.png",
            "image/png",
            new byte[] {1, 2, 3, 4}
        );

        StoredFile stored = storageService.store(file, "png");
        Path storedPath = storageRoot.resolve(stored.filePath());

        assertThat(stored.filePath()).matches("\\d{4}/\\d{2}/[0-9a-f-]+\\.png");
        assertThat(stored.accessUrl()).isEqualTo("/uploads/" + stored.filePath());
        assertThat(Files.readAllBytes(storedPath)).containsExactly(1, 2, 3, 4);

        storageService.delete(stored.filePath());
        assertThat(storedPath).doesNotExist();
    }

    @Test
    void refusesPathsOutsideStorageRoot() {
        LocalStorageService storageService = new LocalStorageService(properties());

        assertThatThrownBy(() -> storageService.delete("../outside.txt"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("文件路径超出存储根目录");
    }

    private FileStorageProperties properties() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setLocalPath(storageRoot);
        properties.setPublicUrlPrefix("/uploads/");
        return properties;
    }
}
