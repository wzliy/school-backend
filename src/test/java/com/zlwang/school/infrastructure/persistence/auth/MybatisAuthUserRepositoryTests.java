package com.zlwang.school.infrastructure.persistence.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zlwang.school.modules.auth.model.AuthUserAccount;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MybatisAuthUserRepositoryTests {

    @Mock
    private AuthUserMapper authUserMapper;

    @InjectMocks
    private MybatisAuthUserRepository repository;

    @Test
    void combinesUserRolesAndPermissions() {
        when(authUserMapper.findByUsername("admin"))
            .thenReturn(new AuthUserRow(1L, "admin", "hash", "管理员", null, 1));
        when(authUserMapper.findRoleCodesByUserId(1L)).thenReturn(List.of("SUPER_ADMIN"));
        when(authUserMapper.findAllPermissions()).thenReturn(List.of(
            new AuthPermissionRow(
                1L,
                0L,
                "系统管理",
                "system",
                "MENU",
                "/system",
                "Layout",
                "Setting",
                10,
                1
            )
        ));

        AuthUserAccount account = repository.findByUsername("admin").orElseThrow();

        assertThat(account.username()).isEqualTo("admin");
        assertThat(account.enabled()).isTrue();
        assertThat(account.roleCodes()).containsExactly("SUPER_ADMIN");
        assertThat(account.permissions()).singleElement()
            .satisfies(permission -> {
                assertThat(permission.code()).isEqualTo("system");
                assertThat(permission.visible()).isTrue();
            });
    }

    @Test
    void missingUserDoesNotQueryRelations() {
        when(authUserMapper.findByUsername("missing")).thenReturn(null);

        assertThat(repository.findByUsername("missing")).isEmpty();

        verify(authUserMapper).findByUsername("missing");
    }
}
