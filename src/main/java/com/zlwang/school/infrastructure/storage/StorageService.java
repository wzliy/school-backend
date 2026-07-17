package com.zlwang.school.infrastructure.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    StoredFile store(MultipartFile file, String extension);

    void delete(String filePath);
}
