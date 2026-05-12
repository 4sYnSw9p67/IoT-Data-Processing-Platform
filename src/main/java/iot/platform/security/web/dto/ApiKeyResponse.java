package iot.platform.security.web.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record ApiKeyResponse(
        UUID id,
        String label,
        String secret,
        Instant lastUsedAt,
        Instant revokedAt,
        Instant createdAt
) {
}
