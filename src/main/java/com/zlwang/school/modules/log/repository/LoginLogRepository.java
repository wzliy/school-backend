package com.zlwang.school.modules.log.repository;

import com.zlwang.school.common.api.PageResult;
import com.zlwang.school.modules.log.model.LoginLog;
import com.zlwang.school.modules.log.model.LoginStatus;
import java.time.LocalDateTime;

public interface LoginLogRepository {

    PageResult<LoginLog> findPage(
        String username,
        LoginStatus loginStatus,
        LocalDateTime startTime,
        LocalDateTime endTime,
        long pageNo,
        long pageSize
    );

    void create(CreateLoginLog command);
}
