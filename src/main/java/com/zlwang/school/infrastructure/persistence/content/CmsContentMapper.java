package com.zlwang.school.infrastructure.persistence.content;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CmsContentMapper {

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM cms_content cc
        WHERE cc.deleted = 0
        <if test='keyword != null and keyword != ""'>
          AND (cc.title LIKE CONCAT('%', #{keyword}, '%')
            OR cc.subtitle LIKE CONCAT('%', #{keyword}, '%')
            OR cc.summary LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test='columnId != null'>
          AND cc.column_id = #{columnId}
        </if>
        <if test='siteType != null'>
          AND cc.site_type = #{siteType}
        </if>
        <if test='status != null'>
          AND cc.status = #{status}
        </if>
        </script>
        """)
    long countContents(
        @Param("keyword") String keyword,
        @Param("columnId") Long columnId,
        @Param("siteType") String siteType,
        @Param("status") String status
    );

    @Select("""
        <script>
        SELECT cc.id, cc.column_id, c.column_name, cc.site_type, cc.title, cc.subtitle,
               cc.summary, cc.content_html, cc.cover_url, cc.source, cc.author,
               cc.publish_at, cc.status, cc.top_flag, cc.recommend_flag, cc.sort_no,
               cc.view_count, cc.seo_title, cc.seo_keywords, cc.seo_description,
               CAST(cc.extension_data AS CHAR) AS extension_data,
               cc.created_at, cc.updated_at
        FROM cms_content cc
        INNER JOIN cms_column c ON c.id = cc.column_id AND c.deleted = 0
        WHERE cc.deleted = 0
        <if test='keyword != null and keyword != ""'>
          AND (cc.title LIKE CONCAT('%', #{keyword}, '%')
            OR cc.subtitle LIKE CONCAT('%', #{keyword}, '%')
            OR cc.summary LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test='columnId != null'>
          AND cc.column_id = #{columnId}
        </if>
        <if test='siteType != null'>
          AND cc.site_type = #{siteType}
        </if>
        <if test='status != null'>
          AND cc.status = #{status}
        </if>
        ORDER BY cc.top_flag DESC, cc.sort_no, cc.publish_at DESC, cc.id DESC
        LIMIT #{limit} OFFSET #{offset}
        </script>
        """)
    List<CmsContentRow> findContents(
        @Param("keyword") String keyword,
        @Param("columnId") Long columnId,
        @Param("siteType") String siteType,
        @Param("status") String status,
        @Param("offset") long offset,
        @Param("limit") long limit
    );

    @Select("""
        SELECT cc.id, cc.column_id, c.column_name, cc.site_type, cc.title, cc.subtitle,
               cc.summary, cc.content_html, cc.cover_url, cc.source, cc.author,
               cc.publish_at, cc.status, cc.top_flag, cc.recommend_flag, cc.sort_no,
               cc.view_count, cc.seo_title, cc.seo_keywords, cc.seo_description,
               CAST(cc.extension_data AS CHAR) AS extension_data,
               cc.created_at, cc.updated_at
        FROM cms_content cc
        INNER JOIN cms_column c ON c.id = cc.column_id AND c.deleted = 0
        WHERE cc.id = #{id}
          AND cc.deleted = 0
        """)
    CmsContentRow findById(@Param("id") long id);

    @Select("""
        SELECT id, content_id, media_id, file_name, file_url, file_size, file_type,
               sort_no, created_at, updated_at
        FROM cms_content_attachment
        WHERE content_id = #{contentId}
          AND deleted = 0
        ORDER BY sort_no, id
        """)
    List<CmsContentAttachmentRow> findAttachments(@Param("contentId") long contentId);

    @Insert("""
        INSERT INTO cms_content (
          column_id, site_type, title, subtitle, summary, content_html, cover_url,
          source, author, publish_at, status, top_flag, recommend_flag, sort_no,
          seo_title, seo_keywords, seo_description, extension_data,
          created_by, updated_by, deleted
        ) VALUES (
          #{row.columnId}, #{row.siteType}, #{row.title}, #{row.subtitle}, #{row.summary},
          #{row.contentHtml}, #{row.coverUrl}, #{row.source}, #{row.author}, #{row.publishAt},
          #{row.status}, #{row.topFlag}, #{row.recommendFlag}, #{row.sortNo},
          #{row.seoTitle}, #{row.seoKeywords}, #{row.seoDescription}, CAST(#{row.extensionData} AS JSON),
          #{row.operatorId}, #{row.operatorId}, 0
        )
        """)
    int insertContent(@Param("row") CmsContentWriteRow row);

    @Select("SELECT LAST_INSERT_ID()")
    long lastInsertId();

    @Update("""
        UPDATE cms_content
        SET column_id = #{row.columnId},
            site_type = #{row.siteType},
            title = #{row.title},
            subtitle = #{row.subtitle},
            summary = #{row.summary},
            content_html = #{row.contentHtml},
            cover_url = #{row.coverUrl},
            source = #{row.source},
            author = #{row.author},
            publish_at = #{row.publishAt},
            top_flag = #{row.topFlag},
            recommend_flag = #{row.recommendFlag},
            sort_no = #{row.sortNo},
            seo_title = #{row.seoTitle},
            seo_keywords = #{row.seoKeywords},
            seo_description = #{row.seoDescription},
            extension_data = CAST(#{row.extensionData} AS JSON),
            updated_by = #{row.operatorId}
        WHERE id = #{id}
          AND deleted = 0
        """)
    int updateContent(@Param("id") long id, @Param("row") CmsContentWriteRow row);

    @Update("""
        UPDATE cms_content_attachment
        SET deleted = 1, updated_by = #{operatorId}
        WHERE content_id = #{contentId} AND deleted = 0
        """)
    int deleteAttachments(@Param("contentId") long contentId, @Param("operatorId") long operatorId);

    @Insert("""
        <script>
        INSERT INTO cms_content_attachment (
          content_id, media_id, file_name, file_url, file_size, file_type, sort_no,
          created_by, updated_by, deleted
        ) VALUES
        <foreach collection='rows' item='row' separator=','>
          (#{contentId}, #{row.mediaId}, #{row.fileName}, #{row.fileUrl}, #{row.fileSize},
           #{row.fileType}, #{row.sortNo}, #{operatorId}, #{operatorId}, 0)
        </foreach>
        </script>
        """)
    int insertAttachments(
        @Param("contentId") long contentId,
        @Param("rows") List<CmsContentAttachmentWriteRow> rows,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE cms_content
        SET publish_at = #{publishAt}, status = 'PUBLISHED', updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int publish(
        @Param("id") long id,
        @Param("publishAt") LocalDateTime publishAt,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE cms_content
        SET status = 'OFFLINE', updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int offline(@Param("id") long id, @Param("operatorId") long operatorId);

    @Update("""
        UPDATE cms_content
        SET top_flag = #{topFlag}, updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int updateTop(
        @Param("id") long id,
        @Param("topFlag") int topFlag,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE cms_content
        SET recommend_flag = #{recommendFlag}, updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int updateRecommend(
        @Param("id") long id,
        @Param("recommendFlag") int recommendFlag,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE cms_content
        SET status = 'OFFLINE', deleted = 1, updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int deleteContent(@Param("id") long id, @Param("operatorId") long operatorId);
}
