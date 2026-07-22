package com.zlwang.school.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.zlwang.school.infrastructure.persistence.auth.AuthUserMapper;
import com.zlwang.school.infrastructure.persistence.banner.CmsBannerMapper;
import com.zlwang.school.infrastructure.persistence.column.CmsColumnMapper;
import com.zlwang.school.infrastructure.persistence.content.CmsContentMapper;
import com.zlwang.school.infrastructure.persistence.link.CmsFriendLinkMapper;
import com.zlwang.school.infrastructure.persistence.log.LoginLogMapper;
import com.zlwang.school.infrastructure.persistence.log.OperationLogMapper;
import com.zlwang.school.infrastructure.persistence.media.CmsMediaMapper;
import com.zlwang.school.infrastructure.persistence.page.PageSectionMapper;
import com.zlwang.school.infrastructure.persistence.permission.SystemPermissionMapper;
import com.zlwang.school.infrastructure.persistence.role.SystemRoleMapper;
import com.zlwang.school.infrastructure.persistence.site.CmsSiteConfigMapper;
import com.zlwang.school.infrastructure.persistence.user.SystemUserMapper;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class MybatisMapperConfigurationTests {

    @Test
    void mapperAnnotationsCanBeParsedWithoutDatabaseConnection() {
        Configuration configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);

        configuration.addMapper(AuthUserMapper.class);
        configuration.addMapper(CmsBannerMapper.class);
        configuration.addMapper(CmsColumnMapper.class);
        configuration.addMapper(CmsContentMapper.class);
        configuration.addMapper(CmsFriendLinkMapper.class);
        configuration.addMapper(LoginLogMapper.class);
        configuration.addMapper(CmsMediaMapper.class);
        configuration.addMapper(OperationLogMapper.class);
        configuration.addMapper(PageSectionMapper.class);
        configuration.addMapper(SystemPermissionMapper.class);
        configuration.addMapper(SystemRoleMapper.class);
        configuration.addMapper(CmsSiteConfigMapper.class);
        configuration.addMapper(SystemUserMapper.class);

        assertThat(configuration.hasStatement(
            SystemUserMapper.class.getName() + ".findUsers"
        )).isTrue();
        assertThat(configuration.hasStatement(
            AuthUserMapper.class.getName() + ".findPermissionsByUserId"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsBannerMapper.class.getName() + ".findBanners"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsBannerMapper.class.getName() + ".findActive"
        )).isTrue();
        assertThat(configuration.hasStatement(
            SystemRoleMapper.class.getName() + ".insertRolePermissions"
        )).isTrue();
        assertThat(configuration.hasStatement(
            SystemPermissionMapper.class.getName() + ".findAll"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsColumnMapper.class.getName() + ".findAll"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsContentMapper.class.getName() + ".findContents"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsContentMapper.class.getName() + ".findPublishedByColumn"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsContentMapper.class.getName() + ".countPublishedPage"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsContentMapper.class.getName() + ".findPublishedPage"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsContentMapper.class.getName() + ".countPublishedSearch"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsContentMapper.class.getName() + ".findPublishedSearch"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsContentMapper.class.getName() + ".incrementPublishedViewCount"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsContentMapper.class.getName() + ".findPublishedGallery"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsMediaMapper.class.getName() + ".findMedia"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsFriendLinkMapper.class.getName() + ".findFriendLinks"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsFriendLinkMapper.class.getName() + ".findEnabledForSite"
        )).isTrue();
        assertThat(configuration.hasStatement(
            OperationLogMapper.class.getName() + ".findLogs"
        )).isTrue();
        assertThat(configuration.hasStatement(
            LoginLogMapper.class.getName() + ".findLogs"
        )).isTrue();
        assertThat(configuration.hasStatement(
            CmsSiteConfigMapper.class.getName() + ".findAll"
        )).isTrue();
        assertThat(configuration.hasStatement(
            PageSectionMapper.class.getName() + ".findAll"
        )).isTrue();
    }
}
