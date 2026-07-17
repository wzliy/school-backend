package com.zlwang.school.infrastructure.persistence.log;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.infrastructure.persistence.local.LocalLogStore;
import com.zlwang.school.modules.log.model.LoginLog;
import com.zlwang.school.modules.log.model.LoginStatus;
import com.zlwang.school.modules.log.repository.CreateLoginLog;
import com.zlwang.school.modules.log.repository.LoginLogRepository;
import java.time.LocalDateTime;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("local")
public class LocalLoginLogRepository implements LoginLogRepository {

    private final LocalLogStore localLogStore;

    public LocalLoginLogRepository(LocalLogStore localLogStore) {
        this.localLogStore = localLogStore;
    }

    @Override
    public PageResult<LoginLog> findPage(
        String username,
        LoginStatus loginStatus,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long pageNo,
        long pageSize
    ) {
        return localLogStore.findLoginLogs(
            username,
            loginStatus,
            startTime,
            endTime,
            pageNo,
            pageSize
        );
    }

    @Override
    public void create(CreateLoginLog command) {
        localLogStore.createLoginLog(command);
    }
}
