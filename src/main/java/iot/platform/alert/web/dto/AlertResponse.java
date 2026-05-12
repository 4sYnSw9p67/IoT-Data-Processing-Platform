package iot.platform.alert.web.dto;

import iot.platform.alert.model.AlertSeverity;
import iot.platform.alert.model.AlertSource;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record AlertResponse(
        UUID id,
        UUID deviceId,
        String deviceName,
        UUID ownerUserId,
        AlertSource source,
        AlertSeverity severity,
        String message,
        UUID ruleId,
        Instant raisedAt,
        Instant acknowledgedAt
) {
}
