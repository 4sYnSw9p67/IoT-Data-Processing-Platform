package iot.platform.event;

import iot.platform.alert.model.AlertSeverity;
import iot.platform.alert.model.AlertSource;
import iot.platform.measurement.model.Measurement;

import java.util.UUID;

public record AlertRaisedEvent(
        Measurement measurement,
        AlertSource source,
        AlertSeverity severity,
        String message,
        UUID ruleId
) {
}
