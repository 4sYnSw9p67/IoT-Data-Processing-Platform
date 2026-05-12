package iot.platform.alert.web.dto;

import iot.platform.alert.model.Alert;
import iot.platform.device.model.Device;

public final class AlertMapper {

    private AlertMapper() {
    }

    public static AlertResponse toResponse(Alert alert) {
        Device device = alert.getDevice();
        return AlertResponse.builder()
                .id(alert.getId())
                .deviceId(device.getId())
                .deviceName(device.getName())
                .ownerUserId(alert.getOwnerUserId())
                .source(alert.getSource())
                .severity(alert.getSeverity())
                .message(alert.getMessage())
                .ruleId(alert.getRuleId())
                .raisedAt(alert.getRaisedAt())
                .acknowledgedAt(alert.getAcknowledgedAt())
                .build();
    }
}
