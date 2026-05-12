package iot.platform.rule.web.dto;

import iot.platform.rule.model.AutomationRule;

public final class RuleMapper {

    private RuleMapper() {
    }

    public static RuleResponse toResponse(AutomationRule rule) {
        return RuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .deviceId(rule.getDevice().getId())
                .deviceName(rule.getDevice().getName())
                .ownerUserId(rule.getOwnerUserId())
                .conditionType(rule.getConditionType())
                .threshold(rule.getThreshold())
                .severity(rule.getSeverity())
                .enabled(rule.isEnabled())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
