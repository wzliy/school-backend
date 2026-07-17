package com.zlwang.school.infrastructure.persistence.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zlwang.school.modules.permission.model.SystemPermission;
import com.zlwang.school.modules.permission.repository.CreateSystemPermission;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MybatisSystemPermissionRepositoryTests {

    @Mock
    private SystemPermissionMapper systemPermissionMapper;

    @InjectMocks
    private MybatisSystemPermissionRepository repository;

    @Test
    void findAllMapsPermissionRows() {
        LocalDateTime now = LocalDateTime.now();
        when(systemPermissionMapper.findAll()).thenReturn(List.of(
            new SystemPermissionRow(
                1L, 0L, "系统管理", "system", "MENU", "/system", "Layout", "Setting",
                null, null, 10, 1, 1, null, now, now
            )
        ));

        List<SystemPermission> permissions = repository.findAll();

        assertThat(permissions).singleElement().satisfies(permission -> {
            assertThat(permission.permissionCode()).isEqualTo("system");
            assertThat(permission.visible()).isTrue();
        });
    }

    @Test
    void createReturnsPersistedPermissionId() {
        CreateSystemPermission command = new CreateSystemPermission(
            0L, "测试菜单", "test:menu", "MENU", "/test", "test/index", "Settings",
            null, null, 10, true, 1, null, 1L
        );
        when(systemPermissionMapper.findIdByCode("test:menu")).thenReturn(300L);

        assertThat(repository.create(command)).isEqualTo(300L);

        verify(systemPermissionMapper).insertPermission(
            0L, "测试菜单", "test:menu", "MENU", "/test", "test/index", "Settings",
            null, null, 10, true, 1, null, 1L
        );
    }

    @Test
    void deleteRemovesRoleRelations() {
        when(systemPermissionMapper.deletePermission(300L, 1L)).thenReturn(1);

        assertThat(repository.delete(300L, 1L)).isTrue();

        verify(systemPermissionMapper).deleteRolePermissions(300L);
    }
}
