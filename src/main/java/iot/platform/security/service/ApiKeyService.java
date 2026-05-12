package iot.platform.security.service;

import iot.platform.exception.NotFoundException;
import iot.platform.security.OwnershipGuard;
import iot.platform.security.model.ApiKey;
import iot.platform.security.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String KEY_PREFIX = "iot_";

    private final ApiKeyRepository apiKeyRepository;
    private final OwnershipGuard ownershipGuard;

    @Transactional
    public Issued issue(UUID ownerUserId, String label) {
        byte[] raw = new byte[32];
        RANDOM.nextBytes(raw);
        String tokenValue = KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        ApiKey entity = ApiKey.builder()
                .ownerUserId(ownerUserId)
                .keyHash(hash(tokenValue))
                .label(label == null || label.isBlank() ? "default" : label)
                .build();
        ApiKey saved = apiKeyRepository.save(entity);
        log.info("API key issued id={} label={} owner={}", saved.getId(), saved.getLabel(), ownerUserId);
        return new Issued(saved, tokenValue);
    }

    @Transactional(readOnly = true)
    public List<ApiKey> listForUser(UUID ownerUserId) {
        return apiKeyRepository.findAllByOwnerUserIdOrderByCreatedAtDesc(ownerUserId);
    }

    @Transactional
    public void revoke(UUID id) {
        ApiKey entity = apiKeyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("API key not found: " + id));
        ownershipGuard.checkOwnership(entity.getOwnerUserId());
        entity.setRevokedAt(Instant.now());
        log.info("API key revoked id={} owner={}", id, entity.getOwnerUserId());
    }

    @Transactional
    public Optional<ApiKey> authenticate(String presented) {
        if (presented == null || presented.isBlank()) {
            return Optional.empty();
        }
        return apiKeyRepository.findByKeyHash(hash(presented))
                .filter(ApiKey::isActive)
                .map(entity -> {
                    entity.setLastUsedAt(Instant.now());
                    return entity;
                });
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

    public record Issued(ApiKey entity, String secretToken) {
    }
}
