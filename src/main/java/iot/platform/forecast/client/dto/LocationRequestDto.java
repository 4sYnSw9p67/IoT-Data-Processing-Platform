package iot.platform.forecast.client.dto;

import java.util.UUID;

public record LocationRequestDto(
        UUID ownerUserId,
        String label,
        String country,
        Double latitude,
        Double longitude
) {
}
