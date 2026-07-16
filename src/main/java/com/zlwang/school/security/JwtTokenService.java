package com.zlwang.school.security;

import java.time.Instant;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtProperties properties;
    private final JwtEncoder jwtEncoder;

    public JwtTokenService(JwtProperties properties, JwtEncoder jwtEncoder) {
        this.properties = properties;
        this.jwtEncoder = jwtEncoder;
    }

    public String generateToken(AuthenticatedUser user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.getAccessTokenTtl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(properties.getIssuer())
            .subject(user.getUsername())
            .claim("uid", user.id())
            .claim("authorities", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .distinct()
                .sorted()
                .toList())
            .issuedAt(issuedAt)
            .expiresAt(expiresAt)
            .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public long expiresInSeconds() {
        return properties.getAccessTokenTtl().toSeconds();
    }
}
