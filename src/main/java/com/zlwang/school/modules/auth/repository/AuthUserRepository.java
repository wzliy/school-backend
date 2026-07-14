package com.zlwang.school.modules.auth.repository;

import com.zlwang.school.modules.auth.model.AuthUserAccount;
import java.util.Optional;

public interface AuthUserRepository {

    Optional<AuthUserAccount> findByUsername(String username);
}
