package iot.platform.measurement.web.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record MeasurementResponse(
        UUID id,
        UUID deviceId,
        Instant takenAt,
        Double temperatureC,
        Double humidityPct,
        Instant createdAt
) {
}
