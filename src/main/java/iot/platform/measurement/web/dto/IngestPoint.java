package iot.platform.measurement.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public record IngestPoint(

        @NotNull
        UUID deviceId,

        @NotNull
        Instant takenAt,

        @DecimalMin("-100.0")
        @DecimalMax("100.0")
        Double temperatureC,

        @DecimalMin("0.0")
        @DecimalMax("100.0")
        Double humidityPct,

        @Size(max = 512)
        String rawPayload
) {
}
