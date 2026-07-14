package com.zlwang.school.security;

import com.zlwang.school.modules.auth.repository.AuthUserRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SchoolUserDetailsService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    public SchoolUserDetailsService(AuthUserRepository authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);
        return authUserRepository.findByUsername(normalizedUsername)
            .map(AuthenticatedUser::new)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在"));
    }
}
