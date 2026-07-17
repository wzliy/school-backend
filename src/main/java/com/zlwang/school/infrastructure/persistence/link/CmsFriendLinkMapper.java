package com.zlwang.school.infrastructure.persistence.link;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CmsFriendLinkMapper {

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM cms_friend_link
        WHERE deleted = 0
        <if test='keyword != null and keyword != ""'>
          AND name LIKE CONCAT('%', #{keyword}, '%')
        </if>
        <if test='siteType != null'>
          AND site_type = #{siteType}
        </if>
        <if test='enabled != null'>
          AND enabled = #{enabled}
        </if>
        </script>
        """)
    long countFriendLinks(
        @Param("keyword") String keyword,
        @Param("siteType") String siteType,
        @Param("enabled") Integer enabled
    );

    @Select("""
        <script>
        SELECT id, site_type, name, link_url, logo_url, sort_no, enabled, remark,
               created_at, updated_at
        FROM cms_friend_link
        WHERE deleted = 0
        <if test='keyword != null and keyword != ""'>
          AND name LIKE CONCAT('%', #{keyword}, '%')
        </if>
        <if test='siteType != null'>
          AND site_type = #{siteType}
        </if>
        <if test='enabled != null'>
          AND enabled = #{enabled}
        </if>
        ORDER BY sort_no, id
        LIMIT #{limit} OFFSET #{offset}
        </script>
        """)
    List<CmsFriendLinkRow> findFriendLinks(
        @Param("keyword") String keyword,
        @Param("siteType") String siteType,
        @Param("enabled") Integer enabled,
        @Param("offset") long offset,
        @Param("limit") long limit
    );

    @Select("""
        SELECT id, site_type, name, link_url, logo_url, sort_no, enabled, remark,
               created_at, updated_at
        FROM cms_friend_link
        WHERE id = #{id}
          AND deleted = 0
        """)
    CmsFriendLinkRow findById(@Param("id") long id);

    @Insert("""
        INSERT INTO cms_friend_link (
          site_type, name, link_url, logo_url, sort_no, enabled, remark,
          created_by, updated_by, deleted
        ) VALUES (
          #{row.siteType}, #{row.name}, #{row.linkUrl}, #{row.logoUrl},
          #{row.sortNo}, #{row.enabled}, #{row.remark},
          #{row.operatorId}, #{row.operatorId}, 0
        )
        """)
    int insert(@Param("row") CmsFriendLinkWriteRow row);

    @Select("SELECT LAST_INSERT_ID()")
    long lastInsertId();

    @Update("""
        UPDATE cms_friend_link
        SET site_type = #{row.siteType},
            name = #{row.name},
            link_url = #{row.linkUrl},
            logo_url = #{row.logoUrl},
            sort_no = #{row.sortNo},
            enabled = #{row.enabled},
            remark = #{row.remark},
            updated_by = #{row.operatorId}
        WHERE id = #{id}
          AND deleted = 0
        """)
    int update(@Param("id") long id, @Param("row") CmsFriendLinkWriteRow row);

    @Update("""
        UPDATE cms_friend_link
        SET enabled = #{enabled}, updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int updateStatus(
        @Param("id") long id,
        @Param("enabled") int enabled,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE cms_friend_link
        SET sort_no = #{sortNo}, updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int updateSort(
        @Param("id") long id,
        @Param("sortNo") int sortNo,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE cms_friend_link
        SET enabled = 0, deleted = 1, updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int delete(@Param("id") long id, @Param("operatorId") long operatorId);
}
