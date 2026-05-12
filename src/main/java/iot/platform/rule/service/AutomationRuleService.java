package iot.platform.rule.service;

import iot.platform.aspect.Auditable;
import iot.platform.device.model.Device;
import iot.platform.device.service.DeviceService;
import iot.platform.exception.NotFoundException;
import iot.platform.rule.model.AutomationRule;
import iot.platform.rule.repository.AutomationRuleRepository;
import iot.platform.rule.web.dto.RuleMapper;
import iot.platform.rule.web.dto.RuleRequest;
import iot.platform.rule.web.dto.RuleResponse;
import iot.platform.rule.web.dto.RuleUpdateRequest;
import iot.platform.security.OwnershipGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomationRuleService {

    private final AutomationRuleRepository ruleRepository;
    private final DeviceService deviceService;
    private final OwnershipGuard ownershipGuard;

    @Transactional(readOnly = true)
    public List<RuleResponse> listForUser(UUID ownerUserId) {
        return ruleRepository.findAllByOwnerUserIdOrderByCreatedAtDesc(ownerUserId).stream()
                .map(RuleMapper::toResponse)
                .toList();
    }

    @Auditable("rule.create")
    @Transactional
    public RuleResponse create(UUID ownerUserId, RuleRequest request) {
        Device device = deviceService.loadOwned(request.deviceId());
        AutomationRule rule = AutomationRule.builder()
                .name(request.name())
                .device(device)
                .ownerUserId(ownerUserId)
                .conditionType(request.conditionType())
                .threshold(request.threshold())
                .severity(request.severity())
                .enabled(request.enabled() == null || request.enabled())
                .build();
        AutomationRule saved = ruleRepository.save(rule);
        log.info("Automation rule created id={} device={} owner={}",
                saved.getId(), device.getId(), ownerUserId);
        return RuleMapper.toResponse(saved);
    }

    @Auditable("rule.update")
    @Transactional
    public RuleResponse update(UUID id, RuleUpdateRequest request) {
        AutomationRule rule = loadOwned(id);
        if (request.name() != null) {
            rule.setName(request.name());
        }
        if (request.conditionType() != null) {
            rule.setConditionType(request.conditionType());
        }
        if (request.threshold() != null) {
            rule.setThreshold(request.threshold());
        }
        if (request.severity() != null) {
            rule.setSeverity(request.severity());
        }
        if (request.enabled() != null) {
            rule.setEnabled(request.enabled());
        }
        log.info("Automation rule updated id={} enabled={}", rule.getId(), rule.isEnabled());
        return RuleMapper.toResponse(rule);
    }

    @Auditable("rule.delete")
    @Transactional
    public void delete(UUID id) {
        AutomationRule rule = loadOwned(id);
        ruleRepository.delete(rule);
        log.info("Automation rule deleted id={} owner={}", id, rule.getOwnerUserId());
    }

    @Transactional(readOnly = true)
    public List<AutomationRule> activeRulesForDevice(Device device) {
        return ruleRepository.findAllByDeviceAndEnabledTrue(device);
    }

    private AutomationRule loadOwned(UUID id) {
        AutomationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rule not found: " + id));
        ownershipGuard.checkOwnership(rule.getOwnerUserId());
        return rule;
    }
}
