package com.zlwang.school.modules.auth.service;

import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.auth.dto.LoginRequest;
import com.zlwang.school.modules.auth.model.LoginClientInfo;
import com.zlwang.school.modules.auth.vo.CurrentUserResponse;
import com.zlwang.school.modules.auth.vo.LoginResponse;
import com.zlwang.school.modules.log.model.LoginStatus;
import com.zlwang.school.modules.log.repository.CreateLoginLog;
import com.zlwang.school.modules.log.repository.LoginLogRepository;
import com.zlwang.school.security.AuthenticatedUser;
import com.zlwang.school.security.JwtTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final LoginLogRepository loginLogRepository;

    public AuthService(
        AuthenticationManager authenticationManager,
        JwtTokenService jwtTokenService,
        LoginLogRepository loginLogRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.loginLogRepository = loginLogRepository;
    }

    public LoginResponse login(LoginRequest request, LoginClientInfo clientInfo) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password())
            );
            AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
            assert user != null;
            LoginResponse response = new LoginResponse(
                "Bearer",
                jwtTokenService.generateToken(user),
                jwtTokenService.expiresInSeconds(),
                CurrentUserResponse.from(user)
            );
            recordLogin(user.id(), user.getUsername(), clientInfo, LoginStatus.SUCCESS, null);
            return response;
        } catch (AuthenticationException ex) {
            recordLogin(
                null,
                request.username(),
                clientInfo,
                LoginStatus.FAIL,
                "用户名或密码错误"
            );
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
    }

    private void recordLogin(
        Long userId,
        String username,
        LoginClientInfo clientInfo,
        LoginStatus status,
        String failureReason
    ) {
        try {
            loginLogRepository.create(new CreateLoginLog(
                userId,
                truncate(username, 64),
                truncate(clientInfo.ipAddress(), 64),
                truncate(clientInfo.userAgent(), 512),
                status,
                failureReason
            ));
        } catch (RuntimeException ex) {
            LOGGER.warn("Failed to persist login audit log for account {}", username, ex);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
