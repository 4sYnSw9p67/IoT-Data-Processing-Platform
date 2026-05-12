package iot.platform.rule.service;

import iot.platform.alert.model.AlertSeverity;
import iot.platform.device.model.Device;
import iot.platform.device.model.DeviceType;
import iot.platform.event.AlertRaisedEvent;
import iot.platform.event.MeasurementIngestedEvent;
import iot.platform.measurement.model.Measurement;
import iot.platform.rule.model.AutomationRule;
import iot.platform.rule.model.RuleConditionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEvaluatorUnitTest {

    @Mock
    private AutomationRuleService ruleService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private RuleEvaluator evaluator;

    private Device device;

    @BeforeEach
    void setUp() {
        device = Device.builder()
                .id(UUID.randomUUID())
                .name("Bedroom")
                .type(DeviceType.COMBO)
                .ownerUserId(UUID.randomUUID())
                .minTemperatureC(18.0)
                .maxTemperatureC(26.0)
                .minHumidityPct(35.0)
                .maxHumidityPct(65.0)
                .active(true)
                .build();
    }

    @Test
    void thresholdBreach_emitsTwoEvents_whenTempAndHumidityHigh() {
        Measurement m = Measurement.builder()
                .id(UUID.randomUUID())
                .device(device)
                .takenAt(Instant.now())
                .temperatureC(30.0)
                .humidityPct(80.0)
                .build();
        when(ruleService.activeRulesForDevice(device)).thenReturn(List.of());

        evaluator.onMeasurement(new MeasurementIngestedEvent(device.getId(), m.getId(), m));

        verify(eventPublisher, times(2)).publishEvent(any(AlertRaisedEvent.class));
    }

    @Test
    void noBreach_noEvents() {
        Measurement m = Measurement.builder()
                .id(UUID.randomUUID())
                .device(device)
                .takenAt(Instant.now())
                .temperatureC(22.0)
                .humidityPct(50.0)
                .build();
        when(ruleService.activeRulesForDevice(device)).thenReturn(List.of());

        evaluator.onMeasurement(new MeasurementIngestedEvent(device.getId(), m.getId(), m));

        verify(eventPublisher, never()).publishEvent(any(AlertRaisedEvent.class));
    }

    @Test
    void thresholdBelow_emits() {
        Measurement m = Measurement.builder()
                .id(UUID.randomUUID())
                .device(device)
                .takenAt(Instant.now())
                .temperatureC(15.0)
                .humidityPct(20.0)
                .build();
        when(ruleService.activeRulesForDevice(device)).thenReturn(List.of());

        evaluator.onMeasurement(new MeasurementIngestedEvent(device.getId(), m.getId(), m));

        verify(eventPublisher, times(2)).publishEvent(any(AlertRaisedEvent.class));
    }

    @Test
    void automationRule_matchesTempAbove() {
        AutomationRule rule = AutomationRule.builder()
                .id(UUID.randomUUID())
                .name("Hot day")
                .device(device)
                .ownerUserId(device.getOwnerUserId())
                .conditionType(RuleConditionType.TEMP_ABOVE)
                .threshold(25.0)
                .severity(AlertSeverity.CRITICAL)
                .enabled(true)
                .build();
        Measurement m = Measurement.builder()
                .id(UUID.randomUUID())
                .device(device)
                .takenAt(Instant.now())
                .temperatureC(28.0)
                .build();
        when(ruleService.activeRulesForDevice(device)).thenReturn(List.of(rule));

        evaluator.onMeasurement(new MeasurementIngestedEvent(device.getId(), m.getId(), m));

        ArgumentCaptor<AlertRaisedEvent> captor = ArgumentCaptor.forClass(AlertRaisedEvent.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        assertThat(captor.getAllValues())
                .anySatisfy(ev -> {
                    assertThat(ev.severity()).isEqualTo(AlertSeverity.CRITICAL);
                    assertThat(ev.message()).contains("Hot day");
                });
    }

    @Test
    void automationRule_humidityBelow_matchesWithEmptyTemp() {
        AutomationRule rule = AutomationRule.builder()
                .id(UUID.randomUUID())
                .name("Too dry")
                .device(device)
                .ownerUserId(device.getOwnerUserId())
                .conditionType(RuleConditionType.HUMIDITY_BELOW)
                .threshold(30.0)
                .severity(AlertSeverity.WARNING)
                .enabled(true)
                .build();
        Measurement m = Measurement.builder()
                .id(UUID.randomUUID())
                .device(device)
                .takenAt(Instant.now())
                .humidityPct(25.0)
                .build();
        when(ruleService.activeRulesForDevice(device)).thenReturn(List.of(rule));

        evaluator.onMeasurement(new MeasurementIngestedEvent(device.getId(), m.getId(), m));

        verify(eventPublisher, times(2)).publishEvent(any(AlertRaisedEvent.class));
    }

    @Test
    void automationRule_nullValue_skips() {
        AutomationRule rule = AutomationRule.builder()
                .id(UUID.randomUUID())
                .name("Hot day")
                .device(device)
                .ownerUserId(device.getOwnerUserId())
                .conditionType(RuleConditionType.TEMP_ABOVE)
                .threshold(25.0)
                .severity(AlertSeverity.WARNING)
                .enabled(true)
                .build();
        Measurement m = Measurement.builder()
                .id(UUID.randomUUID())
                .device(device)
                .takenAt(Instant.now())
                .humidityPct(50.0)
                .build();
        when(ruleService.activeRulesForDevice(device)).thenReturn(List.of(rule));

        evaluator.onMeasurement(new MeasurementIngestedEvent(device.getId(), m.getId(), m));

        verify(eventPublisher, never()).publishEvent(any(AlertRaisedEvent.class));
    }

    private static <T> T any(Class<T> type) {
        return org.mockito.ArgumentMatchers.any(type);
    }
}
