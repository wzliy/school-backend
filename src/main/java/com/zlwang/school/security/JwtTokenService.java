package com.zlwang.school.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class JwtTokenService {

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtTokenService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(AuthenticatedUser user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(properties.getAccessTokenTtl());
        return Jwts.builder()
            .issuer(properties.getIssuer())
            .subject(user.getUsername())
            .claim("uid", user.id())
            .issuedAt(Date.from(issuedAt))
            .expiration(Date.from(expiresAt))
            .signWith(signingKey)
            .compact();
    }

    public String parseUsername(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(signingKey)
            .requireIssuer(properties.getIssuer())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        if (!StringUtils.hasText(claims.getSubject())) {
            throw new IllegalArgumentException("JWT subject is missing");
        }
        return claims.getSubject();
    }

    public long expiresInSeconds() {
        return properties.getAccessTokenTtl().toSeconds();
    }
}
