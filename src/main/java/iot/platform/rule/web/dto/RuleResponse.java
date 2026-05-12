package iot.platform.rule.web.dto;

import iot.platform.alert.model.AlertSeverity;
import iot.platform.rule.model.RuleConditionType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record RuleResponse(
        UUID id,
        String name,
        UUID deviceId,
        String deviceName,
        UUID ownerUserId,
        RuleConditionType conditionType,
        Double threshold,
        AlertSeverity severity,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {
}
