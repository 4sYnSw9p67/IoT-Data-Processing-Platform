package iot.platform.forecast.client.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record LocationForecastDto(
        UUID locationId,
        String label,
        String timezone,
        Instant pulledAt,
        List<ForecastDayDto> days
) implements Serializable {
}
