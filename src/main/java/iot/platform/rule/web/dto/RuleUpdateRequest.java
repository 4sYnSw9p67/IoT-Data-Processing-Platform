package iot.platform.rule.web.dto;

import iot.platform.alert.model.AlertSeverity;
import iot.platform.rule.model.RuleConditionType;
import jakarta.validation.constraints.Size;

public record RuleUpdateRequest(

        @Size(max = 100)
        String name,

        RuleConditionType conditionType,

        Double threshold,

        AlertSeverity severity,

        Boolean enabled
) {
}
