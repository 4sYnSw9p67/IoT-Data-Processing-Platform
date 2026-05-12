package iot.platform.device.web.dto;

import iot.platform.device.model.DeviceType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record DeviceResponse(
        UUID id,
        String name,
        DeviceType type,
        UUID roomId,
        String roomName,
        UUID ownerUserId,
        Double minTemperatureC,
        Double maxTemperatureC,
        Double minHumidityPct,
        Double maxHumidityPct,
        boolean active,
        Instant registeredAt,
        Instant updatedAt
) {
}
