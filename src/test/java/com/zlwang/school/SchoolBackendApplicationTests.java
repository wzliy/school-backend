package com.zlwang.school;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class SchoolBackendApplicationTests {

    private static final String LEGACY_ADMIN_HASH =
        "$2y$10$AT7CX..4P1ofYP8xM/j5cOXEDIvskr6yCAtYz5WHIXBm97Luq5IWa";

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void contextLoads() {
    }

    @Test
    void passwordEncoderSupportsLegacyBcryptAndPrefixesNewHashes() {
        assertThat(passwordEncoder.matches("Admin@123456", LEGACY_ADMIN_HASH)).isTrue();
        assertThat(passwordEncoder.encode("Admin@123456")).startsWith("{bcrypt}");
    }
}
