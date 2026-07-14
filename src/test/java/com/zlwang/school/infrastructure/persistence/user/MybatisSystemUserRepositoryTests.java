package com.zlwang.school.infrastructure.persistence.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.user.model.SystemUser;
import com.zlwang.school.modules.user.repository.CreateSystemUser;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MybatisSystemUserRepositoryTests {

    @Mock
    private SystemUserMapper systemUserMapper;

    @InjectMocks
    private MybatisSystemUserRepository repository;

    @Test
    void pageIncludesAssignedRoleIds() {
        LocalDateTime now = LocalDateTime.now();
        when(systemUserMapper.countUsers("admin", 1)).thenReturn(1L);
        when(systemUserMapper.findUsers("admin", 1, 0L, 10L)).thenReturn(List.of(
            new SystemUserRow(
                1L,
                "admin",
                "管理员",
                null,
                "admin@example.com",
                null,
                1,
                null,
                now,
                now
            )
        ));
        when(systemUserMapper.findRoleIdsByUserId(1L)).thenReturn(List.of(1L));

        PageResult<SystemUser> page = repository.findPage("admin", 1, 1L, 10L);

        assertThat(page.total()).isEqualTo(1L);
        assertThat(page.records()).singleElement()
            .satisfies(user -> assertThat(user.roleIds()).containsExactly(1L));
    }

    @Test
    void createPersistsUserAndRolesInOneRepositoryOperation() {
        CreateSystemUser command = new CreateSystemUser(
            "editor",
            "hash",
            "编辑",
            null,
            null,
            1,
            null,
            List.of(2L, 3L),
            1L
        );
        when(systemUserMapper.findIdByUsername("editor")).thenReturn(9L);

        assertThat(repository.create(command)).isEqualTo(9L);

        verify(systemUserMapper).insertUser("editor", "hash", "编辑", null, null, 1, null, 1L);
        verify(systemUserMapper).insertUserRoles(9L, List.of(2L, 3L), 1L);
    }

    @Test
    void replaceRolesStopsWhenUserDoesNotExist() {
        when(systemUserMapper.findById(99L)).thenReturn(null);

        assertThat(repository.replaceRoles(99L, List.of(1L), 1L)).isFalse();

        verify(systemUserMapper).findById(99L);
        verifyNoMoreInteractions(systemUserMapper);
    }
}
