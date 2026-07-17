package com.zlwang.school.modules.log.dto;

import com.zlwang.school.common.pagination.PageQuery;
import com.zlwang.school.modules.log.model.LoginStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class LoginLogPageQuery extends PageQuery {

    @Size(max = 64, message = "账号长度不能超过 64 个字符")
    private String username;

    private LoginStatus loginStatus;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LoginStatus getLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(LoginStatus loginStatus) {
        this.loginStatus = loginStatus;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
