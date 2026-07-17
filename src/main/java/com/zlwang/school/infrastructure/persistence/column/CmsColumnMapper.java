package com.zlwang.school.infrastructure.persistence.column;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CmsColumnMapper {

    @Select("""
        <script>
        SELECT id, parent_id, site_type, column_name, column_code, column_type,
               route_path, external_url, template_key, detail_template_key,
               CAST(template_config AS CHAR) AS template_config, cover_url, sort_no,
               nav_visible, enabled, seo_title, seo_keywords, seo_description,
               remark, created_at, updated_at
        FROM cms_column
        WHERE deleted = 0
        <if test='siteType != null'>
          AND site_type = #{siteType}
        </if>
        ORDER BY sort_no, id
        </script>
        """)
    List<CmsColumnRow> findAll(@Param("siteType") String siteType);

    @Select("""
        SELECT id, parent_id, site_type, column_name, column_code, column_type,
               route_path, external_url, template_key, detail_template_key,
               CAST(template_config AS CHAR) AS template_config, cover_url, sort_no,
               nav_visible, enabled, seo_title, seo_keywords, seo_description,
               remark, created_at, updated_at
        FROM cms_column
        WHERE id = #{id}
          AND deleted = 0
        """)
    CmsColumnRow findById(@Param("id") long id);

    @Select("""
        <script>
        SELECT COUNT(*)
        FROM cms_column
        WHERE site_type = #{siteType}
          AND column_code = #{columnCode}
          AND deleted = 0
        <if test='excludeId != null'>
          AND id != #{excludeId}
        </if>
        </script>
        """)
    long countByCode(
        @Param("siteType") String siteType,
        @Param("columnCode") String columnCode,
        @Param("excludeId") Long excludeId
    );

    @Select("SELECT COUNT(*) FROM cms_column WHERE parent_id = #{id} AND deleted = 0")
    long countChildren(@Param("id") long id);

    @Select("SELECT COUNT(*) FROM cms_content WHERE column_id = #{id} AND deleted = 0")
    long countContents(@Param("id") long id);

    @Insert("""
        INSERT INTO cms_column (
          parent_id, site_type, column_name, column_code, column_type,
          route_path, external_url, template_key, detail_template_key, template_config,
          cover_url, sort_no, nav_visible, enabled,
          seo_title, seo_keywords, seo_description, remark,
          created_by, updated_by, deleted
        ) VALUES (
          #{parentId}, #{siteType}, #{columnName}, #{columnCode}, #{columnType},
          #{routePath}, #{externalUrl}, #{templateKey}, #{detailTemplateKey}, CAST(#{templateConfig} AS JSON),
          #{coverUrl}, #{sortNo}, #{navVisible}, #{enabled},
          #{seoTitle}, #{seoKeywords}, #{seoDescription}, #{remark},
          #{operatorId}, #{operatorId}, 0
        )
        """)
    int insert(
        @Param("parentId") long parentId,
        @Param("siteType") String siteType,
        @Param("columnName") String columnName,
        @Param("columnCode") String columnCode,
        @Param("columnType") String columnType,
        @Param("routePath") String routePath,
        @Param("externalUrl") String externalUrl,
        @Param("templateKey") String templateKey,
        @Param("detailTemplateKey") String detailTemplateKey,
        @Param("templateConfig") String templateConfig,
        @Param("coverUrl") String coverUrl,
        @Param("sortNo") int sortNo,
        @Param("navVisible") int navVisible,
        @Param("enabled") int enabled,
        @Param("seoTitle") String seoTitle,
        @Param("seoKeywords") String seoKeywords,
        @Param("seoDescription") String seoDescription,
        @Param("remark") String remark,
        @Param("operatorId") long operatorId
    );

    @Select("""
        SELECT id FROM cms_column
        WHERE site_type = #{siteType} AND column_code = #{columnCode} AND deleted = 0
        """)
    Long findIdByCode(@Param("siteType") String siteType, @Param("columnCode") String columnCode);

    @Update("""
        UPDATE cms_column
        SET parent_id = #{parentId},
            column_name = #{columnName},
            column_code = #{columnCode},
            column_type = #{columnType},
            route_path = #{routePath},
            external_url = #{externalUrl},
            template_key = #{templateKey},
            detail_template_key = #{detailTemplateKey},
            template_config = CAST(#{templateConfig} AS JSON),
            cover_url = #{coverUrl},
            sort_no = #{sortNo},
            nav_visible = #{navVisible},
            enabled = #{enabled},
            seo_title = #{seoTitle},
            seo_keywords = #{seoKeywords},
            seo_description = #{seoDescription},
            remark = #{remark},
            updated_by = #{operatorId}
        WHERE id = #{id}
          AND deleted = 0
        """)
    int update(
        @Param("id") long id,
        @Param("parentId") long parentId,
        @Param("columnName") String columnName,
        @Param("columnCode") String columnCode,
        @Param("columnType") String columnType,
        @Param("routePath") String routePath,
        @Param("externalUrl") String externalUrl,
        @Param("templateKey") String templateKey,
        @Param("detailTemplateKey") String detailTemplateKey,
        @Param("templateConfig") String templateConfig,
        @Param("coverUrl") String coverUrl,
        @Param("sortNo") int sortNo,
        @Param("navVisible") int navVisible,
        @Param("enabled") int enabled,
        @Param("seoTitle") String seoTitle,
        @Param("seoKeywords") String seoKeywords,
        @Param("seoDescription") String seoDescription,
        @Param("remark") String remark,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE cms_column
        SET enabled = #{enabled}, updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int updateStatus(
        @Param("id") long id,
        @Param("enabled") int enabled,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE cms_column
        SET parent_id = #{parentId}, sort_no = #{sortNo}, updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int updateSort(
        @Param("id") long id,
        @Param("parentId") long parentId,
        @Param("sortNo") int sortNo,
        @Param("operatorId") long operatorId
    );

    @Update("""
        UPDATE cms_column
        SET enabled = 0, deleted = 1, updated_by = #{operatorId}
        WHERE id = #{id} AND deleted = 0
        """)
    int delete(@Param("id") long id, @Param("operatorId") long operatorId);
}
