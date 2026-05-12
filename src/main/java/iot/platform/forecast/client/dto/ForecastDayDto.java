package iot.platform.forecast.client.dto;

import java.io.Serializable;
import java.time.LocalDate;

public record ForecastDayDto(
        LocalDate date,
        Double tempMinC,
        Double tempMaxC,
        Double humidityPct,
        Double precipitationMm,
        Double windKmh
) implements Serializable {
}
