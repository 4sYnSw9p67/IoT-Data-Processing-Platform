package iot.platform.rule.service;

import iot.platform.alert.model.AlertSeverity;
import iot.platform.alert.model.AlertSource;
import iot.platform.device.model.Device;
import iot.platform.event.AlertRaisedEvent;
import iot.platform.event.MeasurementIngestedEvent;
import iot.platform.measurement.model.Measurement;
import iot.platform.rule.model.AutomationRule;
import iot.platform.rule.model.RuleConditionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RuleEvaluator {

    private final AutomationRuleService ruleService;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    @Transactional
    public void onMeasurement(MeasurementIngestedEvent event) {
        Measurement measurement = event.measurement();
        Device device = measurement.getDevice();
        evaluateDeviceThresholds(device, measurement);
        evaluateAutomationRules(device, measurement);
    }

    private void evaluateDeviceThresholds(Device device, Measurement measurement) {
        Double temp = measurement.getTemperatureC();
        if (temp != null) {
            if (device.getMinTemperatureC() != null && temp < device.getMinTemperatureC()) {
                publishThresholdAlert(measurement,
                        "Temperature %.1f°C below configured minimum %.1f°C".formatted(temp, device.getMinTemperatureC()));
            }
            if (device.getMaxTemperatureC() != null && temp > device.getMaxTemperatureC()) {
                publishThresholdAlert(measurement,
                        "Temperature %.1f°C above configured maximum %.1f°C".formatted(temp, device.getMaxTemperatureC()));
            }
        }
        Double humidity = measurement.getHumidityPct();
        if (humidity != null) {
            if (device.getMinHumidityPct() != null && humidity < device.getMinHumidityPct()) {
                publishThresholdAlert(measurement,
                        "Humidity %.1f%% below configured minimum %.1f%%".formatted(humidity, device.getMinHumidityPct()));
            }
            if (device.getMaxHumidityPct() != null && humidity > device.getMaxHumidityPct()) {
                publishThresholdAlert(measurement,
                        "Humidity %.1f%% above configured maximum %.1f%%".formatted(humidity, device.getMaxHumidityPct()));
            }
        }
    }

    private void evaluateAutomationRules(Device device, Measurement measurement) {
        List<AutomationRule> rules = ruleService.activeRulesForDevice(device);
        for (AutomationRule rule : rules) {
            if (matches(rule, measurement)) {
                String message = formatMessage(rule, measurement);
                log.info("Rule {} matched for device {} measurement {}",
                        rule.getId(), device.getId(), measurement.getId());
                eventPublisher.publishEvent(new AlertRaisedEvent(
                        measurement, AlertSource.RULE, rule.getSeverity(), message, rule.getId()));
            }
        }
    }

    private boolean matches(AutomationRule rule, Measurement measurement) {
        Double value = switch (rule.getConditionType()) {
            case TEMP_ABOVE, TEMP_BELOW -> measurement.getTemperatureC();
            case HUMIDITY_ABOVE, HUMIDITY_BELOW -> measurement.getHumidityPct();
        };
        if (value == null) {
            return false;
        }
        return switch (rule.getConditionType()) {
            case TEMP_ABOVE, HUMIDITY_ABOVE -> value > rule.getThreshold();
            case TEMP_BELOW, HUMIDITY_BELOW -> value < rule.getThreshold();
        };
    }

    private String formatMessage(AutomationRule rule, Measurement measurement) {
        String label = switch (rule.getConditionType()) {
            case TEMP_ABOVE -> "Temperature above";
            case TEMP_BELOW -> "Temperature below";
            case HUMIDITY_ABOVE -> "Humidity above";
            case HUMIDITY_BELOW -> "Humidity below";
        };
        Double value = switch (rule.getConditionType()) {
            case TEMP_ABOVE, TEMP_BELOW -> measurement.getTemperatureC();
            case HUMIDITY_ABOVE, HUMIDITY_BELOW -> measurement.getHumidityPct();
        };
        return "[%s] %s threshold %.2f (measured %.2f)".formatted(
                rule.getName(), label, rule.getThreshold(), value);
    }

    private void publishThresholdAlert(Measurement measurement, String message) {
        log.info("Device threshold breached device={} message={}", measurement.getDevice().getId(), message);
        eventPublisher.publishEvent(new AlertRaisedEvent(
                measurement, AlertSource.THRESHOLD, AlertSeverity.WARNING, message, null));
    }
}
