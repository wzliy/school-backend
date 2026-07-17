package com.zlwang.school.modules.media.dto;

import com.zlwang.school.common.pagination.PageQuery;
import com.zlwang.school.modules.media.model.MediaFileType;
import com.zlwang.school.modules.media.model.StorageType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class MediaPageQuery extends PageQuery {

    @Size(max = 255, message = "长度不能超过 255 个字符")
    private String keyword;

    private MediaFileType fileType;

    private StorageType storageType;

    @Positive(message = "上传人 ID 必须大于 0")
    private Long uploaderId;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public MediaFileType getFileType() {
        return fileType;
    }

    public void setFileType(MediaFileType fileType) {
        this.fileType = fileType;
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public void setStorageType(StorageType storageType) {
        this.storageType = storageType;
    }

    public Long getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(Long uploaderId) {
        this.uploaderId = uploaderId;
    }
}
