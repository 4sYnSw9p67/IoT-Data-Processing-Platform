package iot.platform.twin.web.dto;

import iot.platform.twin.model.TwinType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record TwinResponse(
        UUID id,
        String name,
        TwinType type,
        UUID parentId,
        String parentName,
        UUID ownerUserId,
        String description,
        String floor,
        String color,
        Double latitude,
        Double longitude,
        long deviceCount,
        long childCount,
        Instant createdAt,
        Instant updatedAt
) {
}
