package iot.platform.twin.repository;

import iot.platform.twin.model.DigitalTwin;
import iot.platform.twin.model.TwinType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DigitalTwinRepository extends JpaRepository<DigitalTwin, UUID> {

    List<DigitalTwin> findAllByOwnerUserIdOrderByTypeAscNameAsc(UUID ownerUserId);

    List<DigitalTwin> findAllByOwnerUserIdAndTypeOrderByNameAsc(UUID ownerUserId, TwinType type);

    List<DigitalTwin> findAllByOwnerUserIdAndParentIsNullOrderByNameAsc(UUID ownerUserId);

    List<DigitalTwin> findAllByParentIdOrderByNameAsc(UUID parentId);

    boolean existsByOwnerUserIdAndParentIdAndName(UUID ownerUserId, UUID parentId, String name);

    boolean existsByOwnerUserIdAndParentIsNullAndName(UUID ownerUserId, String name);

    long countByParentId(UUID parentId);

    long countByOwnerUserId(UUID ownerUserId);

    long countByOwnerUserIdAndType(UUID ownerUserId, TwinType type);
}
