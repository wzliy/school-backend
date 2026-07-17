package com.zlwang.school.infrastructure.persistence.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.log.model.LoginLog;
import com.zlwang.school.modules.log.model.LoginStatus;
import com.zlwang.school.modules.log.repository.CreateLoginLog;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MybatisLoginLogRepositoryTests {

    @Mock
    private LoginLogMapper loginLogMapper;

    private MybatisLoginLogRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MybatisLoginLogRepository(loginLogMapper);
    }

    @Test
    void mapsPageRowsAndStatus() {
        LocalDateTime now = LocalDateTime.now();
        when(loginLogMapper.countLogs("admin", "SUCCESS", null, null)).thenReturn(1L);
        when(loginLogMapper.findLogs("admin", "SUCCESS", null, null, 0, 10))
            .thenReturn(List.of(new LoginLogRow(
                1L,
                1L,
                "admin",
                "127.0.0.1",
                "JUnit",
                "SUCCESS",
                null,
                now
            )));

        PageResult<LoginLog> page = repository.findPage(
            "admin",
            LoginStatus.SUCCESS,
            null,
            null,
            1,
            10
        );

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records()).singleElement().satisfies(log -> {
            assertThat(log.loginStatus()).isEqualTo(LoginStatus.SUCCESS);
            assertThat(log.userId()).isEqualTo(1L);
        });
    }

    @Test
    void createsLoginRowWithStatus() {
        CreateLoginLog command = new CreateLoginLog(
            null,
            "missing",
            "127.0.0.1",
            "JUnit",
            LoginStatus.FAIL,
            "用户名或密码错误"
        );

        repository.create(command);

        verify(loginLogMapper).insert(command, "FAIL");
    }
}
