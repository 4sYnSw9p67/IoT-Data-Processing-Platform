package iot.platform.device.repository;

import iot.platform.device.model.Device;
import iot.platform.twin.model.DigitalTwin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    List<Device> findAllByOwnerUserIdOrderByNameAsc(UUID ownerUserId);

    Optional<Device> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);

    boolean existsByOwnerUserIdAndName(UUID ownerUserId, String name);

    long countByTwin(DigitalTwin twin);

    long countByOwnerUserId(UUID ownerUserId);
}
