package com.zlwang.school.modules.auth.controller;

import com.zlwang.school.common.api.ApiResult;
import com.zlwang.school.modules.auth.dto.LoginRequest;
import com.zlwang.school.modules.auth.model.LoginClientInfo;
import com.zlwang.school.modules.auth.service.AuthService;
import com.zlwang.school.modules.auth.vo.CurrentUserResponse;
import com.zlwang.school.modules.auth.vo.LoginResponse;
import com.zlwang.school.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletRequest httpRequest
    ) {
        return ApiResult.success(authService.login(
            request,
            new LoginClientInfo(httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"))
        ));
    }

    @GetMapping("/me")
    public ApiResult<CurrentUserResponse> currentUser(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResult.success(CurrentUserResponse.from(user));
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        // JWT is stateless; the client removes its token after this protected endpoint succeeds.
        return ApiResult.success();
    }
}
