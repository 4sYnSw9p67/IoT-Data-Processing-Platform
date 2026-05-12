package iot.platform.security.repository;

import iot.platform.security.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("update RefreshToken r set r.revokedAt = :revokedAt where r.userId = :userId and r.revokedAt is null")
    int revokeAllForUser(@Param("userId") UUID userId, @Param("revokedAt") Instant revokedAt);

    @Modifying
    @Query("delete from RefreshToken r where r.expiresAt < :cutoff")
    int deleteExpiredBefore(@Param("cutoff") Instant cutoff);
}
