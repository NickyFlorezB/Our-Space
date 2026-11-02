package com.nicky.ourspace.infrastructure.drivenadapters.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nicky.ourspace.application.dto.AuthenticationTokensDto;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.dbo.RefreshTokenDbo;
import com.nicky.ourspace.infrastructure.drivenadapters.persistence.repository.JpaRefreshTokenRepository;
import com.nicky.ourspace.infrastructure.drivenadapters.security.exception.RefreshTokenExpiredException;
import com.nicky.ourspace.infrastructure.drivenadapters.security.exception.RefreshTokenNotFoundException;
import com.nicky.ourspace.infrastructure.drivenadapters.security.exception.RefreshTokenRevokedException;

import jakarta.transaction.Transactional;

@Service
public class RefreshTokenService {

    private final JpaRefreshTokenRepository repository;
    private final JwtService jwtService;

    public RefreshTokenService(JpaRefreshTokenRepository repository, JwtService jwtService) {
        this.repository = repository;
        this.jwtService = jwtService;
    }

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AuthenticationTokensDto refresh(String rawToken) {

        String hash = hashToken(rawToken);

        RefreshTokenDbo stored = repository
                .findByTokenHash(hash)
                .orElseThrow(() -> new RefreshTokenNotFoundException("Refresh token not found"));

        if (stored.getRevokedAt() != null) {
            throw new RefreshTokenRevokedException("Refresh token has been revoked");
        }

        if (!stored.getExpiresAt().isAfter(Instant.now())) {
            throw new RefreshTokenExpiredException("Refresh token has expired");
        }

        UUID userId = stored.getUserId();

        stored.setRevokedAt(Instant.now());

        String newRefreshToken = generateRefreshToken();

        RefreshTokenDbo replacement = new RefreshTokenDbo();

        replacement.setUserId(userId);
        replacement.setFamilyId(stored.getFamilyId());
        replacement.setTokenHash(hashToken(newRefreshToken));
        replacement.setCreatedAt(Instant.now());
        replacement.setExpiresAt(stored.getExpiresAt());

        repository.save(replacement);

        String accessToken = this.jwtService.generateAccessToken(userId);

        return new AuthenticationTokensDto(accessToken, newRefreshToken);
    }

    @Transactional
    public String create(UUID userId) {

        String refreshToken = generateRefreshToken();
        Instant now = Instant.now();

        RefreshTokenDbo entity = new RefreshTokenDbo();

        entity.setUserId(userId);
        entity.setFamilyId(UUID.randomUUID());
        entity.setTokenHash(hashToken(refreshToken));
        entity.setCreatedAt(now);
        entity.setExpiresAt(now.plus(7, ChronoUnit.DAYS));

        repository.save(entity);

        return refreshToken;
    }

    @Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(hashToken(rawToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(Instant.now());
            }
        });
    }

    private String generateRefreshToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest
                    .getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
