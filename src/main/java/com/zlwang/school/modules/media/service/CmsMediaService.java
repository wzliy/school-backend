package com.zlwang.school.modules.media.service;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.infrastructure.storage.FileStorageProperties;
import com.zlwang.school.infrastructure.storage.StorageException;
import com.zlwang.school.infrastructure.storage.StorageService;
import com.zlwang.school.infrastructure.storage.StoredFile;
import com.zlwang.school.modules.media.dto.MediaPageQuery;
import com.zlwang.school.modules.media.model.CmsMedia;
import com.zlwang.school.modules.media.model.MediaFileType;
import com.zlwang.school.modules.media.repository.CmsMediaRepository;
import com.zlwang.school.modules.media.repository.CreateCmsMedia;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CmsMediaService {

    private static final Map<MediaFileType, Set<String>> TYPE_EXTENSIONS = Map.of(
        MediaFileType.IMAGE, Set.of("jpg", "jpeg", "png", "gif", "webp"),
        MediaFileType.DOCUMENT, Set.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv"),
        MediaFileType.VIDEO, Set.of("mp4", "webm", "mov"),
        MediaFileType.OTHER, Set.of("zip")
    );

    private final CmsMediaRepository cmsMediaRepository;
    private final StorageService storageService;
    private final FileStorageProperties storageProperties;

    public CmsMediaService(
        CmsMediaRepository cmsMediaRepository,
        StorageService storageService,
        FileStorageProperties storageProperties
    ) {
        this.cmsMediaRepository = cmsMediaRepository;
        this.storageService = storageService;
        this.storageProperties = storageProperties;
    }

    public PageResult<CmsMedia> findPage(MediaPageQuery query) {
        return cmsMediaRepository.findPage(
            normalize(query.getKeyword()),
            query.getFileType(),
            query.getStorageType(),
            query.getUploaderId(),
            query.getPageNo(),
            query.getPageSize()
        );
    }

    public CmsMedia findById(long id) {
        return requiredMedia(id);
    }

    public long upload(MultipartFile file, String remark, long uploaderId) {
        ValidatedFile validated = validate(file);
        StoredFile stored;
        try {
            stored = storageService.store(file, validated.extension());
        } catch (StorageException ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件存储失败");
        }
        try {
            return cmsMediaRepository.create(new CreateCmsMedia(
                stored.storageType(),
                validated.fileType(),
                validated.originalName(),
                stored.storedName(),
                validated.extension(),
                validated.mimeType(),
                file.getSize(),
                stored.filePath(),
                stored.accessUrl(),
                uploaderId,
                normalize(remark)
            ));
        } catch (RuntimeException ex) {
            try {
                storageService.delete(stored.filePath());
            } catch (RuntimeException cleanupException) {
                ex.addSuppressed(cleanupException);
            }
            throw ex;
        }
    }

    public void delete(long id, long operatorId) {
        CmsMedia media = requiredMedia(id);
        if (cmsMediaRepository.countReferences(id) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "媒体文件已被内容附件引用，不能删除");
        }
        try {
            storageService.delete(media.filePath());
        } catch (StorageException ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除存储文件失败");
        }
        if (!cmsMediaRepository.delete(id, operatorId)) {
            throw notFound(id);
        }
    }

    private ValidatedFile validate(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() == 0) {
            throw badRequest("上传文件不能为空");
        }
        long maxBytes = storageProperties.getMaxSize().toBytes();
        if (maxBytes <= 0 || file.getSize() > maxBytes) {
            throw badRequest("上传文件大小不能超过 " + storageProperties.getMaxSize());
        }
        String originalName = sanitizeOriginalName(file.getOriginalFilename());
        String extension = extension(originalName);
        if (!storageProperties.normalizedAllowedExtensions().contains(extension)) {
            throw badRequest("不允许上传该文件后缀：" + extension);
        }
        MediaFileType fileType = TYPE_EXTENSIONS.entrySet().stream()
            .filter(entry -> entry.getValue().contains(extension))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(MediaFileType.OTHER);
        String mimeType = StringUtils.hasText(file.getContentType())
            ? file.getContentType().trim().toLowerCase(Locale.ROOT)
            : "application/octet-stream";
        validateMimeType(fileType, mimeType);
        return new ValidatedFile(originalName, extension, mimeType, fileType);
    }

    private String sanitizeOriginalName(String value) {
        if (!StringUtils.hasText(value)) {
            throw badRequest("原始文件名不能为空");
        }
        String normalized = value.trim().replace('\\', '/');
        String originalName = normalized.substring(normalized.lastIndexOf('/') + 1);
        if (!StringUtils.hasText(originalName) || originalName.length() > 255) {
            throw badRequest("原始文件名长度必须在 1-255 个字符之间");
        }
        return originalName;
    }

    private String extension(String originalName) {
        int separator = originalName.lastIndexOf('.');
        if (separator <= 0 || separator == originalName.length() - 1) {
            throw badRequest("上传文件必须包含有效后缀");
        }
        return originalName.substring(separator + 1).toLowerCase(Locale.ROOT);
    }

    private void validateMimeType(MediaFileType fileType, String mimeType) {
        boolean valid = switch (fileType) {
            case IMAGE -> mimeType.startsWith("image/");
            case VIDEO -> mimeType.startsWith("video/");
            case DOCUMENT -> mimeType.startsWith("application/") || mimeType.startsWith("text/");
            case OTHER -> Set.of(
                "application/zip",
                "application/x-zip-compressed",
                "application/octet-stream"
            ).contains(mimeType);
        };
        if (!valid) {
            throw badRequest("文件 MIME 类型与后缀不匹配");
        }
    }

    private CmsMedia requiredMedia(long id) {
        return cmsMediaRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private BusinessException badRequest(String message) {
        return new BusinessException(ErrorCode.PARAM_VALIDATION_FAILED, message);
    }

    private BusinessException notFound(long id) {
        return new BusinessException(ErrorCode.NOT_FOUND, "媒体文件不存在：" + id);
    }

    private record ValidatedFile(
        String originalName,
        String extension,
        String mimeType,
        MediaFileType fileType
    ) {
    }
}
