package com.zlwang.school.infrastructure.storage;

import com.zlwang.school.modules.media.model.StorageType;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(name = "app.file.storage-type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final FileStorageProperties properties;
    private final Path root;

    public LocalStorageService(FileStorageProperties properties) {
        this.properties = properties;
        this.root = properties.getLocalPath().toAbsolutePath().normalize();
    }

    @Override
    public StoredFile store(MultipartFile file, String extension) {
        LocalDate today = LocalDate.now();
        String directory = "%04d/%02d".formatted(today.getYear(), today.getMonthValue());
        String storedName = UUID.randomUUID() + "." + extension;
        String relativePath = directory + "/" + storedName;
        Path target = resolve(relativePath);
        try {
            Files.createDirectories(target.getParent());
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            throw new StorageException("保存本地文件失败", ex);
        }
        return new StoredFile(
            StorageType.LOCAL,
            storedName,
            relativePath,
            properties.normalizedPublicUrlPrefix() + "/" + relativePath
        );
    }

    @Override
    public void delete(String filePath) {
        try {
            Files.deleteIfExists(resolve(filePath));
        } catch (IOException ex) {
            throw new StorageException("删除本地文件失败", ex);
        }
    }

    private Path resolve(String relativePath) {
        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new IllegalArgumentException("文件路径超出存储根目录");
        }
        return resolved;
    }
}
