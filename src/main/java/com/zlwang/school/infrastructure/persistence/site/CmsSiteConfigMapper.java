package com.zlwang.school.infrastructure.persistence.site;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CmsSiteConfigMapper {

    @Select("""
        <script>
        SELECT id, site_type, config_key, config_value, config_type, description,
               created_at, updated_at
        FROM cms_site_config
        WHERE deleted = 0
        <if test='siteType != null'>
          AND site_type = #{siteType}
        </if>
        ORDER BY CASE site_type
                   WHEN 'GLOBAL' THEN 0
                   WHEN 'MAIN_SITE' THEN 1
                   WHEN 'RECRUIT_SITE' THEN 2
                   ELSE 3
                 END,
                 id
        </script>
        """)
    List<CmsSiteConfigRow> findAll(@Param("siteType") String siteType);

    @Update("""
        UPDATE cms_site_config
        SET config_value = #{configValue}, updated_by = #{operatorId}
        WHERE site_type = #{siteType}
          AND config_key = #{configKey}
          AND deleted = 0
        """)
    int updateValue(
        @Param("siteType") String siteType,
        @Param("configKey") String configKey,
        @Param("configValue") String configValue,
        @Param("operatorId") long operatorId
    );
}
