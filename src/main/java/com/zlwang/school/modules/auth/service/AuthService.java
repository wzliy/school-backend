package com.zlwang.school.modules.auth.service;

import com.zlwang.school.common.exception.BusinessException;
import com.zlwang.school.common.exception.ErrorCode;
import com.zlwang.school.modules.auth.dto.LoginRequest;
import com.zlwang.school.modules.auth.vo.CurrentUserResponse;
import com.zlwang.school.modules.auth.vo.LoginResponse;
import com.zlwang.school.security.AuthenticatedUser;
import com.zlwang.school.security.JwtTokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password())
            );
            AuthenticatedUser user = (AuthenticatedUser) authentication.getPrincipal();
            assert user != null;
            return new LoginResponse(
                "Bearer",
                jwtTokenService.generateToken(user),
                jwtTokenService.expiresInSeconds(),
                CurrentUserResponse.from(user)
            );
        } catch (AuthenticationException ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
    }
}
