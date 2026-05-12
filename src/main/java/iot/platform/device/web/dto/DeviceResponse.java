package iot.platform.device.web.dto;

import iot.platform.device.model.DeviceType;
import iot.platform.twin.model.TwinType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record DeviceResponse(
        UUID id,
        String name,
        DeviceType type,
        UUID twinId,
        String twinName,
        TwinType twinType,
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
