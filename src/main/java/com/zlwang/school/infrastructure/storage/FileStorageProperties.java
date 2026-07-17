package com.zlwang.school.infrastructure.storage;

import com.zlwang.school.modules.media.model.StorageType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {

    @NotNull
    private StorageType storageType = StorageType.LOCAL;

    @NotNull
    private Path localPath = Path.of("data/uploads");

    @NotBlank
    private String publicUrlPrefix = "/uploads";

    @NotNull
    private DataSize maxSize = DataSize.ofMegabytes(20);

    @NotNull
    private List<String> allowedExtensions = List.of(
        "jpg", "jpeg", "png", "gif", "webp",
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv",
        "mp4", "webm", "mov", "zip"
    );

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public Path getLocalPath() {
        return localPath;
    }

    public void setLocalPath(Path localPath) {
        this.localPath = localPath;
    }

    public String getPublicUrlPrefix() {
        return publicUrlPrefix;
    }

    public void setPublicUrlPrefix(String publicUrlPrefix) {
        this.publicUrlPrefix = publicUrlPrefix;
    }

    public DataSize getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(DataSize maxSize) {
        this.maxSize = maxSize;
    }

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public String normalizedPublicUrlPrefix() {
        if (publicUrlPrefix == null) {
            return "";
        }
        String value = publicUrlPrefix.trim();
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        while (value.length() > 1 && value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    public List<String> normalizedAllowedExtensions() {
        if (allowedExtensions == null) {
            return List.of();
        }
        return allowedExtensions.stream()
            .filter(value -> value != null && !value.isBlank())
            .map(value -> value.toLowerCase(Locale.ROOT).replaceFirst("^\\.", ""))
            .distinct()
            .toList();
    }

    @AssertTrue(message = "文件访问前缀必须是非根路径且不能包含路径跳转")
    public boolean isPublicUrlPrefixValid() {
        String value = normalizedPublicUrlPrefix();
        return !"/".equals(value)
            && !value.contains("..")
            && value.matches("^/[A-Za-z0-9/_-]+$");
    }
}
