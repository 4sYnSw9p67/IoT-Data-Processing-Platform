package iot.platform.forecast.client.dto;

import java.time.Instant;
import java.util.UUID;

public record LocationResponseDto(
        UUID id,
        UUID ownerUserId,
        String label,
        String country,
        double latitude,
        double longitude,
        String timezone,
        Instant lastPulledAt,
        Instant createdAt,
        Instant updatedAt
) {
}
