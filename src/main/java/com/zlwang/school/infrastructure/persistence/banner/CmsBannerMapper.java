package com.zlwang.school.infrastructure.persistence.banner;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CmsBannerMapper {

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM cms_banner
        WHERE deleted = 0
        <if test='keyword != null and keyword != ""'>
          AND (title LIKE CONCAT('%', #{keyword}, '%')
            OR subtitle LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test='siteType != null'>
          AND site_type = #{siteType}
        </if>
        <if test='position != null'>
          AND position = #{position}
        </if>
        <if test='enabled != null'>
          AND enabled = #{enabled}
        </if>
        </script>
        """)
    long countBanners(
        @Param("keyword") String keyword,
        @Param("siteType") String siteType,
        @Param("position") String position,
        @Param("enabled") Integer enabled
    );

    @Select("""
        <script>
        SELECT id, site_type, position, title, subtitle, image_url, mobile_image_url,
               link_type, link_ref_id, link_url, link_target, sort_no, enabled,
               start_time, end_time, remark, created_at, updated_at
        FROM cms_banner
        WHERE deleted = 0
        <if test='keyword != null and keyword != ""'>
          AND (title LIKE CONCAT('%', #{keyword}, '%')
            OR subtitle LIKE CONCAT('%', #{keyword}, '%'))
        </if>
        <if test='siteType != null'>
          AND site_type = #{siteType}
        </if>
        <if test='position != null'>
          AND position = #{position}
        </if>
        <if test='enabled != null'>
          AND enabled = #{enabled}
        </if>
        ORDER BY sort_no, id
        LIMIT #{limit} OFFSET #{offset}
        </script>
        """)
    List<CmsBannerRow> findBanners(
        @Param("keyword") String keyword,
        @Param("siteType") String siteType,
        @Param("position") String position,
        @Param("enabled") Integer enabled,
        @Param("offset") long offset,
        @Param("limit") long limit
    );

    @Select("""
        SELECT id, site_type, position, title, subtitle, image_url, mobile_image_url,
               link_type, link_ref_id, link_url, link_target, sort_no, enabled,
               start_time, end_time, remark, created_at, updated_at
        FROM cms_banner
        WHERE id = #{id}
          AND deleted = 0
        """)
    CmsBannerRow findById(@Param("id") long id);

    @Select("""
        SELECT id, site_type, position, title, subtitle, image_url, mobile_image_url,
               link_type, link_ref_id, link_url, link_target, sort_no, enabled,
               start_time, end_time, remark, created_at, updated_at
        FROM cms_banner
        WHERE site_type = #{siteType}
          AND position = #{position}
          AND enabled = 1
          AND deleted = 0
          AND (start_time IS NULL OR start_time &lt;= #{effectiveAt})
          AND (end_time IS NULL OR end_time &gt;= #{effectiveAt})
        ORDER BY sort_no, id
        """)
    List<CmsBannerRow> findActive(
        @Param("siteType") String siteType,
        @Param("position") String position,
        @Param("effectiveAt") LocalDateTime effectiveAt
    );

    @Insert("""
        INSERT INTO cms_banner (
          site_type, position, title, subtitle, image_url, mobile_image_url,
          link_type, link_ref_id, link_url, link_target, sort_no, enabled,
          start_time, end_time, remark, created_by, updated_by, deleted
        ) VALUES (
          #{row.siteType}, #{row.position}, #{row.title}, #{row.subtitle},
          #{row.imageUrl}, #{row.mobileImageUrl}, #{row.linkType}, #{row.linkRefId},
          #{row.linkUrl}, #{row.linkTarget}, #{row.sortNo}, #{row.enabled},
          #{row.startTime}, #{row.endTime}, #{row.remark},
          #{row.operatorId}, #{row.operatorId}, 0
        )
        """)
    int insert(@Param("row") CmsBannerWriteRow row);

    @Select("SELECT LAST_INSERT_ID()")
    long lastInsertId();

    @Update("""
        UPDATE cms_banner
        SET site_type = #{row.siteType},
            position = #{row.position},
            title = #{row.title},
            subtitle = #{row.subtitle},
            image_url = #{row.imageUrl},
            mobile_image_url = #{row.mobileImageUrl},
            link_type = #{row.linkType},
            link_ref_id = #{row.linkRefId},
            link_url = #{row.linkUrl},
            link_target = #{row.linkTarget},
            sort_no = #{row.sortNo},
            enabled = #{row.enabled},
            start_time = #{row.startTime},
            end_time = #{row.endTime},
            remark = #{row.remark},
            updated_by = #{row.operatorId}
        WHERE id = #{id}
          AND deleted = 0
        """)
    int update(@Param("id") long id, @Param("row") CmsBannerWriteRow row);

    @Update("""
        UPDATE cms_banner
        SET enabled = #{enabled}, updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int updateStatus(
        @Param("id") long id,
        @Param("enabled") int enabled,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE cms_banner
        SET sort_no = #{sortNo}, updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int updateSort(
        @Param("id") long id,
        @Param("sortNo") int sortNo,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE cms_banner
        SET enabled = 0, deleted = 1, updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int delete(@Param("id") long id, @Param("operatorId") long operatorId);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM cms_banner
        WHERE link_type = #{linkType}
          AND link_ref_id = #{linkRefId}
          AND deleted = 0
        <if test='enabledOnly'>
          AND enabled = 1
        </if>
        </script>
        """)
    long countReferences(
        @Param("linkType") String linkType,
        @Param("linkRefId") long linkRefId,
        @Param("enabledOnly") boolean enabledOnly
    );
}
