package iot.platform.alert.service;

import iot.platform.alert.model.Alert;
import iot.platform.alert.repository.AlertRepository;
import iot.platform.aspect.Auditable;
import iot.platform.alert.web.dto.AlertMapper;
import iot.platform.alert.web.dto.AlertResponse;
import iot.platform.event.AlertRaisedEvent;
import iot.platform.exception.NotFoundException;
import iot.platform.security.OwnershipGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final OwnershipGuard ownershipGuard;

    @Transactional(readOnly = true)
    public List<AlertResponse> listForUser(UUID ownerUserId, boolean unacknowledgedOnly) {
        List<Alert> alerts = unacknowledgedOnly
                ? alertRepository.findUnacknowledged(ownerUserId)
                : alertRepository.findAllByOwnerUserIdOrderByRaisedAtDesc(ownerUserId);
        return alerts.stream().map(AlertMapper::toResponse).toList();
    }

    @Auditable("alert.acknowledge")
    @Transactional
    public AlertResponse acknowledge(UUID id) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Alert not found: " + id));
        ownershipGuard.checkOwnership(alert.getOwnerUserId());
        if (alert.getAcknowledgedAt() == null) {
            alert.setAcknowledgedAt(Instant.now());
            log.info("Alert acknowledged id={} owner={}", alert.getId(), alert.getOwnerUserId());
        }
        return AlertMapper.toResponse(alert);
    }

    @EventListener
    @Transactional
    public void onAlertRaised(AlertRaisedEvent event) {
        Alert alert = Alert.builder()
                .device(event.measurement().getDevice())
                .ownerUserId(event.measurement().getDevice().getOwnerUserId())
                .source(event.source())
                .severity(event.severity())
                .message(event.message())
                .ruleId(event.ruleId())
                .raisedAt(Instant.now())
                .build();
        Alert saved = alertRepository.save(alert);
        log.info("Alert persisted id={} source={} severity={} device={}",
                saved.getId(), saved.getSource(), saved.getSeverity(), saved.getDevice().getId());
    }
}
