package iot.platform.measurement.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record MeasurementCreateRequest(

        @NotNull
        Instant takenAt,

        @DecimalMin("-100.0")
        @DecimalMax("100.0")
        Double temperatureC,

        @DecimalMin("0.0")
        @DecimalMax("100.0")
        Double humidityPct,

        String rawPayload
) {
}
