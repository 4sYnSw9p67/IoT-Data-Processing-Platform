package iot.platform.device.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

public record DeviceUpdateRequest(

        @Size(max = 80)
        String name,

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
