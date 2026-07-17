package com.zlwang.school.infrastructure.persistence.page;

import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PageSectionMapper {

    @Select("""
        SELECT id, site_type, page_code, section_code, section_name, section_type,
               data_source_column_id, display_count, display_style,
               CAST(config_json AS CHAR) AS config_json,
               sort_no, enabled, created_at, updated_at
        FROM cms_page_section
        WHERE site_type = #{siteType}
          AND page_code = #{pageCode}
          AND deleted = 0
        ORDER BY sort_no, id
        """)
    List<PageSectionRow> findAll(
        @Param("siteType") String siteType,
        @Param("pageCode") String pageCode
    );

    @Select("""
        SELECT id
        FROM cms_page_section
        WHERE site_type = #{siteType}
          AND page_code = #{pageCode}
          AND section_code = #{sectionCode}
        ORDER BY deleted, id
        LIMIT 1
        """)
    Long findAnyIdByCode(
        @Param("siteType") String siteType,
        @Param("pageCode") String pageCode,
        @Param("sectionCode") String sectionCode
    );

    @Insert("""
        INSERT INTO cms_page_section (
          site_type, page_code, section_code, section_name, section_type,
          data_source_column_id, display_count, display_style, config_json,
          sort_no, enabled, created_by, updated_by, deleted
        ) VALUES (
          #{row.siteType}, #{row.pageCode}, #{row.sectionCode}, #{row.sectionName}, #{row.sectionType},
          #{row.dataSourceColumnId}, #{row.displayCount}, #{row.displayStyle}, CAST(#{row.configJson} AS JSON),
          #{row.sortNo}, #{row.enabled}, #{row.operatorId}, #{row.operatorId}, 0
        )
        """)
    int insert(@Param("row") PageSectionWriteRow row);

    @Update("""
        UPDATE cms_page_section
        SET site_type = #{row.siteType},
            page_code = #{row.pageCode},
            section_code = #{row.sectionCode},
            section_name = #{row.sectionName},
            section_type = #{row.sectionType},
            data_source_column_id = #{row.dataSourceColumnId},
            display_count = #{row.displayCount},
            display_style = #{row.displayStyle},
            config_json = CAST(#{row.configJson} AS JSON),
            sort_no = #{row.sortNo},
            enabled = #{row.enabled},
            updated_by = #{row.operatorId},
            deleted = 0
        WHERE id = #{id}
        """)
    int update(@Param("id") long id, @Param("row") PageSectionWriteRow row);
}
