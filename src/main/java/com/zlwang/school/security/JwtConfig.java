package com.zlwang.school.security;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration(proxyBeanMethods = false)
public class JwtConfig {

    @Bean
    SecretKey jwtSecretKey(JwtProperties properties) {
        byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(properties.getSecret());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "JWT 密钥必须是合法的 Base64 字符串",
                    exception
            );
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "HS256 密钥不能少于 32 字节"
            );
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey secretKey, JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.getIssuer()));

        return decoder;
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey secretKey) {
        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }
}
