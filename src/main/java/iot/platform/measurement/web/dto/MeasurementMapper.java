package iot.platform.measurement.web.dto;

import iot.platform.measurement.model.Measurement;

public final class MeasurementMapper {

    private MeasurementMapper() {
    }

    public static MeasurementResponse toResponse(Measurement measurement) {
        return MeasurementResponse.builder()
                .id(measurement.getId())
                .deviceId(measurement.getDevice().getId())
                .takenAt(measurement.getTakenAt())
                .temperatureC(measurement.getTemperatureC())
                .humidityPct(measurement.getHumidityPct())
                .createdAt(measurement.getCreatedAt())
                .build();
    }
}
