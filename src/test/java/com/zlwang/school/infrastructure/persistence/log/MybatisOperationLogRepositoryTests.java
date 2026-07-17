package com.zlwang.school.infrastructure.persistence.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.log.model.LogResultStatus;
import com.zlwang.school.modules.log.model.OperationLog;
import com.zlwang.school.modules.log.model.OperationModule;
import com.zlwang.school.modules.log.model.OperationType;
import com.zlwang.school.modules.log.repository.CreateOperationLog;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MybatisOperationLogRepositoryTests {

    @Mock
    private OperationLogMapper operationLogMapper;

    private MybatisOperationLogRepository repository;

    @BeforeEach
    void setUp() {
        repository = new MybatisOperationLogRepository(operationLogMapper);
    }

    @Test
    void mapsPageFiltersAndEnums() {
        LocalDateTime start = LocalDateTime.of(2026, 7, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 31, 23, 59);
        LocalDateTime now = LocalDateTime.now();
        when(operationLogMapper.countLogs(
            "admin",
            "CONTENT",
            "PUBLISH",
            "SUCCESS",
            start,
            end
        )).thenReturn(1L);
        when(operationLogMapper.findLogs(
            "admin",
            "CONTENT",
            "PUBLISH",
            "SUCCESS",
            start,
            end,
            0,
            10
        )).thenReturn(List.of(new OperationLogRow(
            1L,
            1L,
            "admin",
            "CONTENT",
            "PUBLISH",
            "PUT",
            "/api/admin/contents/1/publish",
            "127.0.0.1",
            null,
            "SUCCESS",
            null,
            12,
            now
        )));

        PageResult<OperationLog> page = repository.findPage(
            "admin",
            OperationModule.CONTENT,
            OperationType.PUBLISH,
            LogResultStatus.SUCCESS,
            start,
            end,
            1,
            10
        );

        assertThat(page.total()).isEqualTo(1);
        assertThat(page.records()).singleElement().satisfies(log -> {
            assertThat(log.moduleName()).isEqualTo(OperationModule.CONTENT);
            assertThat(log.operationType()).isEqualTo(OperationType.PUBLISH);
            assertThat(log.resultStatus()).isEqualTo(LogResultStatus.SUCCESS);
        });
    }

    @Test
    void createsAuditRowWithEnumValues() {
        CreateOperationLog command = new CreateOperationLog(
            1L,
            "admin",
            OperationModule.MEDIA,
            OperationType.UPLOAD,
            "POST",
            "/api/admin/media/upload",
            "127.0.0.1",
            "{\"body\":{\"file\":\"campus.jpg\"}}",
            LogResultStatus.SUCCESS,
            null,
            18
        );

        repository.create(command);

        verify(operationLogMapper).insert(command, "MEDIA", "UPLOAD", "SUCCESS");
    }
}
