package iot.platform.rule.repository;

import iot.platform.device.model.Device;
import iot.platform.rule.model.AutomationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AutomationRuleRepository extends JpaRepository<AutomationRule, UUID> {

    List<AutomationRule> findAllByOwnerUserIdOrderByCreatedAtDesc(UUID ownerUserId);

    List<AutomationRule> findAllByDeviceAndEnabledTrue(Device device);
}
