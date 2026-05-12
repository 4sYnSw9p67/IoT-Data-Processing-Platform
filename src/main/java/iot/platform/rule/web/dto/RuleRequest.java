package iot.platform.rule.web.dto;

import iot.platform.alert.model.AlertSeverity;
import iot.platform.rule.model.RuleConditionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record RuleRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        UUID deviceId,

        @NotNull
        RuleConditionType conditionType,

        @NotNull
        Double threshold,

        @NotNull
        AlertSeverity severity,

        Boolean enabled
) {
}
