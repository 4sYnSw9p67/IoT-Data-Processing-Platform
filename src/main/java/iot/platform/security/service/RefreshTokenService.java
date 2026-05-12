package iot.platform.security.service;

import iot.platform.exception.InvalidTokenException;
import iot.platform.security.config.SecurityProperties;
import iot.platform.security.model.RefreshToken;
import iot.platform.security.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final RefreshTokenRepository repository;
    private final SecurityProperties securityProperties;

    @Transactional
    public IssuedRefreshToken issue(UUID userId) {
        byte[] raw = new byte[48];
        RANDOM.nextBytes(raw);
        String tokenValue = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        Duration ttl = Duration.ofDays(securityProperties.getJwt().getRefreshTokenTtlDays());
        RefreshToken entity = RefreshToken.builder()
                .userId(userId)
                .tokenHash(hash(tokenValue))
                .expiresAt(Instant.now().plus(ttl))
                .build();
        repository.save(entity);
        return new IssuedRefreshToken(tokenValue, entity.getExpiresAt());
    }

    @Transactional
    public RefreshToken rotate(String presentedTokenValue) {
        String tokenHash = hash(presentedTokenValue);
        RefreshToken stored = repository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not recognised"));
        if (!stored.isActive(Instant.now())) {
            throw new InvalidTokenException("Refresh token is no longer valid");
        }
        stored.setRevokedAt(Instant.now());
        repository.save(stored);
        return stored;
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        int revoked = repository.revokeAllForUser(userId, Instant.now());
        if (revoked > 0) {
            log.info("Revoked {} active refresh tokens for user {}", revoked, userId);
        }
    }

    @Transactional
    public int purgeExpired() {
        return repository.deleteExpiredBefore(Instant.now());
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    public record IssuedRefreshToken(String value, Instant expiresAt) {
    }
}
