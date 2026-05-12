package iot.platform.measurement.repository;

import iot.platform.device.model.Device;
import iot.platform.measurement.model.Measurement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, UUID> {

    Page<Measurement> findByDevice(Device device, Pageable pageable);

    @Query("select m from Measurement m where m.device = :device and m.takenAt between :from and :to order by m.takenAt asc")
    List<Measurement> findRange(@Param("device") Device device, @Param("from") Instant from, @Param("to") Instant to);

    Optional<Measurement> findFirstByDeviceOrderByTakenAtDesc(Device device);

    @Modifying
    @Query("delete from Measurement m where m.device = :device")
    int deleteByDevice(@Param("device") Device device);

    @Modifying
    @Query("delete from Measurement m where m.takenAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);

    long countByDevice(Device device);
}
