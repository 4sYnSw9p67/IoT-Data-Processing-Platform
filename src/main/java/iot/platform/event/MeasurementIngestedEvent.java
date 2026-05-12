package iot.platform.event;

import iot.platform.measurement.model.Measurement;

import java.util.UUID;

public record MeasurementIngestedEvent(UUID deviceId, UUID measurementId, Measurement measurement) {
}
