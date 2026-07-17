package com.zlwang.school.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.zlwang.school.infrastructure.persistence.auth.AuthUserMapper;
import com.zlwang.school.infrastructure.persistence.permission.SystemPermissionMapper;
import com.zlwang.school.infrastructure.persistence.role.SystemRoleMapper;
import com.zlwang.school.infrastructure.persistence.user.SystemUserMapper;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class MybatisMapperConfigurationTests {

    @Test
    void mapperAnnotationsCanBeParsedWithoutDatabaseConnection() {
        Configuration configuration = new Configuration();
        configuration.setMapUnderscoreToCamelCase(true);

        configuration.addMapper(AuthUserMapper.class);
        configuration.addMapper(SystemPermissionMapper.class);
        configuration.addMapper(SystemRoleMapper.class);
        configuration.addMapper(SystemUserMapper.class);

        assertThat(configuration.hasStatement(
            SystemUserMapper.class.getName() + ".findUsers"
        )).isTrue();
        assertThat(configuration.hasStatement(
            AuthUserMapper.class.getName() + ".findPermissionsByUserId"
        )).isTrue();
        assertThat(configuration.hasStatement(
            SystemRoleMapper.class.getName() + ".insertRolePermissions"
        )).isTrue();
        assertThat(configuration.hasStatement(
            SystemPermissionMapper.class.getName() + ".findAll"
        )).isTrue();
    }
}
