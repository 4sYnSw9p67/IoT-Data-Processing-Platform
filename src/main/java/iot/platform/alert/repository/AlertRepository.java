package iot.platform.alert.repository;

import iot.platform.alert.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID> {

    List<Alert> findAllByOwnerUserIdOrderByRaisedAtDesc(UUID ownerUserId);

    @Query("select a from Alert a where a.ownerUserId = :owner and a.acknowledgedAt is null order by a.raisedAt desc")
    List<Alert> findUnacknowledged(@Param("owner") UUID ownerUserId);
}
