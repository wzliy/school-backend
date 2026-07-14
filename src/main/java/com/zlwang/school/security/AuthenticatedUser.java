package com.zlwang.school.security;

import com.zlwang.school.modules.auth.model.AuthPermission;
import com.zlwang.school.modules.auth.model.AuthUserAccount;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedUser implements UserDetails {

    private final AuthUserAccount account;
    private final List<GrantedAuthority> authorities;

    public AuthenticatedUser(AuthUserAccount account) {
        this.account = account;
        this.authorities = Stream.concat(
                account.roleCodes().stream().map(role -> "ROLE_" + role),
                account.permissions().stream().map(AuthPermission::code)
            )
            .distinct()
            .map(SimpleGrantedAuthority::new)
            .map(GrantedAuthority.class::cast)
            .toList();
    }

    public long id() {
        return account.id();
    }

    public String realName() {
        return account.realName();
    }

    public String avatarUrl() {
        return account.avatarUrl();
    }

    public List<String> roleCodes() {
        return account.roleCodes();
    }

    public List<AuthPermission> permissions() {
        return account.permissions();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return account.password();
    }

    @Override
    public String getUsername() {
        return account.username();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return account.enabled();
    }
}
