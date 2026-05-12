package iot.platform.device.web.dto;

import iot.platform.device.model.DeviceType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record DeviceCreateRequest(

        @NotBlank
        @Size(max = 80)
        String name,

        @NotNull
        DeviceType type,

        UUID twinId,

        @DecimalMin("-100.0")
        @DecimalMax("100.0")
        Double minTemperatureC,

        @DecimalMin("-100.0")
        @DecimalMax("100.0")
        Double maxTemperatureC,

        @DecimalMin("0.0")
        @DecimalMax("100.0")
        Double minHumidityPct,

        @DecimalMin("0.0")
        @DecimalMax("100.0")
        Double maxHumidityPct,

        Boolean active
) {
}
