package com.zlwang.school.infrastructure.persistence.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.role.model.SystemRole;
import com.zlwang.school.modules.role.repository.CreateSystemRole;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MybatisSystemRoleRepositoryTests {

    @Mock
    private SystemRoleMapper systemRoleMapper;

    @InjectMocks
    private MybatisSystemRoleRepository repository;

    @Test
    void pageIncludesAssignedPermissionIds() {
        LocalDateTime now = LocalDateTime.now();
        when(systemRoleMapper.countRoles("管理员", "ADMIN", 1)).thenReturn(1L);
        when(systemRoleMapper.findRoles("管理员", "ADMIN", 1, 0L, 10L)).thenReturn(List.of(
            new SystemRoleRow(2L, "网站管理员", "SITE_ADMIN", 1, 2, null, now, now)
        ));
        when(systemRoleMapper.findPermissionIdsByRoleId(2L)).thenReturn(List.of(5L, 6L, 201L));

        PageResult<SystemRole> page = repository.findPage("管理员", "ADMIN", 1, 1L, 10L);

        assertThat(page.total()).isEqualTo(1L);
        assertThat(page.records()).singleElement()
            .satisfies(role -> assertThat(role.permissionIds()).containsExactly(5L, 6L, 201L));
    }

    @Test
    void createReturnsPersistedRoleId() {
        CreateSystemRole command = new CreateSystemRole(
            "测试角色",
            "TEST_ROLE",
            1,
            10,
            null,
            1L
        );
        when(systemRoleMapper.findIdByCode("TEST_ROLE")).thenReturn(9L);

        assertThat(repository.create(command)).isEqualTo(9L);

        verify(systemRoleMapper).insertRole("测试角色", "TEST_ROLE", 1, 10, null, 1L);
    }

    @Test
    void replacePermissionsStopsWhenRoleDoesNotExist() {
        when(systemRoleMapper.findById(99L)).thenReturn(null);

        assertThat(repository.replacePermissions(99L, List.of(1L), 1L)).isFalse();

        verify(systemRoleMapper).findById(99L);
        verifyNoMoreInteractions(systemRoleMapper);
    }
}
