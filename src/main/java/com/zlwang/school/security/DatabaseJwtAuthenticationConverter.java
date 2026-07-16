package com.zlwang.school.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DatabaseJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserDetailsService userDetailsService;

    public DatabaseJwtAuthenticationConverter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        if (!StringUtils.hasText(jwt.getSubject())) {
            throw new BadCredentialsException("JWT subject 缺失");
        }

        AuthenticatedUser user = loadUser(jwt.getSubject());
        Object userIdClaim = jwt.getClaims().get("uid");
        if (!(userIdClaim instanceof Number userId) || userId.longValue() != user.id()) {
            throw new BadCredentialsException("JWT 用户标识无效");
        }
        if (!user.isEnabled()) {
            throw new DisabledException("用户已被禁用");
        }

        return UsernamePasswordAuthenticationToken.authenticated(user, jwt, user.getAuthorities());
    }

    private AuthenticatedUser loadUser(String username) {
        try {
            UserDetails user = userDetailsService.loadUserByUsername(username);
            if (user instanceof AuthenticatedUser authenticatedUser) {
                return authenticatedUser;
            }
            throw new BadCredentialsException("认证用户类型无效");
        } catch (UsernameNotFoundException exception) {
            throw new BadCredentialsException("JWT 对应用户不存在", exception);
        }
    }
}
