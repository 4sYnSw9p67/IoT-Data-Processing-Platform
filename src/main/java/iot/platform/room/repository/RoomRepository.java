package iot.platform.room.repository;

import iot.platform.room.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findAllByOwnerUserIdOrderByNameAsc(UUID ownerUserId);

    Optional<Room> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);

    boolean existsByOwnerUserIdAndName(UUID ownerUserId, String name);
}
