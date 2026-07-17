package com.zlwang.school.infrastructure.persistence.media;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CmsMediaMapper {

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM cms_media
        WHERE deleted = 0
        <if test='keyword != null and keyword != ""'>
          AND (original_name LIKE CONCAT('%', #{keyword}, '%')
            OR stored_name LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test='fileType != null'>
          AND file_type = #{fileType}
        </if>
        <if test='storageType != null'>
          AND storage_type = #{storageType}
        </if>
        <if test='uploaderId != null'>
          AND uploader_id = #{uploaderId}
        </if>
        </script>
        """)
    long countMedia(
        @Param("keyword") String keyword,
        @Param("fileType") String fileType,
        @Param("storageType") String storageType,
        @Param("uploaderId") Long uploaderId
    );

    @Select("""
        <script>
        SELECT id, storage_type, file_type, original_name, stored_name, extension,
               mime_type, file_size, file_path, access_url, uploader_id, remark,
               created_at, updated_at
        FROM cms_media
        WHERE deleted = 0
        <if test='keyword != null and keyword != ""'>
          AND (original_name LIKE CONCAT('%', #{keyword}, '%')
            OR stored_name LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test='fileType != null'>
          AND file_type = #{fileType}
        </if>
        <if test='storageType != null'>
          AND storage_type = #{storageType}
        </if>
        <if test='uploaderId != null'>
          AND uploader_id = #{uploaderId}
        </if>
        ORDER BY created_at DESC, id DESC
        LIMIT #{limit} OFFSET #{offset}
        </script>
        """)
    List<CmsMediaRow> findMedia(
        @Param("keyword") String keyword,
        @Param("fileType") String fileType,
        @Param("storageType") String storageType,
        @Param("uploaderId") Long uploaderId,
        @Param("offset") long offset,
        @Param("limit") long limit
    );

    @Select("""
        SELECT id, storage_type, file_type, original_name, stored_name, extension,
               mime_type, file_size, file_path, access_url, uploader_id, remark,
               created_at, updated_at
        FROM cms_media
        WHERE id = #{id}
          AND deleted = 0
        """)
    CmsMediaRow findById(@Param("id") long id);

    @Insert("""
        INSERT INTO cms_media (
          storage_type, file_type, original_name, stored_name, extension,
          mime_type, file_size, file_path, access_url, uploader_id, remark,
          created_by, updated_by, deleted
        ) VALUES (
          #{row.storageType}, #{row.fileType}, #{row.originalName}, #{row.storedName},
          #{row.extension}, #{row.mimeType}, #{row.fileSize}, #{row.filePath},
          #{row.accessUrl}, #{row.uploaderId}, #{row.remark},
          #{row.uploaderId}, #{row.uploaderId}, 0
        )
        """)
    int insert(@Param("row") CmsMediaWriteRow row);

    @Select("SELECT LAST_INSERT_ID()")
    long lastInsertId();

    @Update("""
        UPDATE cms_media
        SET deleted = 1, updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int delete(@Param("id") long id, @Param("operatorId") long operatorId);

    @Select("""
        SELECT COUNT(*)
        FROM cms_content_attachment
        WHERE media_id = #{id}
          AND deleted = 0
        """)
    long countReferences(@Param("id") long id);
}
