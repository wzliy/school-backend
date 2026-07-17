package com.zlwang.school.infrastructure.persistence.log;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.log.model.LoginLog;
import com.zlwang.school.modules.log.model.LoginStatus;
import com.zlwang.school.modules.log.repository.CreateLoginLog;
import com.zlwang.school.modules.log.repository.LoginLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!local")
public class MybatisLoginLogRepository implements LoginLogRepository {

    private final LoginLogMapper loginLogMapper;

    public MybatisLoginLogRepository(LoginLogMapper loginLogMapper) {
        this.loginLogMapper = loginLogMapper;
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
        String status = loginStatus == null ? null : loginStatus.name();
        long total = loginLogMapper.countLogs(username, status, startTime, endTime);
        if (total == 0) {
            return PageResult.empty(pageNo, pageSize);
        }
        List<LoginLog> records = loginLogMapper.findLogs(
            username,
            status,
            startTime,
            endTime,
            (pageNo - 1) * pageSize,
            pageSize
        ).stream().map(this::toLog).toList();
        return PageResult.of(records, total, pageNo, pageSize);
    }

    @Override
    public void create(CreateLoginLog command) {
        loginLogMapper.insert(command, command.loginStatus().name());
    }

    private LoginLog toLog(LoginLogRow row) {
        return new LoginLog(
            row.id(),
            row.userId(),
            row.username(),
            row.loginIp(),
            row.userAgent(),
            LoginStatus.valueOf(row.loginStatus()),
            row.failureReason(),
            row.createdAt()
        );
    }
}
