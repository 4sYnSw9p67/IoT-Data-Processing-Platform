package iot.platform.security.repository;

import iot.platform.security.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {

    Optional<ApiKey> findByKeyHash(String keyHash);

    List<ApiKey> findAllByOwnerUserIdOrderByCreatedAtDesc(UUID ownerUserId);
}
